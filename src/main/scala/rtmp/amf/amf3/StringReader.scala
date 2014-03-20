package rtmp.amf.amf3

import java.nio.charset.Charset
import java.nio.ByteBuffer

import scala.collection.mutable

import akka.util.ByteIterator

import rtmp.amf.ObjectReader



/**
 * AMF3 String reader
 */
class StringReader(stringsRefs:mutable.MutableList[String]) extends ObjectReader with Amf3IntegerReader {

  /**
   * UTF-8 is used
   */
  final val CHARSET: Charset = Charset.forName("UTF-8")

  override def read(typeId: Byte, bufferItr: ByteIterator): String = {

    val firstByte = readAmf3Integer(bufferItr)
    if ((firstByte & 1)==0) {
      // Len is index in the string references array
      val idx = firstByte >> 1

      stringsRefs.get(idx) match {
        case Some(string) => string
        case None => throw new Exception("String reference is not found:"+idx)
      }

    } else {

      val len = firstByte >> 1
      val bytes = new Array[Byte](len)

      bufferItr.getBytes(bytes)

      val string = CHARSET.decode(ByteBuffer.wrap(bytes)).toString

      stringsRefs + string

      string
    }
  }
}
