package rtmp.amf.amf0

import akka.util.ByteIterator
import rtmp.amf.{DeserializationContext, ObjectReader}

/**
 * AMF0 Double
 */
class DoubleReader extends ObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Double = bufferItr.getDouble

}
