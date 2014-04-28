
import java.net.InetSocketAddress
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{PrivateKey, PublicKey, KeyFactory, KeyPair}

import scala.concurrent.duration._

import akka.io.Tcp.{Write, Received}
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit._
import akka.event.Logging

import rtmp.amf.{AmfMixedMap, AmfNull}
import rtmp.packet.{Notify, Invoke, Packet}
import rtmp.{Message, HandshakeDataProvider, ConnHandler}

import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.concurrent._
import org.scalatest.matchers.ShouldMatchers

import com.typesafe.config.ConfigFactory


/**
 * Test parsing of the packets received by server during publish live stream from client
 */
/*
class PublishStreamTest(_system: ActorSystem) extends RtmpStreamTest(_system: ActorSystem) {

  def this() = this(ActorSystem("PublishStreamTest", ConfigFactory.load))

  override protected def dumpDir:String = "dump/publish"

  "An ConnHandler" should "be able to correctly register with the controller" in {

    // Check that ConnHandler send RegisterHandler message to the controller
    val msg = clientHandlerProbe.receiveOne(1000.millisecond)

  }

  "An ConnHandler" should "be able to correctly process handshake" in {

    // Check handshake response
    testInputResponse("in_1.rtmp", "out_1.rtmp")
  }

  "An ConnHandler" should "be able to correctly parse client messages" in {

    // Dump in_2.rtmp contain client handshake response and invoke connect
    // packet splitted to the two chunks
    connActor ! Received(readData("in_2.rtmp"))

    verifyReceivedPackets(List[Packet](
      Invoke("connect", 1, List(
        Map(
          "app"  -> "live",
          "type" -> "nonprivate",
          "flashVer" -> "FMLE/3.0 (compatible; Lavf55.2.0)",
          "tcUrl" -> "rtmp://127.0.0.1:1935/live"
        )
      ))
    ))

    testInputPackets("in_3.rtmp", List[Packet]())
    testInputPackets("in_4.rtmp", List[Packet](
      Invoke("releaseStream", 2, List(AmfNull(), "mystream.sdp")),
      Invoke("FCPublish", 3, List(AmfNull(), "mystream.sdp")),
      Invoke("createStream", 4, List(AmfNull()))
    ))

    // Invoke publish ( mystream.sdp, live ) here !!
    // Invoke(publish, 5, List(AmfNull(), 'mystream.sdp', 'live'))
    testInputPackets("in_5.rtmp", List[Packet]())
    testInputPackets("in_6.rtmp", List[Packet](
      Invoke("publish", 5, List(AmfNull(), "mystream.sdp", "live"))
    ))

    // OnMetadata ( notify ) somewhere here and Video packets follow up
    testInputPackets("in_7.rtmp", List[Packet](
      Notify("@setDataFrame", List(
        "onMetaData",
        AmfMixedMap(Map(
          "duration" -> 0.0,
          "filesize" -> 0.0,
          "creation_time" -> "2013-06-21 21:02:32",
          "videocodecid" -> 2.0,
          "height" -> 270.0,
          "videodatarate" -> 195.3125,
          "compatible_brands" -> "qt  ",
          "encoder" -> "Lavf55.2.0",
          "minor_version" -> "537199360",
          "major_brand" -> "qt  ",
          "width" -> 480.0,
          "framerate" -> 2997
        ))
      ))
    ))

    // Video/Audio data here
    testInputPackets("in_8.rtmp", List[Packet]())
    testInputPackets("in_9.rtmp", List[Packet]())
    testInputPackets("in_10.rtmp", List[Packet]())
    testInputPackets("in_11.rtmp", List[Packet]())
    testInputPackets("in_12.rtmp", List[Packet]())
    testInputPackets("in_13.rtmp", List[Packet]())
  }
}
*/