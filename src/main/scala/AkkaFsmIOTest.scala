import akka.actor._

import akka.io.Tcp.{Write, Received}
import akka.io.{ IO, Tcp }
import akka.testkit.{TestFSMRef, TestProbe, TestActorRef}
import akka.util.{CompactByteString, ByteString}
import java.net.InetSocketAddress
import scala.concurrent.duration._

/**
 * This file contain code for Akka FSM IO workbench.
 * This workshop will help to evaluate:
 * - How actor will receive data from network ( ie - what messages is used,... )
 *   Used: Received(data: ByteString)
 * - How to emulate receiving data from net ( for testing purposes )
 * - How to catch and test output of the Actor
 *   Actor send Write(data: ByteString, ack: Event)
 * - How to work with the FSM
 *
 * This simple Actor will:
 * - prompt for login
 * - grab login name
 * - continue grabs incoming messages and send them back as "echo" until user will enter "exit"
 * - after that Actor will be in the idle state for some time and close conn if no login name entered
 */

sealed trait State
case object Auth extends State
case object Running extends State

sealed trait Data
case object Uninitialized extends Data
case class SessionData(login:String) extends Data

class FsmConnHandler(connection: ActorRef, remote: InetSocketAddress) extends Actor with FSM[State, Data] with ActorLogging {

  import Tcp._

  // Actor terminates when connection breaks
  context watch connection

  startWith(Auth, Uninitialized)

  connection ! Write(CompactByteString("Hello ! \r\nEnter login: "))

  when(Auth) {
    case Event(Received(data), Uninitialized) ⇒

      val login = data.decodeString("UTF-8").trim
      log.info("Login entered:"+login)

      connection ! Write(CompactByteString("Welcome "+login+"\r\n"))
      connection ! Write(CompactByteString("Enter command: "))

      goto(Running) using SessionData(login)
  }

  when(Running) {

    case Event(Received(data), SessionData(login)) ⇒

      val command = data.decodeString("UTF-8").trim
      log.info("Command entered:"+login)

      if (command.equals("close")) {

        connection ! Write(CompactByteString("Good by ! \r\n\r\nHello ! \r\nEnter login: "))

        goto(Auth) using Uninitialized

      } else {

        connection ! Write(CompactByteString(login+"> "+command+"\r\n"))
        connection ! Write(CompactByteString("Enter command: "))

        stay()
      }
  }

  // TODO: Handle PeerClosed

  initialize()
}

class Server extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 2222))

  def receive = {
    case b @ Bound(localAddress) ⇒
      // do some logging or setup ...
      log.info("Bound to interface")

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒

      log.info("Connected:"+remote)

      val handler = context.actorOf(Props(classOf[FsmConnHandler], sender, remote))
      val connection = sender
      connection ! Register(handler)
  }
}

class TestStreamSource {

  val testInput = Array[String](
    "user1\r\n","cmd1\r\n","close"
  )

  val inputItr = testInput.iterator

  var testOutput:ByteString = CompactByteString("Hello ! \r\nEnter login: " +
    "Welcome user1\r\n" +
    "Enter command: user1> cmd1\r\n" +
    "Enter command: Good by ! \r\n\r\nHello ! \r\nEnter login: ")

  def getInputChunk:Option[ByteString] = {

    if (inputItr.hasNext) {
      Some(CompactByteString(inputItr.next()))
    } else {
      None
    }
  }

  def getOutputChunk(size:Int):ByteString = {

    val res = testOutput.splitAt(size)

    testOutput = res._2

    res._1
  }

}

/**
 * Will send input to the actor
 *
 * @param testSource
 */
class TcpStreamTester(testSource:TestStreamSource, implicit val system:ActorSystem) {

  val connProbe = TestProbe()

  val connActor:TestActorRef[FsmConnHandler] = TestFSMRef(new FsmConnHandler(connProbe.ref, new InetSocketAddress(0)))

  def testActor() {

    assert(connActor.underlyingActor.stateName == Auth)

    def sendNextChunk():Unit = {

      testSource.getInputChunk match {

        case Some(value) => {

          connActor ! Received(value)

          while (connProbe.msgAvailable) {

            val msg = connProbe.receiveOne(1.millisecond)
            msg match {

              case Write(outputValue, ack)=> {

                val testData = testSource.getOutputChunk(outputValue.length)
                if (!testData.sameElements(outputValue)) {
                  throw new Exception("Test data not match");
                }
              }
            }
          }

          sendNextChunk()
        }

        case None =>
      }
    }

    sendNextChunk()
  }
}


object AkkaFsmIOTest extends App {

  implicit val system = ActorSystem("akkaTest")

  val tester = new TcpStreamTester(new TestStreamSource(), system)
  tester.testActor()

  // val server = system.actorOf(Props[Server])


  import akka.testkit.TestFSMRef

  // TODO: Check how to implement using WordSpec
  // TODO: http://doc.akka.io/docs/akka/2.2.4/scala/testing.html

  /*
  val connProbe = TestProbe()

  val connActor:TestActorRef[ConnHandler] = TestFSMRef(new ConnHandler(connProbe.ref, new InetSocketAddress(0)))
  assert(connActor.underlyingActor.stateName == Auth)

  // TODO: IMPORTANT !!! WE SIMPLY WILL NEEDS TO SEND MESSAGES AND CHECK IS WE HAVE SOMETHING RECEIVED BY PROBE
  // TODO: IF PROBE RECEIVED SOMETHING - CHECK IT AND BACK TO SENDING MESSAGES !!!

  connActor ! Received(CompactByteString("user1"))

  connProbe.receiveWhile()
  */


}
