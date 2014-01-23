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

      log.isDebugEnabled

      // TODO: Check is there are new byte buffers is created. We needs to eliminate this.
      val compactData = data.compact
      val bytes = compactData.asByteBuffer.array()









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
