package rtmp.packet

import scala.collection.immutable.ListSet
import akka.util.ByteString
import rtmp.amf.Serializer
import rtmp.status.Status

/**
 *
 */
sealed trait Packet {
  def serialize(serializer:Serializer)
}

case class Notify(action:String, params:List[Any]) extends Packet {

  override def serialize(serializer: Serializer): Unit = {

  }
}

case class Invoke(action:String, invokeID:Int, params:List[Any]) extends Packet {

  override def serialize(serializer: Serializer): Unit = {

  }
}

case class InvokeResponse(invoke:Invoke, status:Status) extends Packet {

  override def serialize(serializer: Serializer): Unit = {

    if (status.success) serializer.writeObject("_result") else serializer.writeObject("_error")

    serializer.writeObject(invoke.invokeID)

    // TODO: Conn parameters. What is this ??
    serializer.writeObject(null)

    serializer.writeObject(status)
  }
}

case class Video(data:ByteString) extends Packet {

  override def serialize(serializer: Serializer): Unit = {

  }
}

case class Audio(data:ByteString) extends Packet {

  override def serialize(serializer: Serializer): Unit = {

  }
}
