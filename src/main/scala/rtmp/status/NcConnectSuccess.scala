package rtmp.status

import rtmp.amf.{AmfMixedArray, AmfMixedMap, Serializer}

/**
 *
 */
class NcConnectSuccess(val fmsVer:String, val capabilities:Int, val mode:Int, val data:AmfMixedArray) extends
        Status(true, "NetConnection.Connect.Success", "status", "Connection succeeded.") {

  override def serialize(serializer: Serializer): Unit = {

    super.serialize(serializer)

    serializer.writeObject("data")
    serializer.writeObject(data)

    serializer.writeObject("capabilities")
    serializer.writeObject(capabilities)

    serializer.writeObject("fmsVer")
    serializer.writeObject(fmsVer)

    serializer.writeObject("mode")
    serializer.writeObject(mode)
  }
}
