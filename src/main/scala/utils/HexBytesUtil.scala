
package utils

import akka.util.ByteString

/**
 * This utility implements some helper methods for work with the hex strings.
 *
 */
object HexBytesUtil {

  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => bytes.map("%02x".format(_)).mkString
      case _ => bytes.map("%02x".format(_)).mkString(sep.get)
    }
  }

  /**
   * Create printable representation of the two arrays which is can be used for
   * visually finding differences in the data.
   *
   * @param bytes
   * @param sep
   * @return
   */
  def arraysVisualMatch(bytes1: Array[Byte], bytes1Title:String, bytes2: Array[Byte], bytes2Title:String): String = {

    def doubleFold(list:List[(String,String)], bytes1: Array[Byte], bytes2: Array[Byte]):List[(String,String)] = {

      if (bytes1.isEmpty && bytes2.nonEmpty) {
        list.reverse
      }

      def getNumber(bytes:Array[Byte]):String = {
        if (bytes.isEmpty) {
          "  "
        } else {
          "%02x".format(bytes.head)
        }
      }

      def moveHead(bytes:Array[Byte]) = {
        if (bytes.isEmpty) {
          bytes
        } else {
          bytes.tail
        }
      }

      val pair = (getNumber(bytes1), getNumber(bytes2))
      doubleFold(pair::list, moveHead(bytes1), moveHead(bytes2))
    }

    val pairsList = doubleFold(List[(String,String)](), bytes1, bytes2)

    case class FormatterState(data:String="", pos:Int=0, lastMatch:Int=0, linePos:Int=0)

    pairsList.foldLeft(FormatterState)((pair:(String,String))=>{

    })


  }


  /*
  def example {
    val data = "48 65 6C 6C 6F 20 57 6F 72 6C 64 21 21"
    val bytes = hex2bytes(data)
    println(bytes2hex(bytes, Option(" ")))

    val data2 = "48-65-6C-6C-6F-20-57-6F-72-6C-64-21-21"
    val bytes2 = hex2bytes(data2)
    println(bytes2hex(bytes2, Option("-")))

    val data3 = "48656C6C6F20576F726C642121"
    val bytes3 = hex2bytes(data3)
    println(bytes2hex(bytes3))
  }
  */
}
