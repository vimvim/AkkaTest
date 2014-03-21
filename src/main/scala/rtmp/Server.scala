package rtmp

import java.net.InetSocketAddress
import akka.util.ByteString

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.io.{ IO, Tcp }


/**
 *
 */
class Server extends Actor  with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 2222))

  def receive = {
    case b @ Bound(localAddress) ⇒
      log.info("Bound to interface")

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒

      log.info("Connected: "+remote)

      val connection = sender

      val clientHandler = context.actorOf(Props(classOf[ClientHandler], connection, remote))

      val handler = context.actorOf(Props(classOf[ConnHandler], connection, remote, clientHandler))
      connection ! Register(handler)
  }
}
