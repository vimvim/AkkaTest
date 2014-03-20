package rtmp.header

import akka.util.ByteIterator

/**
 * Decode full header ( 0x00 )
 * Fields to decode: stream id, timestamp, length, type id, message stream id
 *
 * TODO: For decoding details see RTMPProtocolDecoder:448
 *
 */
class FullHeaderDecoder extends ShortHeaderDecoder {

  override def decode(firstByte:Byte, bufferItr: ByteIterator): Header = {

    val sid = decodeSID(firstByte, bufferItr)
    val timestamp = decodeTime(bufferItr)
    val size = decodeSize(bufferItr)
    val typeID = bufferItr.getByte
    val messageSID = decodeMessageSID(bufferItr)

    if (timestamp == 0xffffff) {
      val extendedTimeDelta = decodeExtendedTime(bufferItr)
      FullHeader(sid, timestamp, extendedTimeDelta, size, typeID, messageSID)
    } else {
      FullHeader(sid, timestamp, 0, size, typeID, messageSID)
    }
  }

  protected def decodeMessageSID(bufferItr: ByteIterator):Int = {

    val b1 = bufferItr.getByte
    val b2 = bufferItr.getByte
    val b3 = bufferItr.getByte
    val b4 = bufferItr.getByte

    (b4 << 24) + (b3 << 16) + (b2 << 8) + b1
  }

}