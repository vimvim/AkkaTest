

import scala.concurrent.duration._

import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.concurrent._
import org.scalatest.matchers.ShouldMatchers

import com.typesafe.config.ConfigFactory

import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import akka.event.Logging

import rtmp.tests.{ConnTester, TestDataSource}

/**
 * Test encrypted RTMP handshake
 */
class HandshakeV2Test(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with ShouldMatchers
  with FlatSpec
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("HelloAkkaSpec", ConfigFactory.load))

  implicit val log = Logging.getLogger(system, this)
  log.info("RTMP Test started")

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "An ConnHandler" should "be able to correctly process test stream" in {

    // TODO: Read in_1.rtmp packet and send to the ConnHandler actor
    // TODO: Get response and match to the out_1.rtmp

    // TODO: Needs to test only RTMP handshake in/out packets here.
    // val testData = new TestDataSource("dump")

    // val tester = new ConnTester(testData, system)
    // tester.testActor()
  }

}
