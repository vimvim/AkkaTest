
import java.io.{FileInputStream, FileNotFoundException, File}

import rtmp.header.{BasicHeader, ShortHeader, Header, FullHeader}
import rtmp.status.{StreamPublishStart, NcConnectSuccess}
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
import rtmp.packet.{Packet, InvokeResponse, Invoke, InvokeDecoder}
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

  "A composed 'connect' invoke response packet " should "match to the test data out_5.rtmp" in {

    compare(serializePacket(
      (packetLength) =>
      new FullHeader(3, 0, 0, packetLength, 20, 0),
      new InvokeResponse(new Invoke("connect", 1, null), true,
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
    ), "out_5.rtmp")
  }

  "A composed 'releaseStream' invoke response packet " should "match to the test data out_6.rtmp" in {

    compare(serializePacket(
      (packetLength) =>
      new ShortHeader(3, 0, 0, packetLength, 20),
      new InvokeResponse(new Invoke("releaseStream", 2, null), true, null)
    ), "out_6.rtmp")
  }

  "A composed 'FCPublish' invoke response packet " should "match to the test data out_7.rtmp" in {

    compare(serializePacket(
      (packetLength) =>
        new BasicHeader(3),
      new InvokeResponse(new Invoke("FCPublish", 3, null), true, null)
    ), "out_7.rtmp")
  }

  "A composed 'createStream' invoke response packet " should "match to the test data out_8.rtmp" in {

    compare(serializePacket(
      (packetLength) =>
        new ShortHeader(3, 0, 0, packetLength, 20),
      new InvokeResponse(new Invoke("createStream", 4, null), true, 1)
    ), "out_8.rtmp")
  }

  "A composed 'onStatus' invoke packet " should "match to the test data out_9.rtmp" in {

    compare(serializePacket(
      (packetLength) =>
      new FullHeader(4, 0, 0, packetLength, 20, 0),
      new Invoke("onStatus", 0, List(new StreamPublishStart("mystream.sdp", 1)))
    ), "out_9.rtmp")
  }

  private def serializePacket(headerFactory:(Int)=>Header, packet:Packet):ByteString = {

    val packetBuilder = ByteString.newBuilder

    val serializer = new Amf0Serializer(packetBuilder)
    packet.serialize(serializer)
    val serializedPacket = packetBuilder.result()

    // Encode HEADER_NEW
    // CHUNK, E, Header [channelId=3, dataType=20, timerBase=0, timerDelta=0, size=225, streamId=0, extendedTimestamp=0],
    // val header = new FullHeader(3, 0, 0, serializedPacket.length, 20, 0)

    val header = headerFactory(serializedPacket.length)

    val chunkBuilder = ByteString.newBuilder
    header.serialize(chunkBuilder)
    chunkBuilder.append(serializedPacket)

    chunkBuilder.result()
  }

}
