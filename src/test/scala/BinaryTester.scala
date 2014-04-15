
import akka.util.ByteString
import java.io.{FileInputStream, FileNotFoundException, File}
import org.scalatest.FlatSpec
import utils.HexBytesUtil

/**
 * Common trait for tests depends on the some binary data from external dump files.
 */
trait BinaryTester extends FlatSpec {

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

  protected def compare(data:ByteString, testDump:String) = {

    val binaryData = readData(testDump)
    if (!binaryData.equals(data)) {
      // TODO: Print both dumps and raise exception
      // log.debug("Public key bytes:{} ", HexBytesUtil.bytes2hex(publicKey))
      info("Original dump:"+HexBytesUtil.bytes2hex(binaryData.toArray))
      info("Produced dump:"+HexBytesUtil.bytes2hex(data.toArray))
    }
  }

}
