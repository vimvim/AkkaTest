package rtmp.packet

import akka.util.ByteString
import rtmp.amf.EncodingType


/**
 * Base class for packet decoder
 */
abstract class PacketDecoder {

  def decode(encodingType:EncodingType, data:ByteString):Packet

}

