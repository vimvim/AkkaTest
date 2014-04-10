package rtmp.status

import rtmp.amf.{Serializer, CustomSerializable}

/**
 * Describes operation status.
 */
class Status(val success:Boolean, val code:String, val level:String, val description:String, val application:AnyRef = null) extends CustomSerializable {

  override def serialize(serializer: Serializer): Unit = {



  }
}
