package rtmp.packet

import java.nio.ByteOrder
import akka.util.ByteString
import rtmp.amf.EncodingType

/**
 * Decode server bandwidth message
 */
class ServerBWDecoder extends PacketDecoder {

  override def decode(encodingType: EncodingType, data: ByteString): ServerBW = {

    val dataIterator = data.iterator

    ServerBW(dataIterator.getInt(ByteOrder.LITTLE_ENDIAN))
  }
}
