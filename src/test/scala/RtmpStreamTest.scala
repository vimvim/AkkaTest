
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
 * Common class for RTMP stream tests ( publish/play )
 */
abstract class RtmpStreamTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender
  with ShouldMatchers
  with FlatSpec
  with BeforeAndAfterAll
  with BinaryTester
  with HandshakeDataProvider {

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
