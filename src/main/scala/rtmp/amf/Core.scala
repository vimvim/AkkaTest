package rtmp.amf

/**
 *
 */
trait Core {

  /**
   * Padding marker
   */
  final val CORE_SKIP: Byte = 0x00
  /**
   * Null type marker
   */
  final val CORE_NULL: Byte = 0x01
  /**
   * Boolean type marker
   */
  final val CORE_BOOLEAN: Byte = 0x02
  /**
   * Number type marker
   */
  final val CORE_NUMBER: Byte = 0x03
  /**
   * String type marker
   */
  final val CORE_STRING: Byte = 0x04
  /**
   * Date type marker
   */
  final val CORE_DATE: Byte = 0x05
  // Basic stuctures
  /**
   * Array type marker
   */
  final val CORE_ARRAY: Byte = 0x06
  /**
   * Map type marker
   */
  final val CORE_MAP: Byte = 0x07
  /**
   * XML type marker
   */
  final val CORE_XML: Byte = 0x08
  /**
   * Object (Hash) type marker
   */
  final val CORE_OBJECT: Byte = 0x09
  /**
   * ByteArray type marker (AMF3 only)
   */
  final val CORE_BYTEARRAY: Byte = 0x10
  /**
   * Vector type markers
   */
  final val CORE_VECTOR_INT: Byte = 0x0D + 0x30
  final val CORE_VECTOR_UINT: Byte = 0x0E + 0x30
  final val CORE_VECTOR_NUMBER: Byte = 0x0F + 0x30
  final val CORE_VECTOR_OBJECT: Byte = 0x10 + 0x30


}
