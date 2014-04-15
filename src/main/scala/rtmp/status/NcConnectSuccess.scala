package rtmp.status

import rtmp.amf.{AmfMixedArray, Serializer}

/**
 *
 */
class NcConnectSuccess(val fmsVer:String, val capabilities:Int, val mode:Int, val data:AmfMixedArray) extends
        Status(true, "NetConnection.Connect.Success", "status", "Connection succeeded.") {

  override def serialize(serializer: Serializer): Unit = {

    super.serialize(serializer)

    serializer.writeProperty("data", data)
    serializer.writeProperty("capabilities", capabilities)
    serializer.writeProperty("fmsVer", fmsVer)
    serializer.writeProperty("mode", mode)
  }
}
