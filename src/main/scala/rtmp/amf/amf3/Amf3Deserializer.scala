package rtmp.amf.amf3

import scala.collection.mutable.LinkedList
import scala.collection.mutable
import scala.collection.immutable.HashMap

import akka.util.ByteIterator

import rtmp.amf.{AmfObjectReader, Deserializer}
import rtmp.amf.amf0.{Amf0Deserializer, DoubleReader, NullReader}


/**
 * AMF3 deserializer
 */
class Amf3Deserializer(bufferItr:ByteIterator) extends Amf0Deserializer(bufferItr) {

  val stringsRefs = new mutable.MutableList[String]()

  // Will force readString and readInteger to AMF0
  var forceAmf0:Boolean = false

  private def readers = HashMap[Int, AmfObjectReader](
    (Amf3Types.TYPE_UNDEFINED, new NullReader()),
    (Amf3Types.TYPE_INTEGER, new IntegerReader()),
    (Amf3Types.TYPE_NUMBER, new DoubleReader()),
    (Amf3Types.TYPE_STRING, new StringReader(stringsRefs)),
    (Amf3Types.TYPE_BOOLEAN_TRUE, new BooleanReader()),
    (Amf3Types.TYPE_BOOLEAN_FALSE, new BooleanReader())
  )

  override protected def getObjectReader(typeId: Byte): AmfObjectReader = {

    if (forceAmf0) {
      super.getObjectReader(typeId)
    }

    readers.get(typeId) match {
      case Some(reader) => reader
      case None => super.getObjectReader(typeId)
    }
  }

}
