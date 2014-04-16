
package utils

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
