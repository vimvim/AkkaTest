package rtmp

import java.security.KeyPair
import java.util.Random

import rtmp.protocol.v2.handshake.{Constants, Crypto}


/**
 * Handshake data generator
 */
class HandshakeDataGen extends HandshakeDataProvider {

  val random: Random = new Random

  override def getKeyPair: KeyPair = {
    Crypto.generateKeyPair
  }

  override def getRand1: Array[Byte] = {

    val randBytes1 = new Array[Byte](Constants.HANDSHAKE_SIZE-8)
    random.nextBytes(randBytes1)

    randBytes1
  }

  override def getRand2: Array[Byte] = {

    val randBytes2: Array[Byte] = new Array[Byte](Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)
    random.nextBytes(randBytes2)

    randBytes2
  }
}
