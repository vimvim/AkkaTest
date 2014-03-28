

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
 *
 */
class RtmpStreamTest(_system: ActorSystem)
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

  "An actors" should "be able to correctly process test stream" in {

    val testData = new TestDataSource("dump")

    val tester = new ConnTester(testData, system)
    tester.testActor()
  }

}
