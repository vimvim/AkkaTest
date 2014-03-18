package rtmp.amf

import akka.util.ByteIterator

/**
 * AMF3 deserializer
 */
class Amf3Deserializer(bufferItr:ByteIterator) extends Deserializer(bufferItr) with Amf3Core {

  // Will force readString and readInteger to AMF0
  var forceAmf0:Boolean = false

  /**
   * Reads the data type
   *
   * @return byte      Data type
   */
  def readDataType: Byte = {

    val typeID = bufferItr.getByte

    // TODO: Move to the map
    typeID match {
        case TYPE_UNDEFINED => CORE_NULL
        case TYPE_NULL => CORE_NULL

        case TYPE_INTEGER => CORE_NUMBER
        case TYPE_NUMBER => CORE_NUMBER   // !!!! This is DOUBLE !!!!

        case TYPE_BOOLEAN_TRUE => CORE_BOOLEAN
        case TYPE_BOOLEAN_FALSE => CORE_BOOLEAN

        case TYPE_STRING => CORE_STRING

        case TYPE_XML => CORE_XML
        case TYPE_XML_DOCUMENT => CORE_XML

        case TYPE_OBJECT => CORE_OBJECT
        case TYPE_ARRAY => CORE_ARRAY

        case TYPE_DATE => CORE_DATE

        case TYPE_BYTEARRAY => CORE_BYTEARRAY

        case TYPE_VECTOR_INT => CORE_VECTOR_INT

        case TYPE_VECTOR_UINT => CORE_VECTOR_UINT

        case TYPE_VECTOR_NUMBER => CORE_VECTOR_NUMBER

        case TYPE_VECTOR_OBJECT => CORE_VECTOR_OBJECT

        case _ =>
          // log.info("Unknown datatype: {}", currentDataType)
          CORE_SKIP

    }

  }

  def readBoolean:Boolean = {

  }

  def readInteger:Int = {
    readAMF3Integer
  }

  def readDouble:Double = {
    bufferItr.getDouble
  }

  def readString:String = {

  }

  /**
   * Parser of AMF3 "compressed" integer data type
   *
   * @return a converted integer value
   */
  def readAMF3Integer: Int = {

    var n: Int = 0
    var b = bufferItr.getByte

    var result: Int = 0
    while ((b & 0x80) != 0 && n < 3) {
      result <<= 7
      result |= (b & 0x7f)
      b = bufferItr.getByte
      n += 1
    }

    if (n < 3) {
      result <<= 7
      result |= b
    } else {
      result <<= 8
      result |= b & 0x0ff
      if ((result & 0x10000000) != 0) {
        result |= 0xe0000000
      }
    }

    result
  }

}
