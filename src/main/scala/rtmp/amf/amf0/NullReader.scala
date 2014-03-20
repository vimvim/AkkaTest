package rtmp.amf.amf0

import akka.util.ByteIterator

import rtmp.amf.{AmfNull, DeserializationContext, ObjectReader}


/**
 * Reader for NULL AMF object
 */
class NullReader extends ObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): AmfNull = new AmfNull

}
