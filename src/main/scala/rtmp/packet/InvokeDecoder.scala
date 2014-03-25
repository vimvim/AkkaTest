package rtmp.packet

import akka.util.ByteString

import rtmp.amf._
import rtmp.amf.amf3.{Amf3Types, Amf3Deserializer}
import rtmp.amf.amf0.{Amf0Types, Amf0Deserializer}
import akka.event.LoggingAdapter

/**
 * Decoder for Invoke packets
 */
class InvokeDecoder(val log:LoggingAdapter) extends PacketDecoder {

  def decode(encodingType: EncodingType, data: ByteString): Packet = {

    if (encodingType.isInstanceOf[AMF3Encoding]) {

      val deserializer = new Amf3Deserializer(data.iterator)

      // for response, the action string and invokeId is always encoded as AMF0 we use the first byte to decide which encoding to use.
      if (data.head!=Amf0Types.TYPE_AMF3_OBJECT) {
        deserializer.forceAmf0 = true
      }

      decodePacket(deserializer)

    } else {

      val deserializer = new Amf0Deserializer(data.iterator)
      decodePacket(deserializer)
    }
  }

  def decodePacket(deserializer:Deserializer): Packet = {

    val action = deserializer.readObject[String]
    val invokeId = deserializer.readObject[Int]

    log.debug("Invoke {} {}", action, invokeId)

    deserializer match {
      case amf3Deserializer: Amf3Deserializer => amf3Deserializer.forceAmf0 = false
      case _ =>
    }

    val parameters = deserializer.readAll

    log.debug("Invoke params {}", parameters)

    Invoke(action, invokeId, parameters)
  }

}
