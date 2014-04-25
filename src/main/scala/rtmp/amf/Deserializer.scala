package rtmp.amf

import scala.collection.immutable.List
import scala.collection.immutable.ListSet

import akka.util.ByteIterator
import scala.reflect._
import rtmp.amf.amf0.Amf0StringReader

/**
 * Generic AMF deserializer
 */
abstract class Deserializer(bufferItr:ByteIterator) extends Core with Amf0StringReader {

  /**
   * Read object of the some type and try convert to the specified
   *
   * @tparam T
   * @return
   */
  def readObject[T: ClassTag]:T = {

    val cs = implicitly[ClassTag[T]]

    val obj = readSomething
    if (cs.equals(Manifest.Int) && obj.isInstanceOf[Double]) {
      obj.asInstanceOf[Double].toInt.asInstanceOf[T]
    } else {
      obj.asInstanceOf[T]
    }
  }

  /**
   * Read object of the some type
   *
   * @return
   */
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

  /**
   * Read all pairs of the name->value
   *
   * @return
   */
  def readAllProperties:Map[String, Any] = {

    def readPropertiesInternal(properties:Map[String, Any]):Map[String, Any] = {

      if (hasSomething) {

        val property = readProperty

        property._2 match {
          case AmfObjectEnd() => properties
          case _ => readPropertiesInternal(properties+property)
        }

      } else {
        properties
      }
    }

    readPropertiesInternal(Map[String, Any]())
  }

  /**
   * Read single name->value pair
   *
   * @return
   */
  def readProperty:(String, Any) = (readString(bufferItr), readSomething)

  protected def getObjectReader(typeId:Byte):AmfObjectReader

  private def readTypeID:Byte = bufferItr.getByte

}
