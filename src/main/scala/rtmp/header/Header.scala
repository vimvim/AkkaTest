package rtmp.header

import akka.util.ByteStringBuilder

sealed trait Header {

  def streamID:Int

  def serialize(builder:ByteStringBuilder)
}

case class BasicHeader(streamID:Int) extends Header {

  /**
   * Encode basic header ( 0x03 )
   * Fields: stream id
   *
   * @param builder
   */
  override def serialize(builder: ByteStringBuilder): Unit = {

  }
}

case class ExtendedBasicHeader(streamID:Int, timeDelta:Int, extendedTimeDelta:Int) extends Header {

  /**
   * Encode basic header with timestamp ( 0x02 )
   * Fields to decode: stream id, time delta
   *
   * @param builder
   */
  override def serialize(builder: ByteStringBuilder): Unit = {

  }
}

case class FullHeader(streamID:Int, timestamp:Int, extendedTime:Int, length:Int, typeID:Byte, messageSID:Int) extends Header {

  /**
   * Encode full header ( 0x00 )
   * Fields to decode: stream id, timestamp, length, type id, message stream id
   *
   * TODO: For decoding details see RTMPProtocolDecoder:448
   *
   * @param builder
   */
  override def serialize(builder: ByteStringBuilder): Unit = {

  }
}

case class ShortHeader(streamID:Int, timeDelta:Int, extendedTimeDelta:Int, length:Int, typeID:Byte) extends Header {

  /**
   * Encode full header without message id ( 0x01 )
   * Fields to decode: stream id, time delta, length, type id
   *
   * @param builder
   */
  override def serialize(builder: ByteStringBuilder): Unit = {

  }
}

