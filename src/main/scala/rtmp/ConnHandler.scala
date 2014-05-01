package rtmp

import java.net.InetSocketAddress
import java.security.KeyPair
import java.util.Random
import java.nio.{ByteBuffer, ByteOrder}

import scala.concurrent.duration._
import scala.util.control
import scala.collection.immutable.{Iterable, HashMap}
import scala.collection.mutable.{HashMap => MutableMap }
import scala.collection.generic.CanBuildFrom
import scala.collection.GenIterable

import akka.actor._
import akka.io.{ IO, Tcp }
import akka.util.{ByteStringBuilder, ByteIterator, CompactByteString, ByteString}
import akka.event.Logging

import rtmp.v2.Protocol
import rtmp.protocol.v2.handshake.{Constants, Crypto}
import rtmp.protocol.BaseProtocol
import rtmp.protocol.v2.handshake.Crypto
import rtmp.header._


// ConnHandler messages
object ProcessBuffer

// case class DataChunk(header:Header, data:ByteString)


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
case class ReceiveBodyData(header:Header, buffer:ByteString) extends Data


class ChannelInfo(val channelHandler:ActorRef, var packetSize:Int) {
  var readRemaining:Int = packetSize
}


/**
 * Handle incoming binary data received from connection
 *
 * TODO: Check how and where new byte buffers is created and optimize it.
 *
 * @param connection        Underlined connection
 * @param remote            Remote peer address info
 * @param messageHandler    Upstream message handler
 *
 */
class ConnHandler(connection: ActorRef, remote: InetSocketAddress, messageHandler:ActorRef, handshakeData:HandshakeDataProvider)
  extends Actor with FSM[ConnState, Data] with ActorLogging  {

  type DataFunc = (ByteString) => Data

  import Tcp._

  val headerDecoders = HashMap[Int, HeaderDecoder](
    (0, new FullHeaderDecoder()),
    (1, new ShortHeaderDecoder()),
    (2, new ExtBasicHeaderDecoder()),
    (3, new BasicHeaderDecoder())
  )

  val channels = new MutableMap[Int,ChannelInfo]()
  var chunkSize = 128

  var headerID = 1

  messageHandler ! RegisterHandler(this.self)

  startWith(HandshakeGet, Handshake(CompactByteString("")))

  when(HandshakeGet) {
    case Event(Received(data), Handshake(buffer)) ⇒

      appendAndProcess(data, buffer, Constants.HANDSHAKE_SIZE+1, handshake, (buffer)=>{
        stay using Handshake(buffer)
      })
  }

  when(HandshakeConfirm) {
    case Event(Received(data), Handshake(buffer)) ⇒

      appendAndProcess(data, buffer, Constants.HANDSHAKE_SIZE, handshakeResponse, (buffer)=>{
        stay using Handshake(buffer)
      })
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

    case Event(ProcessBuffer, ReceiveBodyData(header, buffer)) ⇒ processBody(buffer, header)

    case Event(Received(data), ReceiveBodyData(header, buffer)) ⇒ processBodyData(buffer, data, header)

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

  def processBodyData(buffer:ByteString, data:ByteString, header:Header):State = {
    processBody(buffer.concat(data), header)
  }

  def processBody(buffer:ByteString, header:Header):State = {

    getChannelInfo(header) match {

      case Some(channelInfo:ChannelInfo) =>

        val readSize = getReadSize(channelInfo)
        log.debug("Read body bytes: {}", readSize)

        processBuffer(buffer, readSize, (bufferItr)=>{

          if (readSize>0) {

            log.debug("Got body bytes: {}", readSize)

            val packetData = new Array[Byte](readSize)
            bufferItr.getBytes(packetData)

            channelInfo.readRemaining = channelInfo.readRemaining - readSize

            log.debug("Remaining body bytes: {}", channelInfo.readRemaining)

            if (channelInfo.readRemaining==0) {
              // All data received for packet so if channel info will not be updated ( using FullHeader )
              // we now expect to read another packet of the previously specified size
              channelInfo.readRemaining = channelInfo.packetSize

              log.debug("Reset remaining body size: {}", channelInfo.packetSize)
            }

            channelInfo.channelHandler ! ChunkReceived(header, CompactByteString(packetData))
          }

          (ReceiveHeader, (buffer)=>{
            Handshake(buffer)
          })
        },
        (buffer)=> stay using ReceiveBodyData(header, buffer)
        )

      case None =>
        gotoClose
    }
  }

  def gotoClose:State = {
    connection ! Close
    goto(ClosedConn)
  }

  def getReadSize(channelInfo:ChannelInfo):Int ={
    if (channelInfo.readRemaining > chunkSize) chunkSize else channelInfo.readRemaining
  }

  def getChannelInfo(header:Header):Option[ChannelInfo] = {

    header match {
      case FullHeader(streamID, timestamp, extendedTime, length, typeID, messageSID) =>

        channels.get(streamID) match {
          case Some(channelInfo) =>

            if (channelInfo.packetSize!=length) {
              channelInfo.packetSize = length
              channelInfo.readRemaining = length
            }

            Some(channelInfo)

          case None =>

            val channelHandler = context.actorOf(Props(classOf[ChannelHandler], streamID, messageHandler), name="channel_"+streamID)
            val channelInfo = new ChannelInfo(channelHandler, length)

            log.debug("Create new channel handler: {}", streamID)

            channels.put(streamID, channelInfo)

            Some(channelInfo)
        }

      case ShortHeader(streamID, timestamp, extendedTime, length, typeID) =>

        channels.get(streamID) match {
          case Some(channelInfo) =>

            if (channelInfo.packetSize!=length) {
              channelInfo.packetSize = length
              channelInfo.readRemaining = length
            }

            Some(channelInfo)

          case None =>

            log.error("ShortHeader received for not-existent channel: {}", streamID)
            throw new Exception("ShortHeader received for not-existent channel:"+streamID)
        }

      case _ =>

        channels.get(header.streamID) match {
          case Some(channelInfo) =>
            Some(channelInfo)

          case None => None
        }
    }
  }

  def handshake(bufferItr:ByteIterator):(ConnState, DataFunc) = {

    val handshakeType = bufferItr.getByte

    val input = Array.ofDim[Byte](Constants.HANDSHAKE_SIZE)
    bufferItr.getBytes(input)

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
   * Process handshake response received from client
   *
   * @param bufferItr
   * @return
   */
  def handshakeResponse(bufferItr:ByteIterator):(ConnState, DataFunc) = {

    log.debug("Got handshake response")

    bufferItr.drop(Constants.HANDSHAKE_SIZE)

    (ReceiveHeader, (buffer)=>{
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

    log.debug("Decode header: {}", headerID)

    val firstByte = bufferItr.getByte & 0xff

    val headerDecoder = getHeaderDecoder(firstByte)
    val header = headerDecoder.decode(firstByte, bufferItr)

    log.debug("Got header: {}", header)

    headerID = headerID + 1

    // goto(ReceiveBody) using ReceiveBodyData(bufferItr.toByteString, header)
    (ReceiveBody, (buffer)=>{
      ReceiveBodyData(header, buffer)
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

      log.debug("Move to the state: {}", state)

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

        /*
        val random: Random = new Random

        val keyPair = Crypto.generateKeyPair

        val randBytes1 = new Array[Byte](Constants.HANDSHAKE_SIZE-8)
        random.nextBytes(randBytes1)

        val randBytes2: Array[Byte] = new Array[Byte](Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)
        random.nextBytes(randBytes2)
        */

        val keyPair = handshakeData.getKeyPair
        val rand1 = handshakeData.getRand1
        val rand2 = handshakeData.getRand2

        implicit val log = this.log

        new rtmp.v2.Protocol(keyPair, rand1, rand2)

      case 0x06 => throw new Exception("Encrypted connections is not supported yet {}")
      case _ => throw new Exception(s"Unsupported handshake version specified $handshakeVersion ")
    }
  }

  def getHeaderDecoder(firstByte:Int):HeaderDecoder = {

    val headerType = firstByte >> 6
    headerDecoders.get(headerType) match {
      case Some(decoder) => decoder
      case None => throw new Exception("Unknown header type "+headerType)
    }
  }

}
