package rtmp.amf.amf0

import akka.util.ByteStringBuilder

import rtmp.amf.{Serializer, AmfObjectWriter, AmfMixedArray}
import java.nio.ByteOrder

/**
 * Writer for AMF mixed array format
 */
class MixedArrayWriter(serializer:Serializer) extends AmfObjectWriter[AmfMixedArray] with Amf0StringWriter {

  override def write(builder: ByteStringBuilder, array: AmfMixedArray): Unit = {

    builder.putByte(Amf0Types.TYPE_MIXED_ARRAY)

    val maxKey = array.maxKey
    builder.putInt(maxKey+1)(ByteOrder.BIG_ENDIAN)

    array.iterateEntries((key:String, value:Any)=> {
      serializer.writeProperty(key, value)
    })

    if (maxKey>=0) {
      serializer.writeProperty("length", maxKey+1)
    }

    writeString(builder, "")
    serializer.writeEndObject()
  }
}
