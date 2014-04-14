package rtmp.status

import rtmp.amf.{Serializer, CustomSerializable}

/**
 * Describes operation status.
 */
class Status(val success:Boolean, val code:String, val level:String, val description:String, val application:Option[Any] = None) extends CustomSerializable {

  override def serialize(serializer: Serializer): Unit = {

    serializer.writeObject("level")
    serializer.writeObject(level)

    serializer.writeObject("code")
    serializer.writeObject(code)

    serializer.writeObject("description")
    serializer.writeObject(description)

    application match {
      case Some(app) =>
        serializer.writeObject("application")
        serializer.writeObject(app)
    }
  }
}
