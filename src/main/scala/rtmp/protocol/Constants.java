package rtmp.protocol;

/**
 */
public class Constants {

    /**
     * RTMP chunk size constant
     */
    public static final byte TYPE_CHUNK_SIZE = 0x01;

    /**
     * Abort message
     */
    public static final byte TYPE_ABORT = 0x02;

    /**
     * Acknowledgment. Send every x bytes read by both sides.
     */
    public static final byte TYPE_BYTES_READ = 0x03;

    /**
     * Ping is a stream control message, it has sub-types
     */
    public static final byte TYPE_PING = 0x04;

    /**
     * Server (downstream) bandwidth marker
     */
    public static final byte TYPE_SERVER_BANDWIDTH = 0x05;

    /**
     * Client (upstream) bandwidth marker
     */
    public static final byte TYPE_CLIENT_BANDWIDTH = 0x06;

    /**
     * Edge / Origin message.
     */
    public static final byte TYPE_EDGE_ORIGIN = 0x07;

    /**
     * Audio data marker
     */
    public static final byte TYPE_AUDIO_DATA = 0x08;

    /**
     * Video data marker
     */
    public static final byte TYPE_VIDEO_DATA = 0x09;

    // Unknown: 0x0A ...  0x0E

    /**
     * AMF3 stream send
     */
    public static final byte TYPE_FLEX_STREAM_SEND = 0x0F;

    /**
     * AMF3 shared object
     */
    public static final byte TYPE_FLEX_SHARED_OBJECT = 0x10;

    /**
     * AMF3 message
     */
    public static final byte TYPE_FLEX_MESSAGE = 0x11;

    /**
     * Notification is invocation without response
     */
    public static final byte TYPE_NOTIFY = 0x12;

    /**
     * Stream metadata
     */
    public static final byte TYPE_STREAM_METADATA = 0x12;

    /**
     * Shared Object marker
     */
    public static final byte TYPE_SHARED_OBJECT = 0x13;

    /**
     * Invoke operation (remoting call but also used for streaming) marker
     */
    public static final byte TYPE_INVOKE = 0x14;

    /**
     * Aggregate data marker
     */
    public static final byte TYPE_AGGREGATE = 0x16;

}
