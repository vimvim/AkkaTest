package rtmp

import java.security.KeyPair

/**
 * Provide data required for RTMP handshake ( rand bytes, key, ... )
 */
trait HandshakeDataProvider {

  def getKeyPair: KeyPair

  def getRand1: Array[Byte]

  def getRand2: Array[Byte]
}
