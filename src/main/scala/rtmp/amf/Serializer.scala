package rtmp.amf

import scala.reflect._

import akka.util.ByteStringBuilder

/**
 * Serialize object into AMF
 */
abstract class Serializer(builder:ByteStringBuilder) {

  // def writeNull()

  def writeEndObject()

  def writeObject[T: ClassTag](obj:T) = {

    obj match {

      case customSerializable: CustomSerializable => getCustomWriter.write(builder, customSerializable)

      case null =>
        writeObjectInternal(classOf[Null])( (writer) =>{
          writer.write(builder, obj.asInstanceOf[Null])
        })

      case _ =>
        val cs = implicitly[ClassTag[T]]
        val objClass = selectPreciseType(cs, obj)

        writeObjectInternal(objClass)((writer) =>{
          writer.write(builder, obj)
        })
    }
  }

  protected def writeObjectInternal[T](objClass:Class[T])(writeFunc: (AmfObjectWriter[T]) =>Unit) = {

    getObjectWriter(objClass.asInstanceOf[Class[T]]) match {
      case Some(writer) => writeFunc(writer)
      case None => throw new Exception("Unable to get object writer for class: "+objClass)
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
