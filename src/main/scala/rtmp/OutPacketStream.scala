package rtmp

import akka.util.ByteString

import rtmp.header.{BasicHeader, ShortHeader, FullHeader, Header}
import rtmp.amf.amf0.Amf0Serializer

/**
 * Represent outgoing message ready for send
 *
 * @param typeID    Type of the outgoing message. Can be used for prioritize video/audio streams.
 * @param data      Serialized data ready for send
 */
case class OutgoingMessage(typeID:Byte, data:ByteString)

case class PacketInfo(timeDelta:Int, extendedTimeDelta:Int, messageSID:Int, packetLength:Int, typeID:Int)

/**
 * Handler for outgoing packets stream.
 */
class OutPacketStream(val streamID:Int, val chunkSize:Int = 128) {

  var lastPacketInfo:Option[PacketInfo] = None
  var lastPacketTime:Long = System.currentTimeMillis / 1000

  def stream(message:Message):List[OutgoingMessage] = {

    val serializedPacket = message.packet.serialize(new Amf0Serializer(ByteString.newBuilder)).result()
    val packetTime = System.currentTimeMillis / 1000

    lastPacketInfo match {

      case Some(lastInfo) =>

        lastPacketInfo = Some(PacketInfo(message.timestamp, message.extendedTime, message.messageSID, serializedPacket.length, message.packet.typeID))

        if (packetTime - lastPacketTime > 250) {
          // Full header

          encodePacket(List[OutgoingMessage](), serializedPacket, message.packet.typeID,
            (dataLength)=>FullHeader(streamID, message.timestamp, message.extendedTime, dataLength, message.packet.typeID, message.messageSID)
          )

        } else if ((lastInfo.typeID != message.packet.typeID) || (lastInfo.packetLength != serializedPacket.length)) {
          // Short header

          encodePacket(List[OutgoingMessage](), serializedPacket, message.packet.typeID,
            (dataLength)=>ShortHeader(streamID, message.timestamp, message.extendedTime, dataLength, message.packet.typeID)
          )

//        } else if () {
          // ExtendedBasicHeader ( with the timer delta )
          // TODO: When we needs to use this ??


        } else {
          // Basic header

          encodePacket(List[OutgoingMessage](), serializedPacket, message.packet.typeID,
            (dataLength)=>BasicHeader(streamID)
          )
        }

      case None =>

        encodePacket(List[OutgoingMessage](), serializedPacket, message.packet.typeID,
          (dataLength)=>FullHeader(streamID, message.timestamp, message.extendedTime, dataLength, message.packet.typeID, message.messageSID)
        )
    }
  }

  private def encodePacket(out:List[OutgoingMessage], data:ByteString, typeID:Byte, headerFactory:(Int)=>Header):List[OutgoingMessage] = {

    if (data.isEmpty) {
      lastPacketTime = System.currentTimeMillis / 1000
      out
    } else {

      val size = if (data.length>chunkSize) chunkSize else data.length
      val header = headerFactory(size)

      encodePacket(
        OutgoingMessage(
          typeID,
          header.serialize(ByteString.newBuilder).append(data.take(size)).result()
        ) :: out,
        data.drop(size), typeID, (length)=>BasicHeader(length))
    }
  }
}
