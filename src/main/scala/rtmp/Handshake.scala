package rtmp

import javax.crypto.spec.{DHPublicKeySpec, DHParameterSpec, SecretKeySpec}
import java.security.{PublicKey, KeyFactory, KeyPairGenerator, KeyPair}
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPublicKey
import java.math.BigInteger
import java.security.spec.KeySpec

/**
 *
 */
object Handshake {
  /**
   * Size of initial handshake between client and server
   */
  final val HANDSHAKE_SIZE: Int = 1536
  final val DIGEST_LENGTH: Int = 32
  final val KEY_LENGTH: Int = 128

  protected final val GENUINE_FMS_KEY: Array[Byte] = Array(0x47.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x6e.asInstanceOf[Byte], 0x75.asInstanceOf[Byte], 0x69.asInstanceOf[Byte], 0x6e.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x41.asInstanceOf[Byte], 0x64.asInstanceOf[Byte], 0x6f.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x46.asInstanceOf[Byte], 0x6c.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x73.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x4d.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x64.asInstanceOf[Byte], 0x69.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x53.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x72.asInstanceOf[Byte], 0x76.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x72.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xf0.asInstanceOf[Byte], 0xee.asInstanceOf[Byte], 0xc2.asInstanceOf[Byte], 0x4a.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0xbe.asInstanceOf[Byte], 0xe8.asInstanceOf[Byte], 0x2e.asInstanceOf[Byte], 0x00.asInstanceOf[Byte], 0xd0.asInstanceOf[Byte], 0xd1.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x9e.asInstanceOf[Byte], 0x7e.asInstanceOf[Byte], 0x57.asInstanceOf[Byte], 0x6e.asInstanceOf[Byte], 0xec.asInstanceOf[Byte], 0x5d.asInstanceOf[Byte], 0x2d.asInstanceOf[Byte], 0x29.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x6f.asInstanceOf[Byte], 0xab.asInstanceOf[Byte], 0x93.asInstanceOf[Byte], 0xb8.asInstanceOf[Byte], 0xe6.asInstanceOf[Byte], 0x36.asInstanceOf[Byte], 0xcf.asInstanceOf[Byte], 0xeb.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xae.asInstanceOf[Byte])

  protected final val GENUINE_FP_KEY: Array[Byte] = Array(0x47.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x6E.asInstanceOf[Byte], 0x75.asInstanceOf[Byte], 0x69.asInstanceOf[Byte], 0x6E.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x41.asInstanceOf[Byte], 0x64.asInstanceOf[Byte], 0x6F.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x46.asInstanceOf[Byte], 0x6C.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x73.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x50.asInstanceOf[Byte], 0x6C.asInstanceOf[Byte], 0x61.asInstanceOf[Byte], 0x79.asInstanceOf[Byte], 0x65.asInstanceOf[Byte], 0x72.asInstanceOf[Byte], 0x20.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xF0.asInstanceOf[Byte], 0xEE.asInstanceOf[Byte], 0xC2.asInstanceOf[Byte], 0x4A.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0xBE.asInstanceOf[Byte], 0xE8.asInstanceOf[Byte], 0x2E.asInstanceOf[Byte], 0x00.asInstanceOf[Byte], 0xD0.asInstanceOf[Byte], 0xD1.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x9E.asInstanceOf[Byte], 0x7E.asInstanceOf[Byte], 0x57.asInstanceOf[Byte], 0x6E.asInstanceOf[Byte], 0xEC.asInstanceOf[Byte], 0x5D.asInstanceOf[Byte], 0x2D.asInstanceOf[Byte], 0x29.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0x6F.asInstanceOf[Byte], 0xAB.asInstanceOf[Byte], 0x93.asInstanceOf[Byte], 0xB8.asInstanceOf[Byte], 0xE6.asInstanceOf[Byte], 0x36.asInstanceOf[Byte], 0xCF.asInstanceOf[Byte], 0xEB.asInstanceOf[Byte], 0x31.asInstanceOf[Byte], 0xAE.asInstanceOf[Byte])

  /** Modulus bytes from flazr */
  protected final val DH_MODULUS_BYTES: Array[Byte] = Array(0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xc9.asInstanceOf[Byte], 0x0f.asInstanceOf[Byte], 0xda.asInstanceOf[Byte], 0xa2.asInstanceOf[Byte], 0x21.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0xc2.asInstanceOf[Byte], 0x34.asInstanceOf[Byte], 0xc4.asInstanceOf[Byte], 0xc6.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x8b.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0xdc.asInstanceOf[Byte], 0x1c.asInstanceOf[Byte], 0xd1.asInstanceOf[Byte], 0x29.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x4e.asInstanceOf[Byte], 0x08.asInstanceOf[Byte], 0x8a.asInstanceOf[Byte], 0x67.asInstanceOf[Byte], 0xcc.asInstanceOf[Byte], 0x74.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x0b.asInstanceOf[Byte], 0xbe.asInstanceOf[Byte], 0xa6.asInstanceOf[Byte], 0x3b.asInstanceOf[Byte], 0x13.asInstanceOf[Byte], 0x9b.asInstanceOf[Byte], 0x22.asInstanceOf[Byte], 0x51.asInstanceOf[Byte], 0x4a.asInstanceOf[Byte], 0x08.asInstanceOf[Byte], 0x79.asInstanceOf[Byte], 0x8e.asInstanceOf[Byte], 0x34.asInstanceOf[Byte], 0x04.asInstanceOf[Byte], 0xdd.asInstanceOf[Byte], 0xef.asInstanceOf[Byte], 0x95.asInstanceOf[Byte], 0x19.asInstanceOf[Byte], 0xb3.asInstanceOf[Byte], 0xcd.asInstanceOf[Byte], 0x3a.asInstanceOf[Byte], 0x43.asInstanceOf[Byte], 0x1b.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x2b.asInstanceOf[Byte], 0x0a.asInstanceOf[Byte], 0x6d.asInstanceOf[Byte], 0xf2.asInstanceOf[Byte], 0x5f.asInstanceOf[Byte], 0x14.asInstanceOf[Byte], 0x37.asInstanceOf[Byte], 0x4f.asInstanceOf[Byte], 0xe1.asInstanceOf[Byte], 0x35.asInstanceOf[Byte], 0x6d.asInstanceOf[Byte], 0x6d.asInstanceOf[Byte], 0x51.asInstanceOf[Byte], 0xc2.asInstanceOf[Byte], 0x45.asInstanceOf[Byte], 0xe4.asInstanceOf[Byte], 0x85.asInstanceOf[Byte], 0xb5.asInstanceOf[Byte], 0x76.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x5e.asInstanceOf[Byte], 0x7e.asInstanceOf[Byte], 0xc6.asInstanceOf[Byte], 0xf4.asInstanceOf[Byte], 0x4c.asInstanceOf[Byte], 0x42.asInstanceOf[Byte], 0xe9.asInstanceOf[Byte], 0xa6.asInstanceOf[Byte], 0x37.asInstanceOf[Byte], 0xed.asInstanceOf[Byte], 0x6b.asInstanceOf[Byte], 0x0b.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0x5c.asInstanceOf[Byte], 0xb6.asInstanceOf[Byte], 0xf4.asInstanceOf[Byte], 0x06.asInstanceOf[Byte], 0xb7.asInstanceOf[Byte], 0xed.asInstanceOf[Byte], 0xee.asInstanceOf[Byte], 0x38.asInstanceOf[Byte], 0x6b.asInstanceOf[Byte], 0xfb.asInstanceOf[Byte], 0x5a.asInstanceOf[Byte], 0x89.asInstanceOf[Byte], 0x9f.asInstanceOf[Byte], 0xa5.asInstanceOf[Byte], 0xae.asInstanceOf[Byte], 0x9f.asInstanceOf[Byte], 0x24.asInstanceOf[Byte], 0x11.asInstanceOf[Byte], 0x7c.asInstanceOf[Byte], 0x4b.asInstanceOf[Byte], 0x1f.asInstanceOf[Byte], 0xe6.asInstanceOf[Byte], 0x49.asInstanceOf[Byte], 0x28.asInstanceOf[Byte], 0x66.asInstanceOf[Byte], 0x51.asInstanceOf[Byte], 0xec.asInstanceOf[Byte], 0xe6.asInstanceOf[Byte], 0x53.asInstanceOf[Byte], 0x81.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte])

  def validateScheme(pBuffer: Array[Byte], scheme: Int): Boolean = {

    var digestOffset: Int = -1

    scheme match {
      case 0 =>
        digestOffset = getDigestOffset0(pBuffer)
      case 1 =>
        digestOffset = getDigestOffset1(pBuffer)
      case _ =>
        // Unknown scheme
        return false
    }

    // log.debug("Scheme: {} client digest offset: {}", scheme, digestOffset)

    val tempBuffer: Array[Byte] = new Array[Byte](HANDSHAKE_SIZE - DIGEST_LENGTH)
    System.arraycopy(pBuffer, 0, tempBuffer, 0, digestOffset)
    System.arraycopy(pBuffer, digestOffset + DIGEST_LENGTH, tempBuffer, digestOffset, HANDSHAKE_SIZE - digestOffset - DIGEST_LENGTH)

    val tempHash: Array[Byte] = calculateHMAC_SHA256(tempBuffer, GENUINE_FP_KEY, 30)

    // log.debug("Temp: {}", Hex.encodeHexString(tempHash))

    var i: Int = 0
    while (i < DIGEST_LENGTH) {

        if (pBuffer(digestOffset + i) != tempHash(i)) {
          return false
        }

        i = i+1
    }

    true
  }

  /**
   * Calculates an HMAC SHA256 hash using a default key length.
   *
   * @param input
   * @param key
   * @return hmac hashed bytes
   */
  def calculateHMAC_SHA256(input: Array[Byte], key: Array[Byte]): Array[Byte] = {
    var output: Array[Byte] = null
    try {
      hmacSHA256.init(new SecretKeySpec(key, "HmacSHA256"))
      output = hmacSHA256.doFinal(input)
    }
    catch {
      case e: InvalidKeyException => {
        log.error("Invalid key", e)
      }
    }
    return output
  }

  /**
   * Calculates an HMAC SHA256 hash using a set key length.
   *
   * @param input
   * @param key
   * @param length
   * @return hmac hashed bytes
   */
  def calculateHMAC_SHA256(input: Array[Byte], key: Array[Byte], length: Int): Array[Byte] = {
    var output: Array[Byte] = null
    try {
      hmacSHA256.init(new SecretKeySpec(key, 0, length, "HmacSHA256"))
      output = hmacSHA256.doFinal(input)
    }
    catch {
      case e: InvalidKeyException => {
        log.error("Invalid key", e)
      }
    }
    return output
  }

  /**
   * Creates a Diffie-Hellman key pair.
   *
   * @return dh keypair
   */
  protected def generateKeyPair: KeyPair = {
    var keyPair: KeyPair = null
    val keySpec: DHParameterSpec = new DHParameterSpec(DH_MODULUS, DH_BASE)
    try {
      val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("DH")
      keyGen.initialize(keySpec)
      keyPair = keyGen.generateKeyPair
      keyAgreement = KeyAgreement.getInstance("DH")
      keyAgreement.init(keyPair.getPrivate)
    }
    catch {
      case e: Exception => {
        log.error("Error generating keypair", e)
      }
    }
    return keyPair
  }

  /**
   * Returns the public key for a given key pair.
   *
   * @param keyPair
   * @return public key
   */
  protected def getPublicKey(keyPair: KeyPair): Array[Byte] = {
    val incomingPublicKey: DHPublicKey = keyPair.getPublic.asInstanceOf[DHPublicKey]
    val dhY: BigInteger = incomingPublicKey.getY
    log.debug("Public key: {}", dhY)
    var result: Array[Byte] = dhY.toByteArray
    log.debug("Public key as bytes - length [{}]: {}", result.length, Hex.encodeHexString(result))
    val temp: Array[Byte] = new Array[Byte](KEY_LENGTH)
    if (result.length < KEY_LENGTH) {
      System.arraycopy(result, 0, temp, KEY_LENGTH - result.length, result.length)
      result = temp
      log.debug("Padded public key length to 128")
    }
    else if (result.length > KEY_LENGTH) {
      System.arraycopy(result, result.length - KEY_LENGTH, temp, 0, KEY_LENGTH)
      result = temp
      log.debug("Truncated public key length to 128")
    }
    return result
  }

  /**
   * Determines the validation scheme for given input.
   *
   * @param otherPublicKeyBytes
   * @param agreement
   * @return shared secret bytes if client used a supported validation scheme
   */
  protected def getSharedSecret(otherPublicKeyBytes: Array[Byte], agreement: KeyAgreement): Array[Byte] = {
    val otherPublicKeyInt: BigInteger = new BigInteger(1, otherPublicKeyBytes)
    try {
      val keyFactory: KeyFactory = KeyFactory.getInstance("DH")
      val otherPublicKeySpec: KeySpec = new DHPublicKeySpec(otherPublicKeyInt, RTMPHandshake.DH_MODULUS, RTMPHandshake.DH_BASE)
      val otherPublicKey: PublicKey = keyFactory.generatePublic(otherPublicKeySpec)
      agreement.doPhase(otherPublicKey, true)
    }
    catch {
      case e: Exception => {
        log.error("Exception getting the shared secret", e)
      }
    }
    val sharedSecret: Array[Byte] = agreement.generateSecret
    log.debug("Shared secret [{}]: {}", sharedSecret.length, Hex.encodeHexString(sharedSecret))
    return sharedSecret
  }

  /**
   * Returns the DH offset from an array of bytes.
   *
   * @param bytes
   * @return DH offset
   */
  protected def getDHOffset(bytes: Array[Byte]): Int = {
    var dhOffset: Int = -1
    validationScheme match {
      case 1 =>
        dhOffset = getDHOffset1(bytes)
        break //todo: break is not supported
      case _ =>
        log.debug("Scheme 0 will be used for DH offset")
      case 0 =>
        dhOffset = getDHOffset0(bytes)
    }
    return dhOffset
  }

  /**
   * Returns the DH byte offset.
   *
   * @return dh offset
   */
  protected def getDHOffset0(bytes: Array[Byte]): Int = {
    var offset: Int = (bytes(1532) & 0x0ff) + (bytes(1533) & 0x0ff) + (bytes(1534) & 0x0ff) + (bytes(1535) & 0x0ff)
    offset = offset % 632
    offset = offset + 772
    if (offset + KEY_LENGTH >= 1536) {
      log.error("Invalid DH offset")
    }
    return offset
  }

  /**
   * Returns the DH byte offset.
   *
   * @return dh offset
   */
  protected def getDHOffset1(bytes: Array[Byte]): Int = {
    var offset: Int = (bytes(768) & 0x0ff) + (bytes(769) & 0x0ff) + (bytes(770) & 0x0ff) + (bytes(771) & 0x0ff)
    offset = offset % 632
    offset = offset + 8
    if (offset + KEY_LENGTH >= 1536) {
      log.error("Invalid DH offset")
    }
    return offset
  }

  /**
   * Returns the digest offset using current validation scheme.
   *
   * @param pBuffer
   * @return digest offset
   */
  protected def getDigestOffset(pBuffer: Array[Byte]): Int = {
    var serverDigestOffset: Int = -1
    validationScheme match {
      case 1 =>
        serverDigestOffset = getDigestOffset1(pBuffer)
        break //todo: break is not supported
      case _ =>
        log.debug("Scheme 0 will be used for DH offset")
      case 0 =>
        serverDigestOffset = getDigestOffset0(pBuffer)
    }
    return serverDigestOffset
  }

  /**
   * Returns a digest byte offset.
   *
   * @param pBuffer source for digest data
   * @return digest offset
   */
  protected def getDigestOffset0(pBuffer: Array[Byte]): Int = {
    if (log.isTraceEnabled) {
      log.trace("Scheme 0 offset bytes {},{},{},{}", Array[AnyRef]((pBuffer(8) & 0x0ff), (pBuffer(9) & 0x0ff), (pBuffer(10) & 0x0ff), (pBuffer(11) & 0x0ff)))
    }
    var offset: Int = (pBuffer(8) & 0x0ff) + (pBuffer(9) & 0x0ff) + (pBuffer(10) & 0x0ff) + (pBuffer(11) & 0x0ff)
    offset = offset % 728
    offset = offset + 12
    if (offset + DIGEST_LENGTH >= 1536) {
      log.error("Invalid digest offset")
    }
    return offset
  }

  /**
   * Returns a digest byte offset.
   *
   * @param pBuffer source for digest data
   * @return digest offset
   */
  protected def getDigestOffset1(pBuffer: Array[Byte]): Int = {
    if (log.isTraceEnabled) {
      log.trace("Scheme 1 offset bytes {},{},{},{}", Array[AnyRef]((pBuffer(772) & 0x0ff), (pBuffer(773) & 0x0ff), (pBuffer(774) & 0x0ff), (pBuffer(775) & 0x0ff)))
    }
    var offset: Int = (pBuffer(772) & 0x0ff) + (pBuffer(773) & 0x0ff) + (pBuffer(774) & 0x0ff) + (pBuffer(775) & 0x0ff)
    offset = offset % 728
    offset = offset + 776
    if (offset + DIGEST_LENGTH >= 1536) {
      log.error("Invalid digest offset")
    }
    return offset
  }
}
