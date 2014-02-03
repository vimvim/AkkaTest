package rtmp.v2

import java.security.KeyPair

import akka.util.ByteString

import rtmp.protocol.{Response, BaseProtocol}
import rtmp.protocol.v2.handshake.{Constants, Crypto, Request}
import javax.crypto.interfaces.DHPublicKey
import java.math.BigInteger
import akka.event.LoggingAdapter


/**
 * Implements RTMP protocol version 0x03 ( with digested handshake )
 *
 *
 * TODO: Do we needs to create separated Actor from this ????
 */
class Protocol(keys: KeyPair, randBytes1:Array[Byte], randBytes2:Array[Byte])(implicit val log:LoggingAdapter) extends BaseProtocol {

  // Get public key
  val publicKey = getPublicKey(keys)

  def handshake(input:Array[Byte]):Response = {

    val request = new Request(input)

    new rtmp.protocol.v2.handshake.Response(3, request.validationScheme, publicKey, request.key, randBytes1, randBytes2)
  }

  /**
   * Returns the public key for a given key pair.
   *
   * @param keyPair
   * @return public key
   */
  def getPublicKey(keyPair: KeyPair): Array[Byte] = {

    val incomingPublicKey: DHPublicKey = keyPair.getPublic.asInstanceOf[DHPublicKey]
    val dhY: BigInteger = incomingPublicKey.getY

    log.debug("Public key: {}", dhY)

    var result: Array[Byte] = dhY.toByteArray
    log.debug("Public key as bytes - length [{}]: {}", result.length, Hex.encodeHexString(result))

    val temp: Array[Byte] = new Array[Byte](Constants.KEY_LENGTH)
    if (result.length < Constants.KEY_LENGTH) {

      System.arraycopy(result, 0, temp, Constants.KEY_LENGTH - result.length, result.length)
      result = temp

      log.debug("Padded public key length to 128")
    } else if (result.length > Constants.KEY_LENGTH) {

      System.arraycopy(result, result.length - Constants.KEY_LENGTH, temp, 0, Constants.KEY_LENGTH)
      result = temp

      log.debug("Truncated public key length to 128")
    }

    result
  }

}
