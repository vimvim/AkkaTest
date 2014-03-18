package rtmp.header

sealed trait Header {
  def streamID:Int
}

case class BasicHeader(streamID:Int) extends Header
case class ExtendedBasicHeader(streamID:Int, timeDelta:Int, extendedTimeDelta:Int) extends Header
case class FullHeader(streamID:Int, timestamp:Int, extendedTime:Int, length:Int, typeID:Byte, messageSID:Int) extends Header
case class ShortHeader(streamID:Int, timeDelta:Int, extendedTimeDelta:Int, length:Int, typeID:Byte) extends Header

