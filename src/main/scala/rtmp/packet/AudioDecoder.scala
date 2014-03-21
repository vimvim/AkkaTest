package rtmp.packet

import akka.util.ByteString

import rtmp.amf.EncodingType

/**
 * AMF Audio packet decoder
 */
class AudioDecoder extends PacketDecoder {

  override def decode(encodingType: EncodingType, data: ByteString): Audio = new Audio(data)

}
