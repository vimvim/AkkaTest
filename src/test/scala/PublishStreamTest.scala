

import akka.io.Tcp.{Write, Received}
import java.net.InetSocketAddress
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{PrivateKey, PublicKey, KeyFactory, KeyPair}

import rtmp.amf.{AmfMixedMap, AmfNull}
import rtmp.packet.{Notify, Invoke, Packet}
import scala.concurrent.duration._
import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.concurrent._
import org.scalatest.matchers.ShouldMatchers

import com.typesafe.config.ConfigFactory

import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit._
import akka.event.Logging

import rtmp.{Message, HandshakeDataProvider, ConnHandler}

/**
 * Test parsing of the packets received by server during publish live stream from client
 */
class PublishStreamTest(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with ShouldMatchers
  with FlatSpec
  with BeforeAndAfterAll
  with BinaryTester
  with HandshakeDataProvider {

  def this() = this(ActorSystem("TestAkka", ConfigFactory.load))

  implicit val log = Logging.getLogger(system, this)
  log.info("RTMP Test started")

  // This is test probe for underlaid network connection
  val connProbe = TestProbe()

  // This is test probe for client connection controller
  val clientHandlerProbe = TestProbe()

  // Connection handler actor. Will be tested here.
  val connActor:TestActorRef[ConnHandler] = TestFSMRef(new ConnHandler(connProbe.ref, new InetSocketAddress(0), clientHandlerProbe.ref, this))

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "An ConnHandler" should "be able to correctly process handshake" in {

    // Check that ConnHandler send RegisterHandler message to the controller
    val msg = clientHandlerProbe.receiveOne(1000.millisecond)

    // Check handshake response
    testInputResponse("in_1.rtmp", "out_1.rtmp")

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
    testInputPackets("in_8.rtmp", List[Packet]())
    testInputPackets("in_9.rtmp", List[Packet]())
    testInputPackets("in_10.rtmp", List[Packet]())
    testInputPackets("in_11.rtmp", List[Packet]())
    testInputPackets("in_12.rtmp", List[Packet]())
    testInputPackets("in_13.rtmp", List[Packet]())

    // val msg = clientHandlerProbe.receiveOne(1000.millisecond)

    // TODO: Test state
    // connActor.underlyingActor

    // TODO: This function will be test that input packets is correctly parsed and passed to controller
    // testInputPackets()


    // TODO: Read in_1.rtmp packet and send to the ConnHandler actor
    // TODO: Get response and match to the out_1.rtmp

    // TODO: Needs to test only RTMP handshake in/out packets here.
    // val testData = new TestDataSource("dump")

    // val tester = new ConnTester(testData, system)
    // tester.testActor()
  }

  /**
   * Send test input to the conn handler actor and check response
   *
   * @param inDump
   * @param outDump
   * @return
   */
  def testInputResponse(inDump:String, outDump:String) = {

    connActor ! Received(readData(inDump))

    val msg = connProbe.receiveOne(1000.millisecond)
    msg match {
      case Write(outputValue, ack) => compare(outputValue, outDump)
      case _ => new Exception("Unexpected message type passed to the conn probe.")
    }
  }

  /**
   * Verify that connection controller test probe received specified packets
   *
   * @param packetsList       List of the packets expected to be received by controller actor
   */
  def verifyReceivedPackets(packetsList: List[Packet]) = {

    packetsList.foreach((packet)=>{
      val msg = clientHandlerProbe.receiveOne(1000.millisecond)
      msg match {
        case Message(streamID, timestamp, extendedTime, messageSID, msgPacket) =>
          if (!packet.equals(msgPacket)) {
            throw new Exception("Packets is not matched. Expected:"+packet+" Received:"+msgPacket)
          }

        case _ => new Exception("Unexpected type of the message received by controller probe")
      }
    })
  }

  /**
   * Send specified test data dump to the conn handler actor and check that it will be correctly parse it
   * and send specified packets to the controller
   *
   * @param dumpName      Name of the dump file
   * @param packets       List of the packets expected to be received by controller actor
   */
  def testInputPackets(dumpName:String, packets: List[Packet]) = {
    connActor ! Received(readData(dumpName))
    verifyReceivedPackets(packets)
  }

  /**
   * Load predefined rand1 bytes ( for ConnHandler )
   *
   * @return
   */
  override def getRand1: Array[Byte] = {
    readBytes("hrand1.rtmp")
  }

  /**
   * Load predefined rand2 bytes ( for ConnHandler )
   *
   * @return
   */
  override def getRand2: Array[Byte] = {
    readBytes("hrand2.rtmp")
  }

  /**
   * Load predefined keys ( for ConnHandler )
   *
   * @return
   */
  override def getKeyPair: KeyPair = {

    val encodedPublicKey = readBytes("public.key")
    val encodedPrivateKey = readBytes("private.key")

    val keyFactory: KeyFactory = KeyFactory.getInstance("DH")

    val publicKeySpec: X509EncodedKeySpec = new X509EncodedKeySpec(encodedPublicKey)
    val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)

    val privateKeySpec: PKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey)
    val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)

    new KeyPair(publicKey, privateKey)
  }
}
