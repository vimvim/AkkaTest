package rtmp.packet

import scala.collection.immutable.ListSet
import akka.util.ByteString

/**
 *
 */
sealed trait Packet

case class Notify(action:String, params:List[Any]) extends Packet
case class Invoke(action:String, invokeID:Int, params:List[Any]) extends Packet

case class Video(data:ByteString) extends Packet
case class Audio(data:ByteString) extends Packet
