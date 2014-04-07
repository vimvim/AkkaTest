package rtmp.amf.amf0

import java.nio.ByteOrder
import akka.util.ByteStringBuilder
import rtmp.amf.AmfObjectWriter


/**
 * AMF0 Double writer
 */
class DoubleWriter extends AmfObjectWriter[Double] {

  override def write(builder: ByteStringBuilder, value: Double): Unit = {
    builder.putByte(Amf0Types.TYPE_NUMBER)
    builder.putDouble(value)(ByteOrder.BIG_ENDIAN)
  }
}
