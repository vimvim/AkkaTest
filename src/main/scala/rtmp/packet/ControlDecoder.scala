package rtmp.packet

import java.nio.ByteOrder
import akka.util.ByteString

import rtmp.amf.EncodingType


/**
 * Decode control packets
 */
class ControlDecoder extends PacketDecoder {

  override def decode(encodingType: EncodingType, data: ByteString): ControlPacket = {

    val dataIterator = data.iterator

    val ctrlType = dataIterator.getShort(ByteOrder.BIG_ENDIAN)
    ctrlType match {
      case ControlTypes.STREAM_BEGIN => StreamBegin()
      case ControlTypes.PING_CLIENT => ClientPing(dataIterator.getInt(ByteOrder.BIG_ENDIAN))
    }
  }
}
