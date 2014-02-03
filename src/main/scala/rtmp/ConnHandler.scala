package rtmp

import akka.actor.{ Actor, ActorRef, Props, FSM }
import akka.io.{ IO, Tcp }
import scala.concurrent.duration._
import akka.util.ByteString
import java.net.InetSocketAddress


import akka.event.Logging
import java.security.KeyPair
import rtmp.v2.Protocol
import rtmp.v2.handshake.{Constants, Crypto}
import rtmp.protocol.BaseProtocol
import java.util.Random

sealed trait State
case object HandshakeGet extends State
case object HandshakeConfirm extends State
case object Open extends State
case object ClosedConn extends State


sealed trait Data
case object Uninitialized extends Data
case object HandshakeData extends Data

/**
 *
 */
class ConnHandler extends Actor with FSM[State, Data] {

  import Tcp._

  startWith(HandshakeGet, Uninitialized)

  when(HandshakeGet) {
    case Event(Received(data), Uninitialized) ⇒

      // if (!data.isCompact) {
      //  data.compact
      // }

      // log.isDebugEnabled

      // TODO: Check is there are new byte buffers is created. We needs to eliminate this.
      val compactData = data.compact
      val input = compactData.asByteBuffer.array()

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


      // TODO: How we will test this ??? Do we needs to create separated Protocol Actor ??
      // TODO: Seems that Write is are simple message so we can expect to test it using testing framework
      Write(response.serialize())

      // stay using Todo(ref, Vector.empty)
    goto(HandshakeConfirm)
  }

  when(HandshakeConfirm) {
    case Event(Received(data), Uninitialized) ⇒ stay()
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

  def createProtocol(handshakeVersion:Byte):BaseProtocol = {

    handshakeVersion match {

      case 0 =>
        new rtmp.v1.Protocol()

      case 0x03 =>

        val random: Random = new Random

        val keyPair = Crypto.generateKeyPair

        val randBytes1 = new Array[Byte](Constants.HANDSHAKE_SIZE-8)
        random.nextBytes(randBytes1)

        val randBytes2: Array[Byte] = new Array[Byte](Constants.HANDSHAKE_SIZE - Constants.DIGEST_LENGTH)
        random.nextBytes(randBytes2)

        new rtmp.v2.Protocol(keyPair, randBytes1, randBytes2)

      case 0x06 => throw new Exception("Encrypted connections is not supported yet {}")
      case _ => throw new Exception(s"Unsupported handshake version specified $handshakeVersion ")
    }
  }

  /*
  def receive = {
    case Received(data) ⇒ sender ! Write(data)
    case PeerClosed     ⇒ context stop self
  }
  */
}
