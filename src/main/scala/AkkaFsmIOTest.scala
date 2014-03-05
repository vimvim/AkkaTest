import akka.actor._

import akka.io.{ IO, Tcp }
import akka.util.{CompactByteString, ByteString}
import java.net.InetSocketAddress

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

class ConnHandler(connection: ActorRef, remote: InetSocketAddress) extends Actor with FSM[State, Data] with ActorLogging {

  import Tcp._

  // Actor terminates when connection breaks
  context watch connection

  startWith(Auth, Uninitialized)

  connection ! Write(CompactByteString("Hello ! \r\nEnter login: "))

  when(Auth) {
    case Event(Received(data), Uninitialized) ⇒

      stay()

      goto(Running) using SessionData(data.decodeString("UTF-8"))
  }

  when(Running) {

    case Event(Received(data), SessionData(_)) ⇒

      val command = data.decodeString("UTF-8")

      if (command=="close") {

        goto(Auth) using Uninitialized

      } else {

        connection ! Write(CompactByteString("cmd: "+command))

        stay()
      }
  }

  // TODO: Handle PeerClosed

  initialize()
}

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 2222))

  def receive = {
    case b @ Bound(localAddress) ⇒
    // do some logging or setup ...

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒
      val handler = context.actorOf(Props(classOf[ConnHandler], sender, remote))
      val connection = sender
      connection ! Register(handler)
  }
}

object AkkaFsmIOTest extends App {

  val system = ActorSystem("akkaTest")

  val server = system.actorOf(Props[Server])

}
