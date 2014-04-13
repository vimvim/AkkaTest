package rtmp.amf.amf0

import rtmp.amf.{AmfObjectReader, AmfMixedArray, AmfObjectWriter}
import akka.util.{ByteIterator, ByteStringBuilder}

/**
 * Created by vim on 4/10/14.
 */
class MixedArrayReader extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Any = {

  }
}
