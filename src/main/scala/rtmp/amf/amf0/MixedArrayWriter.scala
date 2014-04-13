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
    builder.putInt(maxKey)(ByteOrder.BIG_ENDIAN)

    array.iterateEntries((key:String, value:Any)=> {
      writeString(builder, key)
      serializer.writeObject(key)
    })

    if (maxKey>=0) {
      writeString(builder, "length")
      serializer.writeObject(maxKey+1)
    }

    writeString(builder, "")
    serializer.writeEndObject()
  }
}
