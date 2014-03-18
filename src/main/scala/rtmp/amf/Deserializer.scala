package rtmp.amf

import scala.collection.immutable.List

import akka.util.ByteIterator

/**
 *
 */
abstract class Deserializer(bufferItr:ByteIterator) extends Core {

  def readDataType:Byte

  def readString:String

  def readInteger:Int

  def readSomething:Object

  def hasSomething:Boolean = bufferItr.hasNext

  /**
   * Read all objects
   *
   * @return
   */
  def readAll:List[_] = {

    def readNextObject(params:List[_]) = {
      if (hasSomething) {
        // params.::(readSomething)

      } else {
        objects
      }
    }

  }

}
