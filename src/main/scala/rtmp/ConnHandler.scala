package rtmp

import akka.actor._
import akka.io.{ IO, Tcp }
import scala.concurrent.duration._
import akka.util.{CompactByteString, ByteString}
import java.net.InetSocketAddress


import akka.event.Logging
import java.security.KeyPair
import rtmp.v2.Protocol
import rtmp.protocol.v2.handshake.{Constants, Crypto}
import rtmp.protocol.BaseProtocol
import java.util.Random
import rtmp.protocol.v2.handshake.Crypto

sealed trait State
case object HandshakeGet extends State
case object HandshakeConfirm extends State
case object ReceiveHeader extends State
case object ReceiveBody extends State
case object Open extends State
case object ClosedConn extends State


sealed trait Data
case class Handshake(buffer:ByteString) extends Data
case class HandshakeData extends Data

/**
 *
 * TODO: Check how and where new byte buffers is created and optimize it.
 */
class ConnHandler(connection: ActorRef, remote: InetSocketAddress) extends Actor with FSM[State, Data] with ActorLogging  {

  import Tcp._

  startWith(HandshakeGet, Handshake(CompactByteString("")))

  when(HandshakeGet) {
    case Event(Received(data), Handshake(accumulatedBuffer)) ⇒

      val buffer = accumulatedBuffer.concat(data)

      processBuffer(buffer, Constants.HANDSHAKE_SIZE, (buffer, input)=>{

        val handshakeType = input(0)
        val versionByte = input(4)

        if (log.isDebugEnabled) {

          log.debug("Player encryption byte: {}", handshakeType)
          log.debug("Player version byte: {}", versionByte & 0x0ff)

          // If the 5th byte is 0 then dont generate new-style handshake
          log.debug("Detecting flash player version {},{},{},{}", Array[Int](input(4) & 0x0ff, input(5) & 0x0ff, input(6) & 0x0ff, input(7) & 0x0ff))
        }

        val protocol = createProtocol(handshakeType)
        val response = protocol.handshake(input)

        connection ! Write(response.serialize())

        goto(HandshakeConfirm)

      }, (restBuffer)=>{
        stay using Handshake(restBuffer)
      })
  }

  when(HandshakeConfirm) {
    case Event(Received(data), Handshake) ⇒ stay()
  }

  when(ReceiveHeader) {
    case Event(Received(data), Handshake) ⇒ stay()
  }

  when(ReceiveBody) {
    case Event(Received(data), Handshake) ⇒ stay()
  }

  when(ClosedConn) {
    case Event(Received(data), Uninitialized) ⇒ stay()
  }

  whenUnhandled {
    case Event(PeerClosed, _) ⇒
      // context stop self
      goto(ClosedConn)
  }

  initialize()

  def processBuffer(buffer:ByteString, chunkSize:Int, handlerFunc: (ByteString, Array[Byte])=>State, stayFunc: (ByteString)=>State):State = {

    if (buffer.length>=chunkSize) {

      val split = buffer.splitAt(chunkSize)

      val compactData = split._2.compact
      val input = compactData.asByteBuffer.array()

      handlerFunc(split._1, input)

    } else {

      stayFunc(buffer)
    }
  }

  def createProtocol(handshakeVersion:Byte):BaseProtocol = {

    handshakeVersion match {

      // Implement protocol V1 and uncomment this
      // case 0 =>
      //   new rtmp.v1.Protocol()

      case 0x03 =>

        val random: Random = new Random

        val keyPair = Crypto.generateKeyPair

        val randBytes1 = new Array[Byte](Constants.HANDSHAKE_SIZE-8)
        random.nextBytes(randBytes1)

        val randBytes2: Array[Byte] = new Array[Byte](Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)
        random.nextBytes(randBytes2)

        implicit val log = this.log

        new rtmp.v2.Protocol(keyPair, randBytes1, randBytes2)

      case 0x06 => throw new Exception("Encrypted connections is not supported yet {}")
      case _ => throw new Exception(s"Unsupported handshake version specified $handshakeVersion ")
    }
  }

  def getHeaderType(firstByte:Byte) = {

    if ((firstByte & 0x3f) == 0) {
      if (remaining < 2) {
        in.position(position)
        state.bufferDecoding(2)
        return null
      }
      headerValue = (headerByte & 0xff) << 8 | (in.get & 0xff)
      byteCount = 2
    } else if ((firstByte & 0x3f) == 1) {
      if (remaining < 3) {
        in.position(position)
        state.bufferDecoding(3)
        return null
      }
      headerValue = (headerByte & 0xff) << 16 | (in.get & 0xff) << 8 | (in.get & 0xff)
      byteCount = 3
    } else {
      headerValue = headerByte & 0xff
      byteCount = 1
    }
  }

  /*
  def receive = {
    case Received(data) ⇒ sender ! Write(data)
    case PeerClosed     ⇒ context stop self
  }
  */
}
