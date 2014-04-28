package rtmp.protocol.v2.handshake

import akka.event.LoggingAdapter
import akka.util.ByteString
import utils.HexBytesUtil

/**
 *
 */
class Request(input:Array[Byte])(implicit val log:LoggingAdapter) {

  val validationScheme = createValidationScheme()
  log.debug(s"Valid RTMP client detected. Validation scheme: ${validationScheme.id}")

  val clientDHOffset: Int = validationScheme.getDHOffset(input)
  log.debug("Incoming DH offset: {}", clientDHOffset)

  // Get the clients public key
  val outgoingPublicKey = new Array[Byte](Constants.KEY_LENGTH)
  System.arraycopy(input, clientDHOffset, outgoingPublicKey, 0, Constants.KEY_LENGTH)

  val keyChallengeIndex: Int = validationScheme.getDigestOffset(input)
  val challengeKey: Array[Byte] = new Array[Byte](Constants.DIGEST_LENGTH)
  System.arraycopy(input, keyChallengeIndex, challengeKey, 0, Constants.DIGEST_LENGTH)

  // Used for creating hash on the random data
  val key = Crypto.calculateHMAC_SHA256(challengeKey, Constants.GENUINE_FMS_KEY, 68)
  log.debug("Key: {}", HexBytesUtil.bytes2hex(key))

  protected def createValidationScheme(): ValidationScheme = {

    val schema1 = new ValidationScheme1()
    if (schema1.validate(input)) {

      schema1
    } else {

      val schema0 = new ValidationScheme0()
      if (!schema0.validate(input)) {
        log.info("Invalid RTMP connection data detected, you may experience errors")
      }

      schema0
    }
  }

}
