package rtmp.amf.amf0

import akka.util.ByteIterator
import rtmp.amf.AmfObjectReader
import java.nio.ByteOrder

/**
 * AMF0 Long string reader
 */
class LongStringReader extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Any = {

    val len = bufferItr.getInt(ByteOrder.BIG_ENDIAN)
    val bytes = new Array[Byte](len)
    bufferItr.getBytes(bytes)

    new String(bytes)
  }
}
