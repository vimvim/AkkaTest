package rtmp.status

import rtmp.amf.{Serializer, CustomSerializable}

/**
 * Describes operation status.
 */
class Status(val success:Boolean,
             val code:String,
             val level:String,
             val description:String,
             val application:Option[Any] = None)
  extends CustomSerializable {

  override def serialize(serializer: Serializer): Unit = {

    serializer.writeProperty("level", level)
    serializer.writeProperty("code", code)
    serializer.writeProperty("description", description)

    application match {
      case Some(app) => serializer.writeProperty("application", app)
      case None =>
    }
  }
}
