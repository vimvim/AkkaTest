package rtmp

import java.net.InetSocketAddress
import java.security.KeyPair
import java.util.Random

import scala.concurrent.duration._

import akka.actor._
import akka.io.{ IO, Tcp }
import akka.util.{ByteIterator, CompactByteString, ByteString}
import akka.event.Logging

import rtmp.v2.Protocol
import rtmp.protocol.v2.handshake.{Constants, Crypto}
import rtmp.protocol.BaseProtocol
import rtmp.protocol.v2.handshake.Crypto
import java.nio.ByteOrder


object ProcessBuffer

sealed trait Header
case class BasicHeader(streamID:Int) extends Header
case class ExtendedBasicHeader(streamID:Int, timestamp:Int) extends Header
case class FullHeader(streamID:Int, timestamp:Int, length:Int, typeID:Byte, messageSID:Int) extends Header
case class ShortHeader(streamID:Int, timestamp:Int, length:Int, typeID:Byte) extends Header

case class DataChunk(header:Header, data:ByteString)

// Market types for various types of the header
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

      processBuffer(buffer, Constants.HANDSHAKE_SIZE, (buffer)=>{



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


    // case Event(ProcessBuffer, Handshake(buffer)) ⇒


    case Event(Received(data), Handshake(buffer)) ⇒ appendAndProcess(data, buffer, 1, decodeHeader,
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
   * @param bufferItr
   * @return
   */
  def decodeHeader(bufferItr:ByteIterator):State = {

    val firstByte = bufferItr.getByte
    val headerType = getHeaderType(firstByte)

    // TODO: Move decoding in the header classes. Create subclasses for each type of the header.
    headerType match {
      case BasicHeaderType => decodeHeaderSID(bufferItr, firstByte, headerType, decodeBasicHeader)
      // case
    }

  }

  /**
   * Decode stream id from header
   * For stream ID < 64 real value is stored in previous 6 bits and this field is not written;
   * for stream ID < 320 previous 6 bits are zero and this field is single byte,
   * otherwise previous 6 bits contain value "1" and this field is written as two bytes.
   *
   *
   * @param bufferItr
   * @param headerType
   * @param nextDecodeFunc
   * @return
   */
  def decodeHeaderSID(bufferItr:ByteIterator, firstByte:Byte, headerType:HeaderType,
                      nextDecodeFunc:(ByteIterator, HeaderType, Int) => State):State = {

    val sidFirstBits = firstByte & 0x3f

    if (sidFirstBits==0) {
      // Stream ID is one byte ( the next from first in the header )
      nextDecodeFunc(bufferItr, headerType, 64+bufferItr.getByte)
    } else if (sidFirstBits==1) {
      // Stream ID is two bytes ( next from first in the header )
      nextDecodeFunc(bufferItr, headerType, 64+bufferItr.getInt(ByteOrder.LITTLE_ENDIAN))
    } else {
      // Stream ID combined with the chunk type in the single byte
      nextDecodeFunc(bufferItr, headerType, sidFirstBits)
    }
  }

  /**
   * Decode full header ( 0x00 )
   * Fields to decode: timestamp, length, type id, message stream id
   *
   * TODO: For decoding details see RTMPProtocolDecoder:448
   *
   * @param bufferItr
   * @param headerType
   * @param streamID
   * @return
   */
  def decodeFullHeader(bufferItr:ByteIterator, headerType:HeaderType, streamID:Int):State = {


      val timestamp = reade

  }

  /**
   * Decode full header without message id ( 0x01 )
   * Fields to decode: time delta, length, type id
   *
   */
  def decodeShortHeader(bufferItr:ByteIterator, headerType:HeaderType, streamID:Int):State = {

  }

  /**
   * Decode basic header with timestamp ( 0x02 )
   * Fields to decode: time delta
   *
   */
  def decodeExtendedBasicHeader(bufferItr:ByteIterator, headerType:HeaderType, streamID:Int):State = {

  }

  /**
   * Decode basic header ( 0x03 )
   * Fields: none ( contain only stream id which is decoded early )
   *
   * @param bufferItr
   * @param headerType
   * @param streamID
   * @return
   */
  def decodeBasicHeader(bufferItr:ByteIterator, headerType:HeaderType, streamID:Int):State = {
    goto(ReceiveBody) using ReceiveBodyData(buffer, new BasicHeader(streamID))
  }

  /**
   * Append new incoming data into already accumulated buffer and try to parse
   *
   * @param data            New incoming data
   * @param buffer          Accumulated buffer
   * @param chunkSize       Minimal chunk size for processing
   * @param handlerFunc     Processing handler
   * @param stayFunc        Will be called if no enough data
   * @return
   */
  def appendAndProcess(data:ByteString, buffer:ByteString, chunkSize:Int, handlerFunc: (ByteString)=>State, stayFunc: (ByteString)=>State):State = {
    processBuffer(buffer.concat(data), chunkSize, handlerFunc, stayFunc)
  }

  /**
   * Process accumulated buffer
   *
   * @param buffer          Accumulated buffer
   * @param chunkSize       Minimal chunk size for processing
   * @param handlerFunc     Processing handler
   * @param stayFunc        Will be called if no enough data
   * @return
   */
  def processBuffer(buffer:ByteString, chunkSize:Int, handlerFunc: (ByteString)=>State, stayFunc: (ByteString)=>State):State = {

    if (buffer.length>=chunkSize) {

      // TODO: Needs to catch overflow exception ( will be raised if no enough data in the buffer ) and call stayFunc
      handlerFunc(buffer)

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
