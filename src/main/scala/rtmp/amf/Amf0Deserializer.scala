package rtmp.amf

import akka.util.ByteIterator

/**
 * AMF0 deserializer
 */
class Amf0Deserializer(bufferItr:ByteIterator) extends Deserializer(bufferItr) {

  override def readString: String = ???

}
