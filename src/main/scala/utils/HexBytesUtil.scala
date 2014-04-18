
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

    def doubleFold(list:List[(String,String)], bytes1: Array[Byte], bytes2: Array[Byte]):List[(String,String)] = {

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
    }

    val pairsList = doubleFold(List[(String,String)](), bytes1, bytes2)

    case class TextState(
      text:String="",
      line1:String=bytes1Title+" [0]: ",
      line2:String=bytes2Title+" [0]: ",
      line3:String="", pos:Int=0
    )

    case class FormatterState(
      text:TextState = TextState(),
      pos:Int=0,
      lastMatchPos:Int=0,
      lastMatch:Boolean=true
    )

    val state = pairsList.foldLeft(FormatterState())((state, pair:(String,String))=>{

      def makeText(text:TextState) = {

        def makeDiffNotes(diffText:String) = {

          if (pair._1.equals(pair._2) && !state.lastMatch) {
            diffText+" ["+state.lastMatchPos+"-"+state.pos+"]"
          } else {
            diffText
          }

          /*
          if (!pair._1.equals(pair._2)) {

            if (state.lastMatch) {
              diffText+" ["+state.pos+"-"
            } else {
              diffText
            }

          } else {

            if (!state.lastMatch) {
              diffText+"-"+state.pos+"]"
            } else {
              diffText
            }
          }
          */
        }

        if (text.pos>=20) {

          TextState(
            text.text+"\r\n"+text.line1+"\r\n"+text.line2+"\r\n"+text.line3,
            bytes1Title+" ["+state.pos+"]: "+pair._1+" ",
            bytes2Title+" ["+state.pos+"]: "+pair._2+" ",
            makeDiffNotes("Diff: "),
            0
          )
        } else {

          TextState(
            text.text,
            text.line1+" "+pair._1,
            text.line2+" "+pair._2,
            makeDiffNotes(text.line3),
            text.pos+1
          )
        }
      }

      FormatterState(
        makeText(state.text),
        state.pos+1,
        if (pair._1.equals(pair._2)) state.pos else state.lastMatchPos,
        pair._1.equals(pair._2)
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
