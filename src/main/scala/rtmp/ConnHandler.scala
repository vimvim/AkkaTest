package rtmp

import akka.actor._
import akka.io.{ IO, Tcp }
import scala.concurrent.duration._
import akka.util.{CompactByteString, ByteString}
import java.net.InetSocketAddress


import akka.event.Logging
import java.security.KeyPair
import rtmp.v2.Protocol
import rtmp.protocol.v2.handshake.{Constants, Crypto}
import rtmp.protocol.BaseProtocol
import java.util.Random
import rtmp.protocol.v2.handshake.Crypto



object ProcessBuffer

case class Header(streamID:Int)
case class BasicHeader(streamID:Int) extends Header(streamID)
case class ExtendedBasicHeader(timestamp:Int) extends Header
case class FullHeader(timestamp:Int, length:Int, typeID:Byte, messageSID:Int)
case class ShortHeader(timestamp:Int, length:Int, typeID:Byte)

case class DataChunk(header:Header, data:ByteString)

sealed trait HeaderType
case object BasicHeaderType extends HeaderType            // Basic header
case object ExtendedHeaderType extends HeaderType         // Basic header with timestamp
case object FullHeaderType extends HeaderType             // Full header
case object ShortHeaderType extends HeaderType            // Full header without message id

// States
sealed trait State
case object HandshakeGet extends State
case object HandshakeConfirm extends State
case object ReceiveHeader extends State
case object ReceiveBody extends State
case object Open extends State
case object ClosedConn extends State

// States data objects
sealed trait Data
case class Handshake(buffer:ByteString) extends Data
case class HandshakeData() extends Data
case class DecodeHeaderData(buffer:ByteString) extends Data
case class ReceiveBodyData(buffer:ByteString, header:Header) extends Data



/**
 *
 * TODO: Check how and where new byte buffers is created and optimize it.
 */
class ConnHandler(connection: ActorRef, remote: InetSocketAddress) extends Actor with FSM[State, Data] with ActorLogging  {

  import Tcp._

  startWith(HandshakeGet, Handshake(CompactByteString("")))

  when(HandshakeGet) {
    case Event(Received(data), Handshake(accumulatedBuffer)) ⇒

      val buffer = accumulatedBuffer.concat(data)

      processBuffer(buffer, Constants.HANDSHAKE_SIZE, (buffer, input)=>{

        val handshakeType = input(0)
        val versionByte = input(4)

        if (log.isDebugEnabled) {

          log.debug("Player encryption byte: {}", handshakeType)
          log.debug("Player version byte: {}", versionByte & 0x0ff)

          // If the 5th byte is 0 then dont generate new-style handshake
          log.debug("Detecting flash player version {},{},{},{}", Array[Int](input(4) & 0x0ff, input(5) & 0x0ff, input(6) & 0x0ff, input(7) & 0x0ff))
        }

        val protocol = createProtocol(handshakeType)
        val response = protocol.handshake(input)

        connection ! Write(response.serialize())

        // If there is another data stay left in the buffer
        // we needs to send self message, so ensure that this data
        // will be processed after actor is going to the next state
        if (buffer.length>0) self ! ProcessBuffer

        goto(HandshakeConfirm) using Handshake(buffer)

      }, (restBuffer)=>{
        stay using Handshake(restBuffer)
      })
  }

  when(HandshakeConfirm) {
    case Event(Received(data), Handshake) ⇒ stay()
  }

  when(ReceiveHeader) {


    case Event(ProcessBuffer, Handshake(accumulatedBuffer)) ⇒


    case Event(Received(data), Handshake(accumulatedBuffer)) ⇒ accumAndProcess(data, accumulatedBuffer, 1, processHeader,
      (restBuffer)=> stay using Handshake(restBuffer)
    )
/*
      val buffer = accumulatedBuffer.concat(data)

      processBuffer(buffer, 1, (buffer, input) => {

        val headerType = getHeaderType(buffer.head)



      }, (restBuffer)=>{
        stay using Handshake(restBuffer)
      })
*/
  }

  when(ReceiveBody) {
    case Event(Received(data), Handshake) ⇒ stay()
  }

  when(ClosedConn) {
    case Event(Received(data), Uninitialized) ⇒ stay()
  }

  whenUnhandled {
    case Event(PeerClosed, _) ⇒
      // context stop self
      goto(ClosedConn)
  }

  initialize()

  /**
   * Start decoding header
   *
   * @param buffer
   * @param input
   * @return
   */
  def decodeHeader(buffer:ByteString, input:ByteString):State = {

    val headerType = getHeaderType(input.head)

    headerType match {
      case BasicHeaderType => decodeHeaderSID(buffer, input, headerType, decodeBasicHeader)
      // case
    }

  }

  /**
   * Decode stream id from header
   *
   * @param buffer
   * @param input
   * @param headerType
   * @param nextDecodeFunc
   * @return
   */
  def decodeHeaderSID(buffer:ByteString, input:ByteString, headerType:HeaderType,
                      nextDecodeFunc:(ByteString, HeaderType, Int) => State):State = {

    val streamID = input.head & 3
    if (streamID==0) {

    } else if (streamID==1) {

    } else if (streamID==2) {

    } else {

    }
  }

  /**
   * Decode basic header ( contain only stream id )
   *
   * @param buffer
   * @param headerType
   * @param streamID
   * @return
   */
  def decodeBasicHeader(buffer:ByteString, headerType:HeaderType, streamID:Int):State = {
    goto(ReceiveBody) using ReceiveBodyData(buffer, new BasicHeader(streamID))
  }

  /**
   * Decode full header without message id
   */
  def decodeShortHeader(buffer:ByteString, input:ByteString, headerType:HeaderType, streamID:Int):State = {

  }

  /**
   * Decode basic header with timestamp
   *
   */
  def decodeExtendedBasicHeader(buffer:ByteString, input:ByteString, headerType:HeaderType, streamID:Int):State = {

  }

  /**
   * Decode full header ( contain both timestamp and message id )
   *
   * @param buffer
   * @param input
   * @param headerType
   * @param streamID
   * @return
   */
  def decodeFullHeader(buffer:ByteString, input:ByteString, headerType:HeaderType, streamID:Int):State = {

  }

  def processBuffer(buffer:ByteString, chunkSize:Int, handlerFunc: (ByteString, Array[Byte])=>State, stayFunc: (ByteString)=>State):State = {

    if (buffer.length>=chunkSize) {

      val split = buffer.splitAt(chunkSize)

      val compactData = split._2.compact
      val input = compactData.asByteBuffer.array()

      handlerFunc(split._1, input)

    } else {

      stayFunc(buffer)
    }
  }

  def createProtocol(handshakeVersion:Byte):BaseProtocol = {

    handshakeVersion match {

      // Implement protocol V1 and uncomment this
      // case 0 =>
      //   new rtmp.v1.Protocol()

      case 0x03 =>

        val random: Random = new Random

        val keyPair = Crypto.generateKeyPair

        val randBytes1 = new Array[Byte](Constants.HANDSHAKE_SIZE-8)
        random.nextBytes(randBytes1)

        val randBytes2: Array[Byte] = new Array[Byte](Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)
        random.nextBytes(randBytes2)

        implicit val log = this.log

        new rtmp.v2.Protocol(keyPair, randBytes1, randBytes2)

      case 0x06 => throw new Exception("Encrypted connections is not supported yet {}")
      case _ => throw new Exception(s"Unsupported handshake version specified $handshakeVersion ")
    }
  }

  def getHeaderType(firstByte:Byte):HeaderType = {

    val headerType = firstByte >> 6
    headerType match {
      case 0 => FullHeaderType
      case 1 => ShortHeaderType
      case 2 => ExtendedHeaderType
      case 3 => BasicHeaderType
    }
  }

  /*
  def receive = {
    case Received(data) ⇒ sender ! Write(data)
    case PeerClosed     ⇒ context stop self
  }
  */
}
