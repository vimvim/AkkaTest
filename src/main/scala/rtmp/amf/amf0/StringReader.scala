package rtmp.amf.amf0

import akka.util.ByteIterator

import rtmp.amf.AmfObjectReader

trait Amf0StringReader {

  def readString(bufferItr: ByteIterator):String = {

    val len = bufferItr.getShort
    val bytes = new Array[Byte](len)
    bufferItr.getBytes(bytes)

    new String(bytes)
  }

}


/**
 * AMF0 String reader
 */
class StringReader extends AmfObjectReader with Amf0StringReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): String = readString(bufferItr)

}
