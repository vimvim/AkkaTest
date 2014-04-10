package rtmp.amf.amf0

import scala.collection.mutable
import scala.collection.immutable.HashMap

import akka.util.ByteStringBuilder

import rtmp.amf._
import scala.Some

/**
 * Serialize objects into AMF0
 */
class Amf0Serializer(builder:ByteStringBuilder) extends Serializer(builder) {

  private def writers = HashMap[Class[_], AmfObjectWriter[_]](
    (classOf[String], new StringWriter()),
    (classOf[java.lang.String], new StringWriter()),
    (classOf[Boolean], new BooleanWriter()),
    (classOf[java.lang.Boolean], new BooleanWriter()),
    (classOf[Double], new DoubleWriter()),
    (classOf[Integer], new IntegerWriter()),
    (classOf[Int], new IntegerWriter()),
    (classOf[Map[String,Any]], new ObjectWriter(this)),
    (classOf[AmfMixedList], new MixedArrayWriter(this)),
    (classOf[AmfMixedMap], new MixedArrayWriter(this))
  )

  override def writeNull(): Unit = builder.putByte(Amf0Types.TYPE_NULL)

  override def writeEndObject(): Unit = builder.putByte(Amf0Types.TYPE_END_OF_OBJECT)

  override protected def getObjectWriter[T](cls: Class[T]): Option[AmfObjectWriter[T]] = {

    writers.get(cls) match {
      case Some(writer) => Some(writer.asInstanceOf[AmfObjectWriter[T]])
      case None => None
    }
  }
}
