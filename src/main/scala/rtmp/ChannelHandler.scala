package rtmp

import akka.actor.{ActorLogging, Actor}
import akka.util.{CompactByteString, ByteString}

import rtmp.header._
import rtmp.header.FullHeader
import rtmp.header.ExtendedBasicHeader
import rtmp.header.ShortHeader
import rtmp.ChunkReceived
import rtmp.protocol.Constants

case class ChunkReceived(header:Header, data:ByteString)

/**
 * Actor will handle data chunks for single RTMP communication channel
 */
class ChannelHandler(val streamID:Int) extends Actor with ActorLogging {

  var timestamp:Int = 0
  var timeDelta:Int = 0
  var extendedTime:Int = 0
  var length:Int = 0
  var typeID:Byte = 0
  var messageSID:Int = 0

  var readRemaining:Int = 0
  var packetData:ByteString = CompactByteString()

  def receive: Actor.Receive = {

    case ChunkReceived(header, data) => processChunk(header, data)

  }

  def processChunk(header:Header, data:ByteString) = {

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



    }
  }

  def decodePacket() = {

    // TODO: For testing at this moment we only need to support INVOKE, NOTIFY, VIDEO DATA decoders
    // TODO: Resulting packet needs to be sent to the ClientHandler ( actor which is coordinate all actors for client )


    /*
    typeID match {
      case Constants.TYPE_INVOKE =>
        if (log.isDebugEnabled) log.debug("Packet type: Invoke");
        // message = decodeInvoke(conn.getEncoding(), in);

      case Constants.TYPE_NOTIFY =>
        if (log.isDebugEnabled()) log.debug("Packet type: Notify");
      if (header.getStreamId() == 0) {
        message = decodeNotify(conn.getEncoding(), in, header);
      } else {
        message = decodeStreamMetadata(in);
      }
      break;
      case TYPE_AUDIO_DATA:
      if (log.isTraceEnabled()) log.trace("Packet type: Audio data");
      message = decodeAudioData(in);
      message.setSourceType(Constants.SOURCE_TYPE_LIVE);
      break;
      case TYPE_VIDEO_DATA:
      if (log.isTraceEnabled()) log.trace("Packet type: Video data");
      message = decodeVideoData(in);
      message.setSourceType(Constants.SOURCE_TYPE_LIVE);
      break;
      case TYPE_AGGREGATE:
      if (log.isTraceEnabled()) log.trace("Packet type: Aggregate");
      message = decodeAggregate(in);
      break;
      case TYPE_FLEX_SHARED_OBJECT: // represents an SO in an AMF3 container
      if (log.isTraceEnabled()) log.trace("Packet type: Flex shared object");
      message = decodeFlexSharedObject(in);
      break;
      case TYPE_SHARED_OBJECT:
      if (log.isTraceEnabled()) log.trace("Packet type: Shared object");
      message = decodeSharedObject(in);
      break;
      case TYPE_FLEX_MESSAGE:
      if (log.isTraceEnabled()) log.trace("Packet type: Flex message");
      message = decodeFlexMessage(in);
      break;
      case TYPE_FLEX_STREAM_SEND:
      if (log.isTraceEnabled()) log.trace("Packet type: Flex stream send");
      message = decodeFlexStreamSend(in);
      break;
      case TYPE_PING:
      if (log.isTraceEnabled()) log.trace("Packet type: Ping");
      message = decodePing(in);
      break;
      case TYPE_BYTES_READ:
      if (log.isTraceEnabled()) log.trace("Packet type: Bytes read");
      message = decodeBytesRead(in);
      break;
      case TYPE_CHUNK_SIZE:
      if (log.isTraceEnabled()) log.trace("Packet type: Chunk size");
      message = decodeChunkSize(in);
      break;
      case TYPE_SERVER_BANDWIDTH:
      if (log.isTraceEnabled()) log.trace("Packet type: Server bandwidth");
      message = decodeServerBW(in);
      break;
      case TYPE_CLIENT_BANDWIDTH:
      if (log.isTraceEnabled()) log.trace("Packet type: Client bandwidth");
      message = decodeClientBW(in);
      break;
      case TYPE_ABORT:
      if (log.isTraceEnabled()) log.trace("Packet type: Abort");
      message = decodeAbort(in);
      break;
      default:
        log.warn("Unknown object type: {}", dataType);
      message = decodeUnknown(dataType, in);
      break;
    }
    return message;
    */

  }

}
