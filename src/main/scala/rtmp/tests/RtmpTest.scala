package rtmp.tests

import akka.actor.ActorSystem
import akka.event.Logging

import com.typesafe.config.ConfigFactory


/**
 * Test RTMP protocol actors
 */
object RtmpTest  extends App {

  override def main(args: Array[String]) = {

    val config = ConfigFactory.load

    val system = ActorSystem("TestSystem", config)
    implicit val log = Logging.getLogger(system, this)

    log.info("RTMP Test started")

    val testData = new TestDataSource("dump")

    val tester = new ConnTester(testData, system)
    tester.testActor()
  }

}
