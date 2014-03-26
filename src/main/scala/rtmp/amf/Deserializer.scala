package rtmp.amf

import scala.collection.immutable.List
import scala.collection.immutable.ListSet

import akka.util.ByteIterator
import scala.reflect._

/**
 *
 */
abstract class Deserializer(bufferItr:ByteIterator) extends Core {

  def readObject[T: ClassTag]:T = {

    val cs = implicitly[ClassTag[T]]

    val obj = readSomething
    if (cs.equals(Manifest.Int) && obj.isInstanceOf[Double]) {
      obj.asInstanceOf[Double].toInt.asInstanceOf[T]
    } else {
      obj.asInstanceOf[T]
    }
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
        readNextObject(params + readSomething)
      } else {
        params
      }
    }

    readNextObject(new ListSet[Any]())
  }

  protected def getObjectReader(typeId:Byte):AmfObjectReader

  private def readTypeID:Byte = bufferItr.getByte

}
