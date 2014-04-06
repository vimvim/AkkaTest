
import akka.util.ByteString
import java.io.{FileInputStream, FileNotFoundException, File}

/**
 * Common trait for tests depends on the some binary data from external dump files.
 */
trait BinaryTester {

  protected def readData(fileName:String):ByteString = {
    ByteString.fromArray(readBytes(fileName))
  }

  protected def readBytes(fileName:String):Array[Byte] = {

    val file: File = new File("dump/"+fileName)
    if (!file.exists()) {
      throw new FileNotFoundException(fileName)
    }

    val fis: FileInputStream = new FileInputStream(file)
    val data: Array[Byte] = new Array[Byte](file.length.asInstanceOf[Int])
    fis.read(data)
    fis.close()

    data
  }

}
