package rtmp.packet

import scala.reflect.ClassTag
import scala.collection.immutable.ListSet

import akka.util.ByteString

import rtmp.amf.Serializer
import rtmp.status.Status


/**
 *
 */
sealed trait Packet {
  def typeID:Byte
  def serialize(serializer:Serializer):Serializer
}

case class Notify(action:String, params:List[Any]) extends Packet {

  def typeID = PacketTypes.TYPE_NOTIFY

  override def serialize(serializer: Serializer):Serializer = {
    serializer
  }
}

case class Invoke(action:String, invokeID:Int, params:List[Any]) extends Packet {

  def typeID = PacketTypes.TYPE_INVOKE

  override def serialize(serializer: Serializer):Serializer = {

    serializer.writeObject(action)
    serializer.writeObject(invokeID)

    // TODO: Write conn params
    serializer.writeObject(null)

    params.foreach((param)=>{
      serializer.writeObject(param)
    })

    serializer
  }
}

case class InvokeResponse[T: ClassTag](invoke:Invoke, success:Boolean, result:T) extends Packet {

  def typeID = PacketTypes.TYPE_INVOKE

  override def serialize(serializer: Serializer):Serializer = {

    if (success) serializer.writeObject("_result") else serializer.writeObject("_error")

    serializer.writeObject(invoke.invokeID)

    // TODO: Conn parameters. What is this ??
    serializer.writeObject(null)

    serializer.writeObject(result)

    serializer
  }
}

case class Video(data:ByteString) extends Packet {

  def typeID = PacketTypes.TYPE_VIDEO_DATA

  override def serialize(serializer: Serializer):Serializer = {

    serializer
  }
}

case class Audio(data:ByteString) extends Packet {

  def typeID = PacketTypes.TYPE_AUDIO_DATA

  override def serialize(serializer: Serializer):Serializer = {

    serializer
  }
}

case class ServerBW(bandwidth:Int) extends Packet {

  def typeID = PacketTypes.TYPE_SERVER_BANDWIDTH

  override def serialize(serializer: Serializer):Serializer = {
    serializer.writeInt(bandwidth)
  }
}

case class ClientBW(bandwidth:Int, limitType:Byte) extends Packet {

  val LIMIT_HARD:Byte = 0
  val LIMIT_SOFT:Byte = 1
  val LIMIT_DYNAMIC:Byte = 2

  def typeID = PacketTypes.TYPE_CLIENT_BANDWIDTH

  override def serialize(serializer: Serializer):Serializer = {
    serializer.writeInt(bandwidth)
    serializer.writeByte(limitType)
  }
}

trait Control {

  def ctrlType:Short
  def typeID = PacketTypes.TYPE_PING

  def serialize(serializer: Serializer):Serializer = serializer.writeShort(ctrlType)
}

abstract class ControlPacket extends Packet with Control

case class StreamBegin() extends ControlPacket {

  def ctrlType:Short = ControlTypes.STREAM_BEGIN

  override def serialize(serializer: Serializer):Serializer = {
    super.serialize(serializer)
    serializer.writeInt(0)
  }
}

case class ClientPing(time:Int) extends ControlPacket {

  def ctrlType:Short = ControlTypes.PING_CLIENT

  override def serialize(serializer: Serializer):Serializer = {
    super.serialize(serializer)
    serializer.writeInt(time)
  }
}

/**
 * Client specified size of the play buffer ( in milliseconds )
 *
 * @param streamId    Stream ID
 * @param lengthMs    Buffer length ( milliseconds )
 */
case class ClientBuffer(streamId:Int, lengthMs:Int) extends ControlPacket {

  def ctrlType:Short = ControlTypes.CLIENT_BUFFER

  override def serialize(serializer: Serializer):Serializer = {
    super.serialize(serializer)
    serializer.writeInt(streamId)
    serializer.writeInt(lengthMs)
  }
}