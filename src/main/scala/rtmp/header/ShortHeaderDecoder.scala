package rtmp.header

import akka.util.ByteIterator

/**
 * Decode full header without message id ( 0x01 )
 * Fields to decode: stream id, time delta, length, type id
 */
class ShortHeaderDecoder extends ExtBasicHeaderDecoder {

  override def decode(firstByte:Int, bufferItr: ByteIterator): Header = {

    val sid = decodeSID(firstByte, bufferItr)
    val timeDelta = decodeTime(bufferItr)
    val size = decodeSize(bufferItr)
    val typeID = bufferItr.getByte

    if (timeDelta == 0xffffff) {
      val extendedTimeDelta = decodeExtendedTime(bufferItr)
      ShortHeader(sid, timeDelta, extendedTimeDelta, size, typeID)
    } else {
      ShortHeader(sid, timeDelta, 0, size, typeID)
    }
  }

  protected def decodeSize(bufferItr: ByteIterator):Int = {

    val b1 = bufferItr.getByte & 0xff
    val b2 = bufferItr.getByte & 0xff
    val b3 = bufferItr.getByte & 0xff

    (b1 << 16) + (b2 << 8) + b3
  }


}
