package rtmp

/**
 *
 */
abstract class HandshakeValidationScheme {

  /**
   * Validate current scheme against passed data
   * 
   * 
   * @param input           Client's handshake data
   * @return
   */
  def validate(input: Array[Byte]): Boolean = {

    val digestOffset = getDigestOffset(input)

    // log.debug("Scheme: {} client digest offset: {}", scheme, digestOffset)

    val tempBuffer: Array[Byte] = new Array[Byte](Handshake.HANDSHAKE_SIZE - Handshake.DIGEST_LENGTH)
    System.arraycopy(input, 0, tempBuffer, 0, digestOffset)
    System.arraycopy(input, digestOffset + Handshake.DIGEST_LENGTH, tempBuffer, digestOffset, Handshake.HANDSHAKE_SIZE - digestOffset - Handshake.DIGEST_LENGTH)

    val tempHash: Array[Byte] = Handshake.calculateHMAC_SHA256(tempBuffer, Handshake.GENUINE_FP_KEY, 30)

    // log.debug("Temp: {}", Hex.encodeHexString(tempHash))

    var i: Int = 0
    while (i < Handshake.DIGEST_LENGTH) {

      if (input(digestOffset + i) != tempHash(i)) {
        return false
      }

      i = i+1
    }

    true
  }

  abstract def getDHOffset(bytes: Array[Byte]): Int

  abstract def getDigestOffset(input: Array[Byte]):Int


}
