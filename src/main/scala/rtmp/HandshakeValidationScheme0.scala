package rtmp

/**
 *
 */
class HandshakeValidationScheme0 extends HandshakeValidationScheme {

  /**
   * Returns the DH byte offset.
   *
   * @return dh offset
   */
  def getDHOffset(bytes: Array[Byte]): Int = {

    var offset: Int = (bytes(1532) & 0x0ff) + (bytes(1533) & 0x0ff) + (bytes(1534) & 0x0ff) + (bytes(1535) & 0x0ff)
    offset = offset % 632
    offset = offset + 772

    if (offset + Handshake.KEY_LENGTH >= 1536) {
      log.error("Invalid DH offset")
    }

    offset
  }

  /**
   * Returns a digest byte offset.
   *
   * @param pBuffer source for digest data
   * @return digest offset
   */
  protected def getDigestOffset(pBuffer: Array[Byte]): Int = {

    if (log.isTraceEnabled) {
      log.trace("Scheme 0 offset bytes {},{},{},{}", Array[Int](pBuffer(8) & 0x0ff, pBuffer(9) & 0x0ff, pBuffer(10) & 0x0ff, pBuffer(11) & 0x0ff))
    }

    var offset: Int = (pBuffer(8) & 0x0ff) + (pBuffer(9) & 0x0ff) + (pBuffer(10) & 0x0ff) + (pBuffer(11) & 0x0ff)
    offset = offset % 728
    offset = offset + 12

    if (offset + Handshake.DIGEST_LENGTH >= 1536) {
      log.error("Invalid digest offset")
    }

    offset
  }


}
