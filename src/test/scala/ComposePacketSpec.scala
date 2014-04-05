
import java.io.{FileInputStream, FileNotFoundException, File}

import scala.collection.immutable.{ListSet, Iterable}
import scala.concurrent.duration._

import akka.util.{ByteString, CompactByteString}
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }

import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.concurrent._
import org.scalatest._
import org.scalatest.matchers.{ClassicMatchers, Matchers, ShouldMatchers}

import rtmp.amf.{AmfNull, AMF0Encoding}
import rtmp.packet.{Invoke, InvokeDecoder}
import rtmp.amf.amf0.Amf0Deserializer

/**
 * Test compose of the RTMP packets
 */
class ComposePacketSpec extends FlatSpec with ClassicMatchers {

  "A composed ServerBW packet " should "match to the test data out_2.rtmp" in {

  }

  "A composed ClientBW packet " should "match to the test data out_3.rtmp" in {

  }

  "A composed PING PING_CLIENT packet " should "match to the test data out_4.rtmp" in {

  }

  "A composed invoke connect response packet " should "match to the test data out_5.rtmp" in {

    // action ( "_result" , "_error" - in case of error )
    // transaction id ()

    // conn params ( null )
    //


    val binaryData = readData("out_5.rtmp")
    val encoder = new InvokeEncoder(new DummyLogger())
    val packet = encoder.encode(new AMF0Encoding(), binaryData)

    // TODO: Note StatusObject is ICustomSerializable

    assert(packet.equals(Invoke("_result", 1, List(
      null,
      new StatusObject(
        "NetConnection.Connect.Success",
        "status",
        "Connection succeeded.",
        null,
        Map(
        "app" -> "live",
        "type" -> "nonprivate",
        "flashVer" -> "FMLE/3.0 (compatible; Lavf55.2.0)",
        "tcUrl" -> "rtmp://127.0.0.1:1935/live"
      )),
      Map(
      "app" -> "live",
      "type" -> "nonprivate",
      "flashVer" -> "FMLE/3.0 (compatible; Lavf55.2.0)",
      "tcUrl" -> "rtmp://127.0.0.1:1935/live"
      )
    ))))
  }

}
