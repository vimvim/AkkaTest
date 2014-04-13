
import java.io.{FileInputStream, FileNotFoundException, File}

import rtmp.header.FullHeader
import rtmp.status.NcConnectSuccess
import scala.collection.immutable.{ListSet, Iterable}
import scala.concurrent.duration._

import akka.util.{ByteString, CompactByteString}
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }

import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.concurrent._
import org.scalatest._
import org.scalatest.matchers.{ClassicMatchers, Matchers, ShouldMatchers}

import rtmp.amf.{AmfMixedMap, AmfNull, AMF0Encoding}
import rtmp.packet.{InvokeResponse, Invoke, InvokeDecoder}
import rtmp.amf.amf0.{Amf0Serializer, Amf0Deserializer}

/**
 * Test compose of the RTMP packets
 */
class ComposePacketSpec extends FlatSpec with ClassicMatchers with BinaryTester {

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
    val packet = new InvokeResponse(new Invoke("connect", 1, null),
      new NcConnectSuccess(
        "RED5/1,0,2,0",
        31,
        1,
        new AmfMixedMap(Map[String, AnyRef](
          "type" -> "red5",
          "version" -> "4,0,0,1121"
        ))
      )
    )

    val packetBuilder = ByteString.newBuilder

    val serializer = new Amf0Serializer(packetBuilder)
    packet.serialize(serializer)
    val serializedPacket = packetBuilder.result()

    // Encode HEADER_NEW
    // CHUNK, E, Header [channelId=3, dataType=20, timerBase=0, timerDelta=0, size=225, streamId=0, extendedTimestamp=0],
    val header = new FullHeader(3, 0, 0, serializedPacket.length, 20, 0)

    val chunkBuilder = ByteString.newBuilder



    assert(binaryData.equals(serializedPacket))

    /*
    val binaryData = readData("out_5.rtmp")
    val encoder = new InvokeEncoder(new DummyLogger())
    val packet = encoder.encode(new AMF0Encoding(), binaryData)

    // TODO: Note StatusObject is ICustomSerializable

    // Structure of the params in the NcConnectSuccess ( AMF.TYPE_OBJECT )
    // code
    // level
    // description
    // data ( AMF_MIXED_ARRAY )
    //    type -> red5
    //    version -> 4,0,0,1121
    // capabilities=31
    // fmsVer=RED5/1,0,2,0
    // mode=1


    assert(packet.equals(Invoke("_result", 1, List(
      null,
      new NcConnectSuccess(
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
    */
  }

}
