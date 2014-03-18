package rtmp.packet

import scala.collection.immutable.ListSet

/**
 *
 */
sealed trait Packet

case class Invoke(action:String, invokeID:Int, params:ListSet[Any]) extends Packet
