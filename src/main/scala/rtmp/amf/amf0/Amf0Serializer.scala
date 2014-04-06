package rtmp.amf.amf0

import scala.collection.mutable
import scala.collection.immutable.HashMap

import akka.util.ByteStringBuilder

import rtmp.amf.{AmfObjectWriter, Serializer}

/**
 * Serialize objects into AMF0
 */
class Amf0Serializer(builder:ByteStringBuilder) extends Serializer(builder) {

  private def writers = HashMap[Class[_], AmfObjectWriter[_]](
    (classOf[String], new StringWriter()),
    (classOf[Boolean], new BooleanWriter())
  )

  override protected def getObjectWriter[T](cls: Class[T]): AmfObjectWriter[T] = {

    writers.get(cls) match {
      case Some(writer) => writer.asInstanceOf[AmfObjectWriter[T]]
      case None => throw new Exception("Unable to get object writer for class: "+cls)
    }
  }

  override def writeNull(): Unit = {

  }
}
