package rtmp.amf.amf3

import akka.util.ByteIterator

import rtmp.amf.ObjectReader

/**
 * AMF3 Boolean reader
 */
class BooleanReader extends ObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Boolean = typeId==Amf3Types.TYPE_BOOLEAN_TRUE

}
