package rtmp.packet

import akka.util.ByteString

sealed trait EncodingType
case class AMF0Encoding() extends EncodingType
case class AMF3Encoding() extends EncodingType

/**
 * Base class for packet decoder
 */
abstract class PacketDecoder {

  def decode(encodingType:EncodingType, data:ByteString):Packet

}
