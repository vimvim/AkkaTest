package rtmp.amf.amf3

import akka.util.ByteIterator

import rtmp.amf.AmfObjectReader

/**
 * AMF3 Boolean reader
 */
class BooleanReader extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Boolean = typeId==Amf3Types.TYPE_BOOLEAN_TRUE

}
