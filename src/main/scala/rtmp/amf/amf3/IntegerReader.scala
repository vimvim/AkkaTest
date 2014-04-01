package rtmp.amf.amf3

import akka.util.ByteIterator

import rtmp.amf.AmfObjectReader

trait Amf3IntegerReader {

  def readAmf3Integer(bufferItr: ByteIterator):Int = {

    var n: Int = 0
    var b = bufferItr.getByte

    var result: Int = 0
    while ((b & 0x80) != 0 && n < 3) {
      result <<= 7
      result |= (b & 0x7f)
      b = bufferItr.getByte
      n += 1
    }

    if (n < 3) {
      result <<= 7
      result |= b
    } else {
      result <<= 8
      result |= b & 0x0ff
      if ((result & 0x10000000) != 0) {
        result |= 0xe0000000
      }
    }

    result
  }

}


/**
 * Parser of AMF3 "compressed" integer data type
 */
class IntegerReader extends AmfObjectReader with Amf3IntegerReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Int = readAmf3Integer(bufferItr)

}
