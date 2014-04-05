package rtmp.tests

import java.io.{FileInputStream, File}
import java.nio.ByteBuffer

/**
 * Base reader for client and server data dumps
 *
 * @param path
 * @param prefix
 */
class StreamDumpReader(path:String, prefix:String, suffix:String, startId:Int) {

  var sqNum = startId

  def readPacket():Array[Byte] = {

    val bytes = readBytes(nextFileName)
    sqNum = sqNum + 1

    bytes
  }

  def readAsBuffer():ByteBuffer = {
    ByteBuffer.wrap(readPacket())
  }

  def haveNext: Boolean = {
    val file: File = new File(path + "/"+nextFileName)
    file.exists()
  }

  protected def readBytes(fileName:String):Array[Byte] = {

    val file: File = new File(path + "/"+fileName)

    val fis: FileInputStream = new FileInputStream(file)
    val data: Array[Byte] = new Array[Byte](file.length.asInstanceOf[Int])
    fis.read(data)
    fis.close()

    data
  }

  protected def nextFileName: String = s"${prefix}_$sqNum.$suffix"
}
