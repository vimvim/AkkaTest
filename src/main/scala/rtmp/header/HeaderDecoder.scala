package rtmp.header

import akka.util.ByteIterator
import java.nio.ByteOrder

/**
 *
 *
 */
abstract class HeaderDecoder {

  def decode(firstByte:Int, bufferItr: ByteIterator): Header

  /**
   * Decode stream id from header
   * For stream ID < 64 real value is stored in previous 6 bits and this field is not written;
   * for stream ID < 320 previous 6 bits are zero and this field is single byte,
   * otherwise previous 6 bits contain value "1" and this field is written as two bytes.
   *
   *
   * @param bufferItr
   * @return
   */
  protected def decodeSID(firstByte:Int, bufferItr:ByteIterator):Int = {

    val sidFirstBits = firstByte & 0x3f

    if (sidFirstBits==0) {
      // Stream ID is one byte ( the next from first in the header )
      64+bufferItr.getByte
    } else if (sidFirstBits==1) {
      // Stream ID is two bytes ( next from first in the header )
      64+bufferItr.getInt(ByteOrder.LITTLE_ENDIAN)
    } else {
      // Stream ID combined with the chunk type in the single byte
      sidFirstBits
    }
  }

}
