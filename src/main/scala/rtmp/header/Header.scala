package rtmp.header

import java.nio.ByteOrder
import akka.util.ByteStringBuilder

sealed trait Header {

  def streamID:Int

  def serialize(builder:ByteStringBuilder)

  def encodeFirstBytes(builder:ByteStringBuilder, headerType:Byte, sid:Int) = {

    if (sid<64) {
      // Store sid together with the header type in the first byte
      builder.putByte( ((headerType << 6) + sid).toByte )
    } else if (sid<320) {
      // Store header type in the first byte and sid in the next
      builder.putByte((headerType << 6).toByte)
      builder.putByte((sid-64).toByte)
    } else {
      // Store header type in the first byte and sid in the next 4 bytes
      builder.putByte((headerType << 6).toByte)
      builder.putInt(sid-64)(ByteOrder.LITTLE_ENDIAN)
    }
  }

  def encodeTime(builder:ByteStringBuilder, time:Int) = {
    builder.putByte( ((time>>16) & 0xff).toByte )
    builder.putByte( ((time>>8) & 0xff).toByte )
    builder.putByte( (time & 0xff ).toByte )
  }

  def encodeExtendedTime(builder:ByteStringBuilder, extendedTime:Int) = {
    builder.putInt(extendedTime)(ByteOrder.BIG_ENDIAN)
  }

  def encodeSize(builder:ByteStringBuilder, size:Int) = {
    builder.putByte( ((size>>16) & 0xff).toByte )
    builder.putByte( ((size>>8) & 0xff).toByte )
    builder.putByte( (size & 0xff ).toByte )
  }

  def encodeType(builder:ByteStringBuilder, dataType:Byte) = {
    builder.putByte(dataType)
  }

  def encodeMessageSID(builder:ByteStringBuilder, messagesSID:Int) = {
    builder.putByte( ((messagesSID>>24) & 0xff).toByte )
    builder.putByte( ((messagesSID>>16) & 0xff).toByte )
    builder.putByte( ((messagesSID>>8) & 0xff).toByte )
    builder.putByte( (messagesSID & 0xff ).toByte )
  }
}

case class BasicHeader(streamID:Int) extends Header {

  /**
   * Encode basic header ( 0x03 )
   * Fields: stream id
   *
   * @param builder
   */
  override def serialize(builder: ByteStringBuilder): Unit = {
    encodeFirstBytes(builder, 0x03, streamID)
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
    encodeFirstBytes(builder, 0x02, streamID)

    //TODO: Check when we needs to encode extendedTimeDelta
    encodeTime(builder, timeDelta)
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

    encodeFirstBytes(builder, 0x01, streamID)
    encodeTime(builder, timeDelta)
    encodeSize(builder, length)
    encodeType(builder, typeID)

    // TODO: Check when we will needs to encode extendedTimeDelta

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

    encodeFirstBytes(builder, 0x00, streamID)
    encodeTime(builder, timestamp)
    encodeSize(builder, length)
    encodeType(builder, typeID)
    encodeMessageSID(builder, messageSID)

    // TODO: Check when we will needs to encode extendedTimeDelta

  }
}
