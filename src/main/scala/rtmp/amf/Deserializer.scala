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
  def readAll:List[Any] = {

    def readNextObject(params:List[Any]):List[Any] = {

      if (hasSomething) {
        // params.::(readSomething)
        readNextObject(readSomething :: params)
      } else {
        params
      }
    }

    readNextObject(List[Any]()).reverse
  }

  protected def getObjectReader(typeId:Byte):AmfObjectReader

  private def readTypeID:Byte = bufferItr.getByte

}
