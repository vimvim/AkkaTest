package rtmp.amf.amf0

import rtmp.amf.{AmfObjectEnd, AmfObjectReader}
import akka.util.ByteIterator

/**
 * AMF object end market reader
 */
class EndObjectReader extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): AmfObjectEnd = new AmfObjectEnd

}
