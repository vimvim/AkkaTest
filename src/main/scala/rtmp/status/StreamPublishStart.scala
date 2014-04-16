package rtmp.status

import rtmp.amf.Serializer

/**
 * Stream publish status
 */
class StreamPublishStart(val details:String, val clientID:Int) extends Status(true, "NetStream.Publish.Start", "status", "") {

  override def serialize(serializer: Serializer): Unit = {

    super.serialize(serializer)

    serializer.writeProperty("details", details)
    serializer.writeProperty("clientid", clientID)
  }
}
