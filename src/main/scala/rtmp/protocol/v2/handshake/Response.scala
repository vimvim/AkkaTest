package rtmp.protocol.v2.handshake

import akka.util.{ByteStringBuilder, ByteString}
import akka.event.LoggingAdapter
import utils.HexBytesUtil

/**
 * Implements handshake response
 */
class Response(handshakeType:Byte, validationScheme:ValidationScheme, publicKey:Array[Byte], key:Array[Byte], randBytes1:Array[Byte], randBytes2:Array[Byte])
              (implicit val log:LoggingAdapter) extends rtmp.protocol.Response {

  log.debug("Generating handshake response")

  require(randBytes1.length==Constants.HANDSHAKE_SIZE-8)
  require(randBytes2.length==Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)

  val handshakeBytes = new Array[Byte](Constants.HANDSHAKE_SIZE)

  handshakeBytes.update(0, 0)
  handshakeBytes.update(1, 0)
  handshakeBytes.update(2, 0)
  handshakeBytes.update(3, 0)

  handshakeBytes.update(4, 1)
  handshakeBytes.update(5, 2)
  handshakeBytes.update(6, 3)
  handshakeBytes.update(7, 4)

  System.arraycopy(randBytes1, 0, handshakeBytes, 8, randBytes1.length)

  // Get the server dh offset
  val serverDHOffset: Int = validationScheme.getDHOffset(handshakeBytes)
  log.debug("Outgoing DH offset: {}", serverDHOffset)

  // Create handshake response and add public key to it
  System.arraycopy(publicKey, 0, handshakeBytes, serverDHOffset, Constants.KEY_LENGTH)

  // Calculate the server digest ( hash ) and add to handshake data
  val serverDigestOffset: Int = validationScheme.getDigestOffset(handshakeBytes)
  log.debug("Server digest offset: {}", serverDigestOffset)

  val tempBuffer: Array[Byte] = new Array[Byte](Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)
  System.arraycopy(handshakeBytes, 0, tempBuffer, 0, serverDigestOffset)
  System.arraycopy(handshakeBytes, serverDigestOffset + Constants.DIGEST_LENGTH, tempBuffer, serverDigestOffset, Constants.HANDSHAKE_SIZE - serverDigestOffset - Constants.DIGEST_LENGTH)

  val serverHandshakeHash = Crypto.calculateHMAC_SHA256(tempBuffer, Constants.GENUINE_FMS_KEY, 36)
  System.arraycopy(serverHandshakeHash, 0, handshakeBytes, serverDigestOffset, Constants.DIGEST_LENGTH)
  log.debug("Server handshake hash: {}", HexBytesUtil.bytes2hex(serverHandshakeHash))

  val randBytesHash: Array[Byte] = Crypto.calculateHMAC_SHA256(randBytes2, key, Constants.DIGEST_LENGTH)
  log.debug("Rand bytes hash: {}", HexBytesUtil.bytes2hex(randBytesHash))

  def serialize():ByteString = {

    val outputBuilder = new ByteStringBuilder()
    outputBuilder.putByte(handshakeType)
    outputBuilder.putBytes(handshakeBytes)
    outputBuilder.putBytes(randBytes2)
    outputBuilder.putBytes(randBytesHash)

    outputBuilder.result()
  }
}
