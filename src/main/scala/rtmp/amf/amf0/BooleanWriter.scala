package rtmp.amf.amf0

import akka.util.ByteStringBuilder

import rtmp.amf.AmfObjectWriter

/**
 * AMF0 boolean writer
 */
class BooleanWriter extends AmfObjectWriter[Boolean] {

  override def write(builder: ByteStringBuilder, value: Boolean): Unit = {
    builder.putByte(Amf0Types.TYPE_BOOLEAN)
    if (value) builder.putByte(1) else builder.putByte(0)
  }
}
