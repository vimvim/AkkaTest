package rtmp.packet

/**
 * Types of the control messages
 */
object ControlTypes {
  /**
   * Stream begin / clear event
   */
  final val STREAM_BEGIN: Short = 0
  /**
   * Stream EOF, playback of requested stream is completed.
   */
  final val STREAM_PLAYBUFFER_CLEAR: Short = 1
  /**
   * Stream is empty
   */
  final val STREAM_DRY: Short = 2
  /**
   * Client buffer. Sent by client to indicate its buffer time in milliseconds.
   */
  final val CLIENT_BUFFER: Short = 3
  /**
   * Recorded stream. Sent by server to indicate a recorded stream.
   */
  final val RECORDED_STREAM: Short = 4
  /**
   * One more unknown event
   */
  final val UNKNOWN_5: Short = 5
  /**
   * Client ping event. Sent by server to test if client is reachable.
   */
  final val PING_CLIENT: Short = 6
  /**
   * Server response event. A clients ping response.
   */
  final val PONG_SERVER: Short = 7
  /**
   * One more unknown event
   */
  final val UNKNOWN_8: Short = 8
  /**
   * SWF verification ping 0x001a
   */
  final val PING_SWF_VERIFY: Short = 26
  /**
   * SWF verification pong 0x001b
   */
  final val PONG_SWF_VERIFY: Short = 27
  /**
   * Buffer empty.
   */
  final val BUFFER_EMPTY: Short = 31
  /**
   * Buffer full.
   */
  final val BUFFER_FULL: Short = 32

}
