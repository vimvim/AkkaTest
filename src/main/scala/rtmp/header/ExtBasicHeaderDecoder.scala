package rtmp.header

import akka.util.ByteIterator
import java.nio.ByteOrder

/**
 * Decode basic header with timestamp ( 0x02 )
 * Fields to decode: stream id, time delta
 */
class ExtBasicHeaderDecoder extends HeaderDecoder {

  def decode(firstByte:Byte, bufferItr: ByteIterator): Header = {

    val sid = decodeSID(firstByte, bufferItr)
    val timeDelta = decodeTime(bufferItr)

    if (timeDelta == 0xffffff) {
      val extendedTimeDelta = decodeExtendedTime(bufferItr)
      ExtendedBasicHeader(sid, timeDelta, extendedTimeDelta)
    } else {
      ExtendedBasicHeader(sid, timeDelta, 0)
    }
  }

  protected def decodeTime(bufferItr: ByteIterator):Int = {

    val b1 = bufferItr.getByte
    val b2 = bufferItr.getByte
    val b3 = bufferItr.getByte

    (b1 << 16) + (b2 << 8) + b3
  }

  protected def decodeExtendedTime(bufferItr: ByteIterator):Int = {
    bufferItr.getInt(ByteOrder.BIG_ENDIAN) & Integer.MAX_VALUE
  }
}
