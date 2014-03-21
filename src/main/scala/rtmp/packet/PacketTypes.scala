package rtmp.packet

/**
 * RTMP packet types
 */
object PacketTypes {

  /**
   * RTMP chunk size constant
   */
  final val TYPE_CHUNK_SIZE: Byte = 0x01
  /**
   * Abort message
   */
  final val TYPE_ABORT: Byte = 0x02
  /**
   * Acknowledgment. Send every x bytes read by both sides.
   */
  final val TYPE_BYTES_READ: Byte = 0x03
  /**
   * Ping is a stream control message, it has sub-types
   */
  final val TYPE_PING: Byte = 0x04
  /**
   * Server (downstream) bandwidth marker
   */
  final val TYPE_SERVER_BANDWIDTH: Byte = 0x05
  /**
   * Client (upstream) bandwidth marker
   */
  final val TYPE_CLIENT_BANDWIDTH: Byte = 0x06
  /**
   * Edge / Origin message.
   */
  final val TYPE_EDGE_ORIGIN: Byte = 0x07
  /**
   * Audio data marker
   */
  final val TYPE_AUDIO_DATA: Byte = 0x08
  /**
   * Video data marker
   */
  final val TYPE_VIDEO_DATA: Byte = 0x09
  // Unknown: 0x0A ...  0x0E
  /**
   * AMF3 stream send
   */
  final val TYPE_FLEX_STREAM_SEND: Byte = 0x0F
  /**
   * AMF3 shared object
   */
  final val TYPE_FLEX_SHARED_OBJECT: Byte = 0x10
  /**
   * AMF3 message
   */
  final val TYPE_FLEX_MESSAGE: Byte = 0x11
  /**
   * Notification is invocation without response
   */
  final val TYPE_NOTIFY: Byte = 0x12
  /**
   * Stream metadata
   */
  final val TYPE_STREAM_METADATA: Byte = 0x12
  /**
   * Shared Object marker
   */
  final val TYPE_SHARED_OBJECT: Byte = 0x13
  /**
   * Invoke operation (remoting call but also used for streaming) marker
   */
  final val TYPE_INVOKE: Byte = 0x14
  /**
   * Aggregate data marker
   */
  final val TYPE_AGGREGATE: Byte = 0x16

}
