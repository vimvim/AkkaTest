package rtmp.amf

import akka.util.ByteIterator

/**
 *
 */
abstract class AmfObjectReader {

  def read(typeId:Byte, bufferItr:ByteIterator):Any

}
