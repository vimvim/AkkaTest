package rtmp.amf

import scala.reflect._

import akka.util.ByteStringBuilder

/**
 * Serialize object into AMF
 */
abstract class Serializer(builder:ByteStringBuilder) {

  def writeNull()

  def writeObject[T: ClassTag](obj:T) = {

    val cs = implicitly[ClassTag[T]]
    val objClass = cs.runtimeClass

    val writer = getObjectWriter(obj.getClass.asInstanceOf[Class[T]])
    writer.write(builder, obj)
  }

  protected def getObjectWriter[T](cls:Class[T]):AmfObjectWriter[T]
}
