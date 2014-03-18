package rtmp.amf

import scala.collection.immutable.List
import scala.collection.immutable.ListSet

import akka.util.ByteIterator

/**
 *
 */
abstract class Deserializer(bufferItr:ByteIterator) extends Core {

  def readDataType:Byte

  def readString:String

  def readInteger:Int

  def readSomething:Any

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

}
