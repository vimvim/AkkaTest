package rtmp.tests

import java.security.{PrivateKey, PublicKey, KeyFactory, KeyPair}
import java.io.{FileNotFoundException, FileInputStream, File}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

import akka.util.{ByteStringBuilder, ByteIterator, CompactByteString, ByteString}

import rtmp.HandshakeDataProvider


/**
 * Test stream provider
 * TODO: Obsolote, replaced by HandshakeV4Test - remove
 */
class TestDataSource(path:String) extends HandshakeDataProvider {

  var inputIdx = 1
  var outputIdx = 1

  var outputDataItr:Option[ByteIterator] = None

  def getKeyPair: KeyPair = {

    val encodedPublicKey = readBytes("public.key")
    val encodedPrivateKey = readBytes("private.key")

    val keyFactory: KeyFactory = KeyFactory.getInstance("DH")

    val publicKeySpec: X509EncodedKeySpec = new X509EncodedKeySpec(encodedPublicKey)
    val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)

    val privateKeySpec: PKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey)
    val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)

    new KeyPair(publicKey, privateKey)
  }

  def getRand1: Array[Byte] = {
    readBytes("hrand1.rtmp")
  }

  def getRand2: Array[Byte] = {
    readBytes("hrand2.rtmp")
  }

  def getInputChunk:Option[ByteString] = {

    try {

      val bytes = readPacket("in", inputIdx)
      inputIdx = inputIdx + 1

      Some(CompactByteString(bytes))

    } catch {
      case ex:FileNotFoundException => None
    }
  }

  def getOutputChunk(size:Int):ByteString = {

    def readChunk(builder:ByteStringBuilder, dataItrHolder:Option[ByteIterator]):Option[ByteIterator] = {

      if (builder.length==size) {
        // Builder have enough data

        dataItrHolder
      } else {
        // Builder is not have enough data

        dataItrHolder match {

          case Some(dataItr) =>
            // Some data already read



            val toRead = if (dataItr.len>=size) size else dataItr.len

            val chunk = new Array[Byte](toRead)
            dataItr.getBytes(chunk)

            builder.append(CompactByteString(chunk))

            if (dataItr.len==0) {
              readChunk(builder, None)
            } else {
              readChunk(builder, Some(dataItr))
            }

          case None =>
            // Needs to read next data chunk

            val chunk = CompactByteString(readPacket("out", outputIdx))
            outputIdx = outputIdx + 1

            readChunk(builder, Some(chunk.iterator))
        }
      }
    }

    val builder = ByteString.newBuilder
    outputDataItr = readChunk(builder, outputDataItr)

    builder.result()
  }

  def readPacket(direction:String, sqNum:Int):Array[Byte] = {
    readBytes(s"${direction}_$sqNum.rtmp")
  }

  protected def readBytes(fileName:String):Array[Byte] = {

    val file: File = new File(path + "/"+fileName)
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
