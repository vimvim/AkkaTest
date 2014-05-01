package rtmp.packet

import java.nio.ByteOrder
import akka.util.ByteString
import rtmp.amf.EncodingType

/**
 * Decode client bandwidth message
 */
class ClientBWDecoder extends PacketDecoder {

  override def decode(encodingType: EncodingType, data: ByteString): ClientBW = {

    val dataIterator = data.iterator

    ClientBW(dataIterator.getInt(ByteOrder.BIG_ENDIAN), dataIterator.getByte)
  }
}
