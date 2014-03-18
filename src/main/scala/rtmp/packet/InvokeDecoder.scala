package rtmp.packet

import akka.util.ByteString
import rtmp.amf.{Amf0Deserializer, Amf3Deserializer, Deserializer, AMF}

/**
 * Decoder for Invoke packets
 */
class InvokeDecoder extends PacketDecoder {

  def decode(encodingType: EncodingType, data: ByteString): Packet = {

    if (encodingType.isInstanceOf[AMF3Encoding]) {

      val deserializer = new Amf3Deserializer(data.iterator)

      // for response, the action string and invokeId is always encoded as AMF0 we use the first byte to decide which encoding to use.
      if (data.head!=AMF.TYPE_AMF3_OBJECT) {
        deserializer.forceAmf0 = true
      }

      decodePacket(deserializer)

    } else {

      val deserializer = new Amf0Deserializer(data.iterator)
      decodePacket(deserializer)
    }
  }

  def decodePacket(deserializer:Deserializer): Packet = {

    val action = deserializer.readString
    val invokeId = deserializer.readInteger

    deserializer match {
      case amf3Deserializer: Amf3Deserializer => amf3Deserializer.forceAmf0 = false
      case _ =>
    }

    val parameters = deserializer.readAll

    Invoke(action, invokeId, parameters)
  }

}
