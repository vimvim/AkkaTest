package rtmp.header

import akka.util.ByteIterator

/**
 * Decode basic header ( 0x03 )
 * Fields: stream id
 */
class BasicHeaderDecoder extends HeaderDecoder {

  override def decode(firstByte: Byte, bufferItr: ByteIterator): Header = {

    val sid = decodeSID(firstByte, bufferItr)
    BasicHeader(sid)
  }
}
