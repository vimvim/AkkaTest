package rtmp.amf

import scala.reflect._

import akka.util.ByteStringBuilder

/**
 * Serialize object into AMF
 */
abstract class Serializer(builder:ByteStringBuilder) {

  def writeNull()

  def writeEndObject()

  def writeObject[T: ClassTag](obj:T) = {

    val cs = implicitly[ClassTag[T]]
    // val objClass = cs.runtimeClass

    val objClass = selectPreciseType(cs, obj)

    // getObjectWriter(obj.getClass.asInstanceOf[Class[T]]) match {
    getObjectWriter(objClass.asInstanceOf[Class[T]]) match {
      case Some(writer) => writer.write(builder, obj)
      case None =>

        obj match {
          case customSerializable: CustomSerializable => getCustomWriter.write(builder, customSerializable)
          case _ => throw new Exception("Unable to get object writer for class: "+obj.getClass)
        }

        // throw new Exception("Unable to get object writer for class: "+obj.getClass)
    }
  }

  protected def getObjectWriter[T](cls:Class[T]):Option[AmfObjectWriter[T]]

  protected def getCustomWriter:AmfObjectWriter[CustomSerializable] = new CustomWriter(this)

  private def selectPreciseType[T](cs:ClassTag[T], obj:T):Class[T] = {

    if (cs.equals(ClassTag.Any)) {
      obj.getClass.asInstanceOf[Class[T]]
    } else {
      cs.runtimeClass.asInstanceOf[Class[T]]
    }
  }

}
