package rtmp

import java.util.Random
import java.nio.ByteBuffer
import scala.Exception
import java.security.KeyPair
import java.util.logging.{Level, Logger}
import akka.event.LoggingAdapter
import akka.util.{ByteStringBuilder, ByteString}


/**
 *
 */
class HandshakeResponse(input:Array[Byte])(implicit val log:LoggingAdapter) {

  val log = Logger.getLogger(classOf[HandshakeResponse].getCanonicalName)

  protected final val random: Random = new Random

  // val output = ByteBuffer.allocateDirect(Handshake.HANDSHAKE_SIZE_SERVER)
  val output = new ByteStringBuilder()


  val handshakeType = input(0)
  val versionByte = input(4)

  if (log.isDebugEnabled) {

    log.debug("Player encryption byte: {}", handshakeType)
    log.debug("Player version byte: {}", versionByte & 0x0ff)

    //if the 5th byte is 0 then dont generate new-style handshake
    log.debug("Detecting flash player version {},{},{},{}", Array[Int](input(4) & 0x0ff, input(5) & 0x0ff, input(6) & 0x0ff, input(7) & 0x0ff))
  }

  // TODO: MOVE AGAIN HANDSHAKE VERSION DETECT CODE TO THE ConnHandler. Create several handshake subclasses
  // TODO: FOR SEVERAL TYPES OF THE HANDSHAKE ( PLAIN OLD, DIGESTED, ENCRYPTED, ... )
  log.debug("Handshake version {}", handshakeType)

  handshakeType match {
    case 0 => createHandshakeV1()
    case 0x03 => createHandshakeV2()
    case 0x06 => throw new Exception("Encrypted connections is not supported yet {}")
    case _ => throw new Exception(s"Unsupported handshake version specified $handshakeType ")
  }

  protected def createHandshakeV1() = {

  }

  protected def createHandshakeV2() = {

    //create keypair
    val keys: KeyPair = Handshake.generateKeyPair

    //get public key
    val incomingPublicKey = Handshake.getPublicKey(keys)


    val validationScheme = createValidationScheme()
    log.debug(s"Valid RTMP client detected. Validation scheme: ${validationScheme.id}")

    val clientDHOffset: Int = validationScheme.getDHOffset(input)
    log.debug("Incoming DH offset: {}", clientDHOffset)

    // Get the clients public key
    val outgoingPublicKey = new Array[Byte](Handshake.KEY_LENGTH)
    System.arraycopy(input, clientDHOffset, outgoingPublicKey, 0, Handshake.KEY_LENGTH)

    val keyChallengeIndex: Int = validationScheme.getDigestOffset(input)
    val challengeKey: Array[Byte] = new Array[Byte](Handshake.DIGEST_LENGTH)
    System.arraycopy(input, keyChallengeIndex, challengeKey, 0, Handshake.DIGEST_LENGTH)

    // Used for creating hash on the  random data
    val key = Handshake.calculateHMAC_SHA256(challengeKey, Handshake.GENUINE_FMS_KEY, 68)



    val handshakeBytes = new Array[Byte](Handshake.HANDSHAKE_SIZE)
    random.nextBytes(handshakeBytes)

    handshakeBytes.update(0, 0)
    handshakeBytes.update(1, 0)
    handshakeBytes.update(2, 0)
    handshakeBytes.update(3, 0)

    handshakeBytes.update(4, 1)
    handshakeBytes.update(5, 2)
    handshakeBytes.update(6, 3)
    handshakeBytes.update(7, 4)


    //get the servers dh offset
    val serverDHOffset: Int = validationScheme.getDHOffset(handshakeBytes)
    log.debug("Outgoing DH offset: {}", serverDHOffset)


    // Create handshake response and add public key to it
    System.arraycopy(incomingPublicKey, 0, handshakeBytes, serverDHOffset, Handshake.KEY_LENGTH)


    // Calculate the server digest ( hash ) and add to handshake data
    // TODO: Think about implementing this calculations in the more "visual" way
    val serverDigestOffset: Int = validationScheme.getDigestOffset(handshakeBytes)

    val tempBuffer: Array[Byte] = new Array[Byte](Handshake.HANDSHAKE_SIZE - Handshake.DIGEST_LENGTH)
    System.arraycopy(handshakeBytes, 0, tempBuffer, 0, serverDigestOffset)
    System.arraycopy(handshakeBytes, serverDigestOffset + Handshake.DIGEST_LENGTH, tempBuffer, serverDigestOffset, Handshake.HANDSHAKE_SIZE - serverDigestOffset - Handshake.DIGEST_LENGTH)

    val serverHandshakeHash = Handshake.calculateHMAC_SHA256(tempBuffer, Handshake.GENUINE_FMS_KEY, 36)
    System.arraycopy(serverHandshakeHash, 0, handshakeBytes, serverDigestOffset, Handshake.DIGEST_LENGTH)


    // Generate random data and sign it with the key
    val randBytes: Array[Byte] = new Array[Byte](Handshake.HANDSHAKE_SIZE - Handshake.DIGEST_LENGTH)
    random.nextBytes(randBytes)

    val randBytesHash: Array[Byte] = Handshake.calculateHMAC_SHA256(randBytes, key, Handshake.DIGEST_LENGTH)


    output.put(handshakeType)
    output.put(handshakeBytes)
    output.put(randBytes)
    output.put(randBytesHash)
    output.flip()
  }

  protected def createValidationScheme():HandshakeValidationScheme = {

    val schema1 = new HandshakeValidationScheme0()
    if (schema1.validate(input)) schema1

    val schema0 = new HandshakeValidationScheme0()
    if (!schema0.validate(input)) {
      log.info("Invalid RTMP connection data detected, you may experience errors")
    }

    schema0
  }

}
