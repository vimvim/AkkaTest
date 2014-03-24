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

  override def decode(firstByte:Int, bufferItr: ByteIterator): Header = {

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

    val b1 = bufferItr.getByte & 0xff
    val b2 = bufferItr.getByte & 0xff
    val b3 = bufferItr.getByte & 0xff
    val b4 = bufferItr.getByte & 0xff

    (b4 << 24) + (b3 << 16) + (b2 << 8) + b1
  }

}
