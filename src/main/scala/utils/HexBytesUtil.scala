
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
   * @return
   */
  def arraysVisualMatch(bytes1: Array[Byte], bytes1Title:String, bytes2: Array[Byte], bytes2Title:String): String = {

    /**
     * Class used for storing of the pairs from both arrays.
     * @param byte1   Byte from bytes1
     * @param byte2   Byte from bytes2
     * @param char1   Byte represented as char from bytes1
     * @param char2   Byte represented as char from bytes2
     */
    case class Pair(byte1:String, byte2:String, char1:String, char2:String)

    def doubleFold(list:List[Pair], bytes1: Array[Byte], bytes2: Array[Byte]):List[Pair] = {

      if (bytes1.isEmpty && bytes2.isEmpty) {

        list.reverse
      } else {

        def getNumber(bytes:Array[Byte]):String = {
          if (bytes.isEmpty) {
            "  "
          } else {
            "%02x".format(bytes.head)
          }
        }

        def getChar(bytes:Array[Byte]):String = {
          if (bytes.isEmpty || (bytes.head<=32)) {
            "  "
          } else {
            bytes.head.toChar.toString
          }
        }

        def moveHead(bytes:Array[Byte]) = {
          if (bytes.isEmpty) {
            bytes
          } else {
            bytes.tail
          }
        }

        val pair = Pair(getNumber(bytes1), getNumber(bytes2), getChar(bytes1), getChar(bytes2))
        doubleFold(pair::list, moveHead(bytes1), moveHead(bytes2))
      }
    }

    val pairsList = doubleFold(List[Pair](), bytes1, bytes2)

    case class TextState(
      text:String = "",
      line1:String = bytes1Title+" [0000]:",
      line2:String = bytes2Title+" [0000]:",
      line3:String = "",
      pos:Int=0,
      line1Chars:String = "",
      line2Chars:String = ""
    )

    case class FormatterState(
      text:TextState = TextState(),
      pos:Int=0,
      lastMatchPos:Int=0,
      lastMatch:Boolean=true
    )

    val state = pairsList.foldLeft(FormatterState())((state, pair)=>{

      def makeText(text:TextState) = {

        def makeDiffNotes(diffText:String) = {

          if (pair.byte1.equals(pair.byte2) && !state.lastMatch) {
            diffText+" ["+state.lastMatchPos+"-"+state.pos+"]"
          } else {
            diffText
          }
        }

        if (text.pos>=20) {

          TextState(
            text.text+"\r\n"+text.line1+"\t"+text.line1Chars+"\r\n"+text.line2+"\t"+text.line2Chars+(if (text.line3.isEmpty) "" else "\r\nDiff:"+text.line3),
            bytes1Title+" ["+"%04d".format(state.pos)+"]: "+pair.byte1,
            bytes2Title+" ["+"%04d".format(state.pos)+"]: "+pair.byte2,
            makeDiffNotes(""),
            0,
            pair.char1,
            pair.char2
          )
        } else {

          TextState(
            text.text,
            text.line1+" "+pair.byte1,
            text.line2+" "+pair.byte2,
            makeDiffNotes(text.line3),
            text.pos+1,
            text.line1Chars+pair.char1,
            text.line2Chars+pair.char2
          )
        }
      }

      FormatterState(
        makeText(state.text),
        state.pos+1,
        if (pair.byte1.equals(pair.byte2)) state.pos else state.lastMatchPos,
        pair.byte1.equals(pair.byte2)
      )
    })

    state.text.text+"\r\n"+state.text.line1+"\r\n"+state.text.line2+"\r\n"+state.text.line3
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
