package rtmp.amf.amf0

import java.nio.ByteOrder
import akka.util.ByteStringBuilder

import rtmp.amf.AmfObjectWriter


trait Amf0StringWriter {

  def writeString(builder:ByteStringBuilder, value:String) = {
    builder.putShort(value.length)(ByteOrder.BIG_ENDIAN)
    builder.putBytes(value.getBytes)
  }
}

/**
 * AMF0 string writer
 */
class StringWriter extends AmfObjectWriter[String] with Amf0StringWriter {

  override def write(builder: ByteStringBuilder, value: String) = {
    builder.putByte(Amf0Types.TYPE_STRING)
    writeString(builder, value)
  }
}
