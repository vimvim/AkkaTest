
import akka.util.{ByteString, CompactByteString}
import java.io.{FileInputStream, FileNotFoundException, File}

import rtmp.amf.AMF0Encoding
import rtmp.packet.{Invoke, InvokeDecoder}
import scala.collection.immutable.{ListSet, Iterable}
import scala.concurrent.duration._

import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.concurrent._
import org.scalatest._
import org.scalatest.matchers.{ClassicMatchers, Matchers, ShouldMatchers}

import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }

import rtmp.amf.amf0.Amf0Deserializer

/**
 * Testing AMF parsing
 */
class ParseAmfSpec extends FlatSpec with ClassicMatchers {

  "A parsed data in packet_invoke_connect_1.rtmp" should "match to test data" in {

    val binaryData = readData("packet_invoke_connect_1.rtmp")
    val decoder = new InvokeDecoder(new DummyLogger())
    val packet = decoder.decode(new AMF0Encoding(), binaryData)

    assert(packet.equals(Invoke("connect", 1, ListSet(Map(
      "app" -> "live",
      "type" -> "nonprivate",
      "flashVer" -> "FMLE/3.0 (compatible; Lavf55.2.0)",
      "tcUrl" -> "rtmp://127.0.0.1:1935/live"
    )))))
  }

  "A parsed data in packet_invoke_releaseStream_2.rtmp" should "match to test data" in {

    val binaryData = readData("packet_invoke_releaseStream_2.rtmp")
    val decoder = new InvokeDecoder(new DummyLogger())
    val packet = decoder.decode(new AMF0Encoding(), binaryData)

    assert(packet.equals(Invoke("connect", 1, ListSet(Map(
      "app" -> "live",
      "type" -> "nonprivate",
      "flashVer" -> "FMLE/3.0 (compatible; Lavf55.2.0)",
      "tcUrl" -> "rtmp://127.0.0.1:1935/live"
    )))))
  }

  private def readData(fileName:String):ByteString = {
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
