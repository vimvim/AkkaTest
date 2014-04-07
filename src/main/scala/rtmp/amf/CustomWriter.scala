package rtmp.amf

import akka.util.ByteStringBuilder

/**
 * Writer for CustomSerializable objects
 */
class CustomWriter(serializer:Serializer) extends AmfObjectWriter[CustomSerializable] {

  override def write(builder: ByteStringBuilder, value: CustomSerializable): Unit = value.serialize(serializer)
}
