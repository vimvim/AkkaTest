package rtmp.tests

import scala.concurrent.duration._

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.testkit.{TestFSMRef, TestActorRef, TestProbe}
import akka.io.Tcp.{Write, Received}

import rtmp.ConnHandler

/**
 *
 *
 */
class ConnTester (testSource:TestDataSource, implicit val system:ActorSystem) {

  val connProbe = TestProbe()
  val clientHandlerProbe = TestProbe()

  val connActor:TestActorRef[ConnHandler] = TestFSMRef(new ConnHandler(connProbe.ref, new InetSocketAddress(0), clientHandlerProbe.ref, testSource))

  def testActor() {

    // assert(connActor.underlyingActor.stateName == Auth)

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
                  throw new Exception("Test data not match")
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
