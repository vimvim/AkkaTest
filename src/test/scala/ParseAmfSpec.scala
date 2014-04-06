
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
 * Testing AMF parsing
 */
class ParseAmfSpec extends FlatSpec with ClassicMatchers with BinaryTester {

  "A parsed data in packet_invoke_connect_1.rtmp" should "match to test data" in {

    val binaryData = readData("packet_invoke_connect_1.rtmp")
    val decoder = new InvokeDecoder(new DummyLogger())
    val packet = decoder.decode(new AMF0Encoding(), binaryData)

    assert(packet.equals(Invoke("connect", 1, List(Map(
      "app" -> "live",
      "type" -> "nonprivate",
      "flashVer" -> "FMLE/3.0 (compatible; Lavf55.2.0)",
      "tcUrl" -> "rtmp://127.0.0.1:1935/live"
    )))))
  }

  "A parsed data in packet_invoke_releaseStream_2.rtmp" should "match to test data" in {

    val binaryData = readData("packet_invoke_releaseStream_2.rtmp")
    val decoder = new InvokeDecoder(new DummyLogger())
    val packet = decoder.decode(new AMF0Encoding(), binaryData)

    assert(packet.equals(Invoke("releaseStream", 2, List(
      AmfNull(),
      "mystream.sdp"
    ))))
  }

  "A parsed data in packet_invoke_FCPublish_3.rtmp" should "match to test data" in {

    val binaryData = readData("packet_invoke_FCPublish_3.rtmp")
    val decoder = new InvokeDecoder(new DummyLogger())
    val packet = decoder.decode(new AMF0Encoding(), binaryData)

    assert(packet.equals(Invoke("FCPublish", 3, List(
      AmfNull(),
      "mystream.sdp"
    ))))
  }

  "A parsed data in packet_invoke_createStream_4.rtmp" should "match to test data" in {

    val binaryData = readData("packet_invoke_createStream_4.rtmp")
    val decoder = new InvokeDecoder(new DummyLogger())
    val packet = decoder.decode(new AMF0Encoding(), binaryData)

    assert(packet.equals(Invoke("createStream", 4, List(
      AmfNull()
    ))))
  }

  "A parsed data in packet_invoke_publish_5.rtmp" should "match to test data" in {

    val binaryData = readData("packet_invoke_publish_5.rtmp")
    val decoder = new InvokeDecoder(new DummyLogger())
    val packet = decoder.decode(new AMF0Encoding(), binaryData)

    assert(packet.equals(Invoke("publish", 5, List(
      AmfNull(),
      "mystream.sdp",
      "live"
    ))))
  }
}
