package rtmp.protocol

import akka.util.ByteString

/**
 */
abstract class BaseProtocol {
  def handshake(input:Array[Byte]):Response
}
