package rtmp.protocol.v2.handshake

import akka.event.LoggingAdapter

/**
 *
 */
class ValidationScheme1(implicit override val log:LoggingAdapter) extends ValidationScheme(1, log) {

  /**
   * Returns the DH byte offset.
   *
   * @return dh offset
   */
  def getDHOffset(bytes: Array[Byte]): Int = {

    var offset: Int = (bytes(768) & 0x0ff) + (bytes(769) & 0x0ff) + (bytes(770) & 0x0ff) + (bytes(771) & 0x0ff)
    offset = offset % 632
    offset = offset + 8

    if (offset + Constants.KEY_LENGTH >= 1536) {
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
  def getDigestOffset(pBuffer: Array[Byte]): Int = {

    if (log.isDebugEnabled) {
      log.debug("Scheme 1 offset bytes {},{},{},{}", Array[Int](pBuffer(772) & 0x0ff, pBuffer(773) & 0x0ff, pBuffer(774) & 0x0ff, pBuffer(775) & 0x0ff))
    }

    var offset: Int = (pBuffer(772) & 0x0ff) + (pBuffer(773) & 0x0ff) + (pBuffer(774) & 0x0ff) + (pBuffer(775) & 0x0ff)
    offset = offset % 728
    offset = offset + 776

    if (offset + Constants.DIGEST_LENGTH >= 1536) {
      log.error("Invalid digest offset")
    }

    offset
  }


}
