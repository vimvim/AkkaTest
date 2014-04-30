package rtmp

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.util.{CompactByteString, ByteString}

import rtmp.header._
import rtmp.packet._
import rtmp.header.FullHeader
import rtmp.header.ShortHeader
import rtmp.header.BasicHeader
import rtmp.header.ExtendedBasicHeader
import rtmp.amf.{AMF0Encoding, AMF3Encoding}

case class ChunkReceived(header:Header, data:ByteString)

/**
 * Actor will handle data chunks for single RTMP communication channel
 *
 * @param streamID        Channel ID
 * @param messageHandler  Upstream message handler
 */
class ChannelHandler(val streamID:Int, val messageHandler:ActorRef) extends Actor with ActorLogging {

  val decoders = Map[Byte, PacketDecoder](
    PacketTypes.TYPE_INVOKE -> new InvokeDecoder(log),
    PacketTypes.TYPE_NOTIFY -> new NotifyDecoder(log),
    PacketTypes.TYPE_AUDIO_DATA -> new AudioDecoder(),
    PacketTypes.TYPE_VIDEO_DATA -> new VideoDecoder(),
    // PacketTypes.TYPE_CHUNK_SIZE -> new ChunkSizeDecoder(),
    PacketTypes.TYPE_CLIENT_BANDWIDTH -> new ClientBWDecoder(),
    PacketTypes.TYPE_SERVER_BANDWIDTH -> new ServerBWDecoder(),
    PacketTypes.TYPE_PING -> new ControlDecoder()
  )

  var timestamp:Int = 0
  var extendedTime:Int = 0
  var messageSID:Int = 0

  var typeID:Byte = 0
  var length:Int = 0
  var packetData:ByteString = CompactByteString()

  def receive: Actor.Receive = {

    case ChunkReceived(header, data) => processChunk(header, data)

  }

  def processChunk(header:Header, data:ByteString) = {

    log.debug("Got new packet chunk {} size {} of {}", header, data.length, length)

    header match {
      case FullHeader(hStreamID, hTimestamp, hExtendedTime, hLength, hTypeID, hMessageSID) =>
        timestamp = hTimestamp
        extendedTime = hExtendedTime
        length = hLength
        typeID = hTypeID
        messageSID = hMessageSID


      case ShortHeader(hStreamID, hTimeDelta, hExtendedTimeDelta, hLength, hTypeID) =>
        // timeDelta = hTimeDelta
        // extendedTime = hExtendedTime
        length = hLength
        typeID = hTypeID

      case ExtendedBasicHeader(hStreamID, hTimeDelta, hExtendedTimeDelta) =>
      // timeDelta = hTimeDelta
      // extendedTime = hExtendedTime

      case BasicHeader(hStreamID) =>
    }

    packetData = packetData.concat(data)
    if (packetData.length==length) {

      log.debug("Full packet data received. Size:{}", length)

      decoders.get(typeID) match {
        case Some(decoder) =>

          val packet = decoder.decode(new AMF0Encoding(), packetData)
          log.debug("Packet decoded: "+packet)

          messageHandler ! new Message(streamID, timestamp, extendedTime, messageSID, packet)

        case None => log.debug("Packet decoder is not found. Type: {} ", typeID)
      }

      packetData = CompactByteString()
    }
  }
}
