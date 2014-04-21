package rtmp

import akka.util.ByteString

import rtmp.header.Header
import rtmp.amf.amf0.Amf0Serializer

/**
 * Represent outgoing message ready for send
 *
 * @param typeID    Type of the outgoing message. Can be used for prioritize video/audio streams.
 * @param data      Serialized data ready for send
 */
case class OutgoingMessage(typeID:Int, data:ByteString)

case class PacketInfo(timeDelta:Int, extendedTimeDelta:Int, messageSID:Int, packetLength:Int)

/**
 * Handler for outgoing packets stream.
 */
class PacketStream(streamID:Int) {

  var lastPacketInfo:Option[PacketInfo] = None

  def stream(message:Message):List[OutgoingMessage] = {

    val packetBuilder = ByteString.newBuilder

    val serializer = new Amf0Serializer(packetBuilder)
    message.packet.serialize(serializer)
    val serializedPacket = packetBuilder.result()

    //TODO: Split to chunks. Note: BasicHeader used only for splitted packets !!

    val packetInfo = PacketInfo(timeDelta:Int, extendedTimeDelta:Int)

    lastPacketInfo match {

      case Some(_) => _ match {
        case packetInfo =>
        case _ =>
      }

      case None =>
    }
  }
}
