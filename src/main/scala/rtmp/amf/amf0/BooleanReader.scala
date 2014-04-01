package rtmp.amf.amf0

import akka.util.ByteIterator

import rtmp.amf.AmfObjectReader

/**
 * AMF0 Boolean reader
 */
class BooleanReader extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Boolean = bufferItr.getByte==1

}
