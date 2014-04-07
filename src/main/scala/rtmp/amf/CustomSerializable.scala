package rtmp.amf

/**
 * Allow to custom AMF serialization for objects
 */
trait CustomSerializable {

  def serialize(serializer:Serializer)
}
