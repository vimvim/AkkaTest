package rtmp.amf.amf0

import akka.util.ByteIterator
import rtmp.amf.{DeserializationContext, AmfObjectReader}

/**
 * AMF0 Double
 */
class DoubleReader extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Double = bufferItr.getDouble

}
