package rtmp.amf

import akka.util.ByteStringBuilder
import rtmp.amf.amf0.{Amf0StringWriter, Amf0Types}

/**
 * Writer for CustomSerializable objects
 */
class CustomWriter(serializer:Serializer) extends AmfObjectWriter[CustomSerializable] with Amf0StringWriter {

  override def write(builder: ByteStringBuilder, value: CustomSerializable): Unit = {

    builder.putByte(Amf0Types.TYPE_OBJECT)

    value.serialize(serializer)

    writeString(builder, "")
    serializer.writeEndObject()
  }
}
