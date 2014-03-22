package rtmp.amf.amf0

import scala.collection.immutable.HashMap

import akka.util.ByteIterator

import rtmp.amf.{AmfObjectReader, Deserializer}

/**
 * AMF0 deserializer
 */
class Amf0Deserializer(bufferItr:ByteIterator) extends Deserializer(bufferItr) {

  // References to the objects created during packet deserialization
  val objectRefs = new ObjectReferences()

  private def readers = HashMap[Int, AmfObjectReader](
    (Amf0Types.TYPE_UNDEFINED, new NullReader()),
    (Amf0Types.TYPE_NUMBER, new DoubleReader()),
    (Amf0Types.TYPE_STRING, new StringReader()),
    (Amf0Types.TYPE_BOOLEAN, new BooleanReader()),
    (Amf0Types.TYPE_OBJECT, new ObjectReader(this, objectRefs)),
    (Amf0Types.TYPE_OBJECT, new EndObjectReader())
  )

  override protected def getObjectReader(typeId: Byte): AmfObjectReader = {

    readers.get(typeId) match {
      case Some(reader) => reader
      case None => throw new Exception("Unable to get object reader. typeId: "+typeId)
    }
  }
}
