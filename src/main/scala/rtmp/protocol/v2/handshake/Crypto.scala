package rtmp.protocol.v2.handshake

import javax.crypto.spec.{DHPublicKeySpec, DHParameterSpec, SecretKeySpec}
import java.security.{PublicKey, KeyFactory, KeyPairGenerator, KeyPair}
import javax.crypto.{Mac, KeyAgreement}
import javax.crypto.interfaces.DHPublicKey
import java.math.BigInteger
import java.security.spec.KeySpec

/**
 *
 */
object Crypto {

  /** Modulus bytes from flazr */
  final val DH_MODULUS_BYTES: Array[Byte] = Array(0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xc9.asInstanceOf[Byte], 0x0f.asInstanceOf[Byte], 0xda.asInstanceOf[Byte], 0xa2.asInstanceOf[Byte], 0x21.asInstanceOf[Byte], 0x68.asInstanceOf[Byte], 0xc2.asInstanceOf[Byte], 0x34.asInstanceOf[Byte], 0xc4.asInstanceOf[Byte], 0xc6.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x8b.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0xdc.asInstanceOf[Byte], 0x1c.asInstanceOf[Byte], 0xd1.asInstanceOf[Byte], 0x29.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x4e.asInstanceOf[Byte], 0x08.asInstanceOf[Byte], 0x8a.asInstanceOf[Byte], 0x67.asInstanceOf[Byte], 0xcc.asInstanceOf[Byte], 0x74.asInstanceOf[Byte], 0x02.asInstanceOf[Byte], 0x0b.asInstanceOf[Byte], 0xbe.asInstanceOf[Byte], 0xa6.asInstanceOf[Byte], 0x3b.asInstanceOf[Byte], 0x13.asInstanceOf[Byte], 0x9b.asInstanceOf[Byte], 0x22.asInstanceOf[Byte], 0x51.asInstanceOf[Byte], 0x4a.asInstanceOf[Byte], 0x08.asInstanceOf[Byte], 0x79.asInstanceOf[Byte], 0x8e.asInstanceOf[Byte], 0x34.asInstanceOf[Byte], 0x04.asInstanceOf[Byte], 0xdd.asInstanceOf[Byte], 0xef.asInstanceOf[Byte], 0x95.asInstanceOf[Byte], 0x19.asInstanceOf[Byte], 0xb3.asInstanceOf[Byte], 0xcd.asInstanceOf[Byte], 0x3a.asInstanceOf[Byte], 0x43.asInstanceOf[Byte], 0x1b.asInstanceOf[Byte], 0x30.asInstanceOf[Byte], 0x2b.asInstanceOf[Byte], 0x0a.asInstanceOf[Byte], 0x6d.asInstanceOf[Byte], 0xf2.asInstanceOf[Byte], 0x5f.asInstanceOf[Byte], 0x14.asInstanceOf[Byte], 0x37.asInstanceOf[Byte], 0x4f.asInstanceOf[Byte], 0xe1.asInstanceOf[Byte], 0x35.asInstanceOf[Byte], 0x6d.asInstanceOf[Byte], 0x6d.asInstanceOf[Byte], 0x51.asInstanceOf[Byte], 0xc2.asInstanceOf[Byte], 0x45.asInstanceOf[Byte], 0xe4.asInstanceOf[Byte], 0x85.asInstanceOf[Byte], 0xb5.asInstanceOf[Byte], 0x76.asInstanceOf[Byte], 0x62.asInstanceOf[Byte], 0x5e.asInstanceOf[Byte], 0x7e.asInstanceOf[Byte], 0xc6.asInstanceOf[Byte], 0xf4.asInstanceOf[Byte], 0x4c.asInstanceOf[Byte], 0x42.asInstanceOf[Byte], 0xe9.asInstanceOf[Byte], 0xa6.asInstanceOf[Byte], 0x37.asInstanceOf[Byte], 0xed.asInstanceOf[Byte], 0x6b.asInstanceOf[Byte], 0x0b.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0x5c.asInstanceOf[Byte], 0xb6.asInstanceOf[Byte], 0xf4.asInstanceOf[Byte], 0x06.asInstanceOf[Byte], 0xb7.asInstanceOf[Byte], 0xed.asInstanceOf[Byte], 0xee.asInstanceOf[Byte], 0x38.asInstanceOf[Byte], 0x6b.asInstanceOf[Byte], 0xfb.asInstanceOf[Byte], 0x5a.asInstanceOf[Byte], 0x89.asInstanceOf[Byte], 0x9f.asInstanceOf[Byte], 0xa5.asInstanceOf[Byte], 0xae.asInstanceOf[Byte], 0x9f.asInstanceOf[Byte], 0x24.asInstanceOf[Byte], 0x11.asInstanceOf[Byte], 0x7c.asInstanceOf[Byte], 0x4b.asInstanceOf[Byte], 0x1f.asInstanceOf[Byte], 0xe6.asInstanceOf[Byte], 0x49.asInstanceOf[Byte], 0x28.asInstanceOf[Byte], 0x66.asInstanceOf[Byte], 0x51.asInstanceOf[Byte], 0xec.asInstanceOf[Byte], 0xe6.asInstanceOf[Byte], 0x53.asInstanceOf[Byte], 0x81.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte], 0xff.asInstanceOf[Byte])

  final val DH_MODULUS: BigInteger = new BigInteger(1, DH_MODULUS_BYTES)

  final val DH_BASE: BigInteger = BigInteger.valueOf(2)

  val hmacSHA256 = Mac.getInstance("HmacSHA256")

  /**
   * Calculates an HMAC SHA256 hash using a default key length.
   *
   * @param input
   * @param key
   * @return hmac hashed bytes
   */
  def calculateHMAC_SHA256(input: Array[Byte], key: Array[Byte]): Array[Byte] = {
    hmacSHA256.init(new SecretKeySpec(key, "HmacSHA256"))
    hmacSHA256.doFinal(input)
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
    hmacSHA256.init(new SecretKeySpec(key, 0, length, "HmacSHA256"))
    hmacSHA256.doFinal(input)
  }

  /**
   * Creates a Diffie-Hellman key pair.
   *
   * @return dh keypair
   */
  def generateKeyPair: KeyPair = {

    var keyPair: KeyPair = null
    val keySpec: DHParameterSpec = new DHParameterSpec(DH_MODULUS, DH_BASE)

    val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("DH")
    keyGen.initialize(keySpec)
    keyPair = keyGen.generateKeyPair

    val keyAgreement = KeyAgreement.getInstance("DH")
    keyAgreement.init(keyPair.getPrivate)

    keyPair
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

    val keyFactory: KeyFactory = KeyFactory.getInstance("DH")
    val otherPublicKeySpec: KeySpec = new DHPublicKeySpec(otherPublicKeyInt, DH_MODULUS, DH_BASE)
    val otherPublicKey: PublicKey = keyFactory.generatePublic(otherPublicKeySpec)
    agreement.doPhase(otherPublicKey, true)

    val sharedSecret: Array[Byte] = agreement.generateSecret

    // log.debug("Shared secret [{}]: {}", sharedSecret.length, Hex.encodeHexString(sharedSecret))

    sharedSecret
  }

}
