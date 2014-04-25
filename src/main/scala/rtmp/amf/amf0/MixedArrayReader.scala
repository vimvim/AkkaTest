package rtmp.amf.amf0

import rtmp.amf._
import akka.util.{ByteIterator, ByteStringBuilder}
import java.nio.ByteOrder
import scala.Some

/**
 * Reader of the AMF mixed array
 */
class MixedArrayReader(deserializer:Deserializer) extends AmfObjectReader {

  override def read(typeId: Byte, bufferItr: ByteIterator): Any = {

    val maxKey = bufferItr.getInt(ByteOrder.BIG_ENDIAN)

    val properties = deserializer.readAllProperties

    // Will check if there is are some non numeric keys exist
    properties.find((entry) => !(entry._1 forall Character.isDigit)) match {

      case Some(c)=>
        makeMap(properties)

      case None=>
        // There is no non string keys.
        if (properties.size<=maxKey+1) {
          makeList(properties)
        } else {
          makeMap(properties)
        }
    }
  }

  private def makeList(properties:Map[String,Any]):AmfMixedList = {

    val orderedProperties = properties.toSeq.map((pair)=>(pair._1.toInt, pair._2)).sortBy(_._1)
    val values = orderedProperties.foldLeft(List[Any]())((list, pair)=>{
      pair._1 :: list
    }).reverse

    new AmfMixedList(values)
  }

  private def makeMap(properties:Map[String,Any]):AmfMixedMap = {
    new AmfMixedMap(properties)
  }
}
