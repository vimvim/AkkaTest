
import org.scalatest.FlatSpec
import org.scalatest.matchers.ClassicMatchers

import akka.util.ByteString

import rtmp.amf.amf0.{Amf0Deserializer, Amf0Serializer}
import rtmp.amf.{AmfNull, AMF0Encoding}
import rtmp.packet.{Invoke, InvokeDecoder}

/**
 * Testing of the Amf0 Serialization/Deserialization
 */
class Amf0Spec extends FlatSpec with ClassicMatchers {

  "A serialized values" should "match to unserialized" in {

    val builder = ByteString.newBuilder
    val serializer = new Amf0Serializer(builder)

    serializer.writeObject("Test string")
    serializer.writeObject(123)
    serializer.writeObject(true)
    serializer.writeObject(false)
    serializer.writeNull()

    val data = builder.result()
    val dataItr = data.iterator

    val deserializer = new Amf0Deserializer(dataItr)
    val values = deserializer.readAll

    assert(values.equals(List(
      "Test string",
      123,
      true,
      false,
      null
    )))
  }
}
