package rtmp.amf.amf0

import akka.util.ByteIterator
import rtmp.amf.AmfObjectReader

/**
 * AMF0 Long string reader
 */
class LongStringReader extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Any = {

    val len = bufferItr.getInt
    val bytes = new Array[Byte](len)
    bufferItr.getBytes(bytes)

    new String(bytes)
  }
}
