package rtmp.amf.amf0

import scala.collection.mutable
import scala.collection.immutable.HashMap

import akka.util.ByteStringBuilder

import rtmp.amf.{CustomSerializable, AmfObjectWriter, Serializer}

/**
 * Serialize objects into AMF0
 */
class Amf0Serializer(builder:ByteStringBuilder) extends Serializer(builder) {

  private def writers = HashMap[Class[_], AmfObjectWriter[_]](
    (classOf[String], new StringWriter()),
    (classOf[Boolean], new BooleanWriter()),
    (classOf[java.lang.Boolean], new BooleanWriter()),
    (classOf[Double], new DoubleWriter()),
    (classOf[Integer], new IntegerWriter())
  )

  override def writeNull(): Unit = builder.putByte(Amf0Types.TYPE_NULL)

  override protected def getObjectWriter[T](cls: Class[T]): Option[AmfObjectWriter[T]] = {

    writers.get(cls) match {
      case Some(writer) => Some(writer.asInstanceOf[AmfObjectWriter[T]])
      case None => None
    }
  }

  override protected def getObjectWriter: AmfObjectWriter[AnyRef] = new ObjectWriter()

}
