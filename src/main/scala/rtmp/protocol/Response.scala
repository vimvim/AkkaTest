package rtmp.protocol

import akka.util.ByteString

/**
 *
 */
abstract class Response {
  def serialize():ByteString
}
