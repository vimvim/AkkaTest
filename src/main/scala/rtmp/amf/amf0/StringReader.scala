package rtmp.amf.amf0

import akka.util.ByteIterator

import rtmp.amf.ObjectReader

/**
 * AMF0 String reader
 */
class StringReader extends ObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): String = {

    val len = bufferItr.getShort
    val bytes = new Array[Byte](len)
    bufferItr.getBytes(bytes)

    new String(bytes)
  }

}
