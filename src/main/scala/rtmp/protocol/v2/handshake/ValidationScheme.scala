package rtmp.protocol.v2.handshake

import akka.event.LoggingAdapter

/**
 *
 */
abstract class ValidationScheme(val id:Int, implicit val log:LoggingAdapter) {

  /**
   * Validate current scheme against passed data
   * 
   * 
   * @param input           Client's handshake data
   * @return
   */
  def validate(input: Array[Byte]): Boolean = {

    val digestOffset = getDigestOffset(input)

    val length = Constants.HANDSHAKE_SIZE - digestOffset - Constants.DIGEST_LENGTH

    log.debug("Validate scheme: {} digest offset: {} length:{}", id, digestOffset, length)

    val tempBuffer: Array[Byte] = new Array[Byte](Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)

    if (digestOffset>=tempBuffer.length) {
      false
    }
    System.arraycopy(input, 0, tempBuffer, 0, digestOffset)

    if (digestOffset>=tempBuffer.length) {
      false
    }
    System.arraycopy(input, digestOffset + Constants.DIGEST_LENGTH, tempBuffer, digestOffset, length)

    val tempHash: Array[Byte] = Crypto.calculateHMAC_SHA256(tempBuffer, Constants.GENUINE_FP_KEY, 30)

    // log.debug("Temp: {}", Hex.encodeHexString(tempHash))

    var i: Int = 0
    while (i < Constants.DIGEST_LENGTH) {

      if (input(digestOffset + i) != tempHash(i)) {
        return false
      }

      i = i+1
    }

    true
  }

  def getDHOffset(bytes: Array[Byte]): Int

  def getDigestOffset(input: Array[Byte]):Int


}
