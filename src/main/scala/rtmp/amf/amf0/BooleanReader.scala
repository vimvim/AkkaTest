package rtmp.amf.amf0

import akka.util.ByteIterator

import rtmp.amf.{DeserializationContext, ObjectReader}

/**
 * AMF0 Boolean reader
 */
class BooleanReader extends ObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Boolean = bufferItr.getByte==1

}
