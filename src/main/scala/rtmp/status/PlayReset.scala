package rtmp.status

import rtmp.amf.Serializer

/**
 * Created by vim on 5/23/14.
 */
class PlayReset(val streamName:String, val clientID:Int) extends Status(true, "NetStream.Play.Reset", "status", s"Playing and resetting $streamName.") {

  override def serialize(serializer: Serializer): Unit = {

    super.serialize(serializer)

    serializer.writeProperty("details", streamName)
    serializer.writeProperty("clientid", clientID)
  }
}

