package rtmp.amf.amf0

import java.nio.ByteOrder
import akka.util.ByteStringBuilder
import rtmp.amf.AmfObjectWriter


/**
 * AMF0 integer writer
 */
class IntegerWriter extends AmfObjectWriter[Integer] {

  override def write(builder: ByteStringBuilder, value: Integer): Unit = {
    builder.putByte(Amf0Types.TYPE_NUMBER)
    builder.putDouble(value.doubleValue())(ByteOrder.BIG_ENDIAN)
  }
}
