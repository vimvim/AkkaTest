package rtmp

import akka.actor.{ Actor, ActorRef, Props, FSM }
import akka.io.{ IO, Tcp }
import scala.concurrent.duration._
import akka.util.ByteString
import java.net.InetSocketAddress


import akka.event.Logging

sealed trait State
case object HandshakeGet extends State
case object HandshakeConfirm extends State
case object Open extends State
case object ClosedConn extends State


sealed trait Data
case object Uninitialized extends Data
case object Handshake extends Data

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

      // TODO: Check is there are new byte buffers is created. We needs to eliminate this.
      val compactData = data.compact
      val bytes = compactData.asByteBuffer.array()

      val handshakeType = bytes(0)
      val versionByte = bytes(4)

      if (log.isDebugEnabled) {

        log.debug("Player encryption byte: {}", handshakeType)
        log.debug("Player version byte: {}", versionByte & 0x0ff)

        //if the 5th byte is 0 then dont generate new-style handshake
        log.debug("Detecting flash player version {},{},{},{}", Array[Int](bytes(4) & 0x0ff, bytes(5) & 0x0ff, bytes(6) & 0x0ff, bytes(7) & 0x0ff))
      }







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

  /*
  def receive = {
    case Received(data) ⇒ sender ! Write(data)
    case PeerClosed     ⇒ context stop self
  }
  */
}
