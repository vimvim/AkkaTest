package rtmp.tests

import akka.util.{CompactByteString, ByteString}

import java.security.{PrivateKey, PublicKey, KeyFactory, KeyPair}
import java.io.{FileNotFoundException, FileInputStream, File}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

import rtmp.HandshakeDataProvider


/**
 * Test stream provider
 */
class TestDataSource(path:String) extends HandshakeDataProvider {

  var inputIdx = 1
  var outputIdx = 1

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
      val
    }

  }

  def getOutputChunk(size:Int):ByteString = {


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
