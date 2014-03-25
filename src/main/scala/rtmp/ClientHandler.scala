package rtmp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}

import rtmp.packet.Packet

/**
 * Sent by connection handler during initial registration
 *
 * @param connHandler       Connection handler itself
 */
case class RegisterHandler(connHandler:ActorRef)

/**
 * Sent by connection handler when connection handshake is done
 *
 * @param systemChannel     Handler for system channel
 */
case class HandshakeDone(systemChannel:ActorRef)

/**
 * Sent by channel handler when new message ir received and parsed
 *
 * @param channelId
 * @param timestamp
 * @param extendedTime
 * @param messageSID
 * @param packet
 */
case class Message(channelId:Int, timestamp:Int, extendedTime:Int, messageSID:Int, packet:Packet)

/**
 * Coordinate all RTMP client activity
 */
class ClientHandler(val connection:ActorRef, val remote: InetSocketAddress) extends Actor with ActorLogging {

  override def receive: Actor.Receive = {

    case Message(channelId, timestamp, extendedTime, messageSID, packet) =>
    case _ =>

  }

  private def handleMessage(msg:Message) = {

    log.debug("New message {}", msg)

  }

}
