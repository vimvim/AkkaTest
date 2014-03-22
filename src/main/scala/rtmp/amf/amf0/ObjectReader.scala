package rtmp.amf.amf0

import akka.util.ByteIterator

import rtmp.amf.{AmfObjectEnd, Deserializer, AmfObjectReader}


/**
 * AMF0 Object reader
 */
class ObjectReader(deserializer:Deserializer, objRefs:ObjectReferences) extends AmfObjectReader with Amf0StringReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Map[String, Any] = {
    readProperty(Map(), bufferItr)
  }

  private def readProperty(properties:Map[String, Any], bufferItr: ByteIterator):Map[String, Any] = {

    val name = readString(bufferItr)
    val obj = deserializer.readSomething

    obj match {
      case AmfObjectEnd() => properties
      case _ => readProperty(properties ++ Map(name -> obj), bufferItr)
    }
  }
}
