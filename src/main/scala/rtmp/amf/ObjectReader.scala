package rtmp.amf

import akka.util.ByteIterator

/**
 *
 */
abstract class ObjectReader {

  def read(typeId:Byte, bufferItr:ByteIterator):Any

}
