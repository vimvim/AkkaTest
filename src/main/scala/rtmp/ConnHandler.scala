package rtmp

import java.net.InetSocketAddress
import java.security.KeyPair
import java.util.Random
import java.nio.ByteOrder

import scala.concurrent.duration._
import scala.util.control
import scala.collection.immutable.HashMap

import akka.actor._
import akka.io.{ IO, Tcp }
import akka.util.{ByteStringBuilder, ByteIterator, CompactByteString, ByteString}
import akka.event.Logging

import rtmp.v2.Protocol
import rtmp.protocol.v2.handshake.{Constants, Crypto}
import rtmp.protocol.BaseProtocol
import rtmp.protocol.v2.handshake.Crypto
import rtmp.header._
import rtmp.ReceiveBodyData
import rtmp.Handshake


// ConnHandler messages
object ProcessBuffer

case class DataChunk(header:Header, data:ByteString)

// Market types for various types of the header
sealed trait HeaderType
case object BasicHeaderType extends HeaderType            // Basic header
case object ExtendedHeaderType extends HeaderType         // Basic header with timestamp
case object FullHeaderType extends HeaderType             // Full header
case object ShortHeaderType extends HeaderType            // Full header without message id

// States
sealed trait ConnState
case object HandshakeGet extends ConnState
case object HandshakeConfirm extends ConnState
case object ReceiveHeader extends ConnState
case object ReceiveBody extends ConnState
case object ClosedConn extends ConnState

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
class ConnHandler(connection: ActorRef, remote: InetSocketAddress) extends Actor with FSM[ConnState, Data] with ActorLogging  {

  type DataFunc = (ByteString) => Data

  import Tcp._

  val headerDecoders = HashMap[Int, HeaderDecoder](
    (0, new FullHeaderDecoder()),
    (1, new ShortHeaderDecoder()),
    (2, new ExtBasicHeaderDecoder()),
    (3, new BasicHeaderDecoder())
  )

  startWith(HandshakeGet, Handshake(CompactByteString("")))

  when(HandshakeGet) {
    case Event(Received(data), Handshake(buffer)) ⇒

      appendAndProcess(data, buffer, Constants.HANDSHAKE_SIZE, handshake, (buffer)=>{
        stay using Handshake(buffer)
      })
  }

  when(HandshakeConfirm) {
    case Event(Received(data), Handshake) ⇒ stay()
  }

  when(ReceiveHeader) {

    case Event(ProcessBuffer, Handshake(buffer)) ⇒ processBuffer(buffer, 1, decodeHeader,
      (restBuffer)=> stay using Handshake(restBuffer)
    )

    case Event(Received(data), Handshake(buffer)) ⇒ appendAndProcess(data, buffer, 1, decodeHeader,
      (restBuffer)=> stay using Handshake(restBuffer)
    )
  }

  when(ReceiveBody) {
    case Event(Received(data), ReceiveBodyData(header, buffer)) ⇒

      // TODO: Needs to read remaining of the data for packet but no greater than chunk size

      goto(state) using data
  }

  when(ClosedConn) {
    case Event(Received(data), _) ⇒ stay()
  }

  whenUnhandled {
    case Event(PeerClosed, _) ⇒
      // context stop self
      goto(ClosedConn)
  }

  initialize()


  def handshake(bufferItr:ByteIterator):(ConnState, DataFunc) = {

    val input = Array.ofDim[Byte](Constants.HANDSHAKE_SIZE)
    bufferItr.getBytes(input)

    val handshakeType = input(0)
    val versionByte = input(4)

    if (log.isDebugEnabled) {

      log.debug("Player encryption byte: {}", handshakeType)
      log.debug("Player version byte: {}", versionByte & 0x0ff)

      // If the 5th byte is 0 then don't generate new-style handshake
      log.debug("Detecting flash player version {},{},{},{}", Array[Int](input(4) & 0x0ff, input(5) & 0x0ff, input(6) & 0x0ff, input(7) & 0x0ff))
    }

    val protocol = createProtocol(handshakeType)
    val response = protocol.handshake(input)

    connection ! Write(response.serialize())

    // goto(HandshakeConfirm) using Handshake(buffer)
    (HandshakeConfirm, (buffer)=>{
      Handshake(buffer)
    })
  }

  /**
   * Start decoding header
   *
   * @param bufferItr     Iterator over data in the incoming buffer
   * @return
   */
  def decodeHeader(bufferItr:ByteIterator):(ConnState, DataFunc) = {

    val firstByte = bufferItr.getByte

    val headerDecoder = getHeaderDecoder(firstByte)
    val header = headerDecoder.decode(firstByte, bufferItr)

    // goto(ReceiveBody) using ReceiveBodyData(bufferItr.toByteString, header)
    (ReceiveBody, (buffer)=>{
      ReceiveBodyData(buffer, header)
    })
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
  def appendAndProcess(data:ByteString, buffer:ByteString, chunkSize:Int, handlerFunc: (ByteIterator)=>(ConnState, DataFunc), stayFunc: (ByteString)=>State):State = {
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
  def processBuffer(buffer:ByteString, chunkSize:Int, handlerFunc: (ByteIterator)=>(ConnState, DataFunc), stayFunc: (ByteString)=>State):State = {

    if (buffer.length>=chunkSize) {

      val bufferItr = buffer.iterator

      // TODO: Needs to catch overflow exception ( will be raised if no enough data in the buffer ) and call stayFunc
      val (state, dataFunc) = handlerFunc(bufferItr)

      val restBuffer = bufferItr.toByteString
      val data = dataFunc(restBuffer)

      // If there is another data stay left in the buffer
      // we needs to send self message, so ensure that this data
      // will be processed after actor is going to the next state
      if (restBuffer.length>0) self ! ProcessBuffer

      goto(state) using data

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

  def getHeaderDecoder(firstByte:Byte):HeaderDecoder = {

    val headerType = firstByte >> 6
    headerDecoders.get(headerType) match {
      case Some(decoder) => decoder
      case None => throw new Exception("Unknown header type "+headerType)
    }
  }

}
