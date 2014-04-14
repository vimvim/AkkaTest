package rtmp.amf.amf0

import akka.util.ByteStringBuilder

import rtmp.amf.AmfObjectWriter


/**
 * AMF0 null value serializer
 */
class NullWriter extends AmfObjectWriter[Null] {

  override def write(builder: ByteStringBuilder, obj: Null): Unit = builder.putByte(Amf0Types.TYPE_NULL)

}
