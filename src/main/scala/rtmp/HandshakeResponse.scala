package rtmp

import java.util.Random

/**
 *
 */
class HandshakeResponse {

  val bytes = new Array[Byte](Handshake.HANDSHAKE_SIZE)

  protected final val random: Random = new Random

  createHandshakeBytes()

  /**
   * Creates the servers handshake bytes
   */
  protected def createHandshakeBytes():Unit = {

    random.nextBytes(bytes)

    bytes(0) = 0
    bytes(1) = 0
    bytes(2) = 0
    bytes(3) = 0

    bytes(4) = 1
    bytes(5) = 2
    bytes(6) = 3
    bytes(7) = 4
  }
}
