package rtmp.amf.amf0

import rtmp.amf.AmfObjectWriter
import akka.util.ByteStringBuilder

/**
 * AMF0 object writer
 */
class ObjectWriter extends AmfObjectWriter[AnyRef] {

  override def write(builder: ByteStringBuilder, obj: AnyRef) = {

  }
}
