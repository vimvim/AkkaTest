import org.scalatest.FlatSpec
import org.scalatest.matchers.ClassicMatchers
import rtmp.amf.AmfMixedMap
import rtmp.Message
import rtmp.packet._
import rtmp.packet.ClientBW
import rtmp.packet.InvokeResponse
import rtmp.packet.ServerBW
import rtmp.packet.StreamBegin
import rtmp.status.{PlayStart, PlayReset, NcConnectSuccess}
import rtmp.{Message, OutPacketStream}

/**
 * Test serialization of the packets sent by server during client play media flow.
 */
class PlayResponsesTest extends FlatSpec with ClassicMatchers with BinaryTester {

  override protected def dumpDir: String = "dump/play"

  val outStream2 = new OutPacketStream(2)
  val outStream3 = new OutPacketStream(3)
  val outStream4 = new OutPacketStream(4, 1024)

  "A composed ServerBW packet " should "match to the test data out_2.rtmp" in {
    compare(serializeOut(outStream2.stream(Message(2, 0, 0, 0, ServerBW(10000000)))), "out_2.rtmp")
  }

  "A composed ClientBW packet " should "match to the test data out_3.rtmp" in {
    compare(serializeOut(outStream2.stream(Message(2, 0, 0, 0, ClientBW(10000000, 2)))), "out_3.rtmp")
  }

  "A composed STREAM_BEGIN control packet " should "match to the test data out_4.rtmp" in {
    compare(serializeOut(outStream2.stream(Message(2, 0, 0, 0, StreamBegin()))), "out_4.rtmp")
  }

  "A composed 'connect' invoke response packet " should "match to the test data out_5.rtmp" in {

    compare(serializeOut(outStream3.stream(Message(3, 0, 0, 0,
      InvokeResponse(
        new Invoke("connect", 1, null),
        success = true,
        new NcConnectSuccess(
          "RED5/1,0,2,0",
          31,
          1,
          new AmfMixedMap(Map[String, AnyRef](
            "type" -> "red5",
            "version" -> "4,0,0,1121"
          ))
        )
      )
    ))), "out_5.rtmp")
  }

  "A composed 'createStream' invoke response packet " should "match to the test data out_6.rtmp" in {

    compare(serializeOut(outStream3.stream(Message(3, 0, 0, 0,
      InvokeResponse(
        new Invoke("createStream", 2, null),
        success = true,
        1
      )
    ))), "out_6.rtmp")
  }

  "A composed PING_CLIENT control packet " should "match to the test data out_7.rtmp" in {
    compare(serializeOut(outStream2.stream(Message(2, 0, 0, 0, ClientPing(-1468342610)))), "out_7.rtmp")
  }

  "A composed ChunkSize packet " should "match to the test data out_8.rtmp" in {
    compare(serializeOut(outStream2.stream(Message(2, 0, 0, 0, ChunkSize(1024)))), "out_8.rtmp")
  }

  "A composed RECORDED_STREAM control packet " should "match to the test data out_9.rtmp" in {
    compare(serializeOut(outStream2.stream(Message(2, 0, 0, 0, RecordedStream(1)))), "out_9.rtmp")
  }

  "A composed STREAM_BEGIN control packet " should "match to the test data out_10.rtmp" in {
    compare(serializeOut(outStream2.stream(Message(2, 0, 0, 0, StreamBegin(1)))), "out_10.rtmp")
  }

  "A composed onStatus NetStream.Play.Reset packet " should "match to the test data out_11.rtmp" in {
    compare(serializeOut(outStream4.stream(Message(2, 0, 0, 1, Invoke("onStatus", 0, List(new PlayReset("mp4:test_sd.mp4", 1)))))), "out_11.rtmp")
  }

  "A composed onStatus NetStream.Play.Start packet " should "match to the test data out_12.rtmp" in {
    compare(serializeOut(outStream4.stream(Message(2, 0, 0, 1, Invoke("onStatus", 0, List(new PlayStart("mp4:test_sd.mp4", 1)))))), "out_12.rtmp")
  }

}
