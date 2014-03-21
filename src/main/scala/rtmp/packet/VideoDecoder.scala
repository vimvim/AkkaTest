package rtmp.packet

import rtmp.amf.EncodingType
import akka.util.ByteString

/**
 * AMF video packet decoder
 */
class VideoDecoder extends PacketDecoder {

  override def decode(encodingType: EncodingType, data: ByteString): Video = new Video(data)

}
