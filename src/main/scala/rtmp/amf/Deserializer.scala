package rtmp.amf

import scala.collection.immutable.List
import scala.collection.immutable.ListSet

import akka.util.ByteIterator

/**
 *
 */
abstract class Deserializer(bufferItr:ByteIterator) extends Core {

  def readObject[T]:T = {
    val obj = readSomething
    obj.asInstanceOf[T]
  }

  def readSomething:Any = {

    val typeId = readTypeID
    val objectReader = getObjectReader(typeId)

    objectReader.read(typeId, bufferItr)
  }

  def hasSomething:Boolean = bufferItr.hasNext

  /**
   * Read all objects
   *
   * @return
   */
  def readAll:ListSet[Any] = {

    def readNextObject(params:ListSet[Any]):ListSet[Any] = {

      if (hasSomething) {
        // params.::(readSomething)
        params + readSomething
      } else {
        params
      }
    }

    readNextObject(new ListSet[Any]())
  }

  protected def getObjectReader(typeId:Byte):ObjectReader

  private def readTypeID:Byte = bufferItr.getByte

}
