
import org.scalatest.FlatSpec
import org.scalatest.matchers.ClassicMatchers

import akka.util.ByteString

import rtmp.amf.amf0.{Amf0Deserializer, Amf0Serializer}
import rtmp.amf.{Serializer, CustomSerializable, AmfNull, AMF0Encoding}
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
    // serializer.writeNull()
    serializer.writeObject(null)

    val data = builder.result()
    val dataItr = data.iterator

    val deserializer = new Amf0Deserializer(dataItr)
    val values = deserializer.readAll

    assert(values.equals(List(
      "Test string",
      123,
      true,
      false,
      AmfNull()
    )))
  }

  "A object supported CustomSerializable" should "be correctly serialized" in {

    class TestObject(val1:String, val2:Integer, val3:Boolean) extends CustomSerializable {

      override def serialize(serializer: Serializer) = {
        serializer.writeObject(val1)
        serializer.writeObject(val2)
        serializer.writeObject(val3)
      }
    }

    val builder = ByteString.newBuilder
    val serializer = new Amf0Serializer(builder)

    val testObject = new TestObject("Test string", 123, true)
    serializer.writeObject(testObject)

    val data = builder.result()
    val dataItr = data.iterator

    val deserializer = new Amf0Deserializer(dataItr)
    val values = deserializer.readAll

    assert(values.equals(List(
      "Test string",
      123,
      true
    )))
  }

  "A serialized AMF object ( Map )" should "be correctly deserialized" in {

    val builder = ByteString.newBuilder
    val serializer = new Amf0Serializer(builder)

    serializer.writeObject(Map[String,Any](
      "prop1" -> "Value 1",
      "prop2" -> 123,
      "prop3" -> false
    ))

    val data = builder.result()
    val dataItr = data.iterator

    val deserializer = new Amf0Deserializer(dataItr)
    val value = deserializer.readSomething

    assert(value.equals(Map[String,Any](
      "prop1" -> "Value 1",
      "prop2" -> 123,
      "prop3" -> false
    )))
  }
}
