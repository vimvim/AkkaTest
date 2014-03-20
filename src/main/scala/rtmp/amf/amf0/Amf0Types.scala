package rtmp.amf.amf0


/**
 *
 */
object Amf0Types {
  /**
   * Number marker constant
   */
  final val TYPE_NUMBER: Byte = 0x00
  /**
   * Boolean value marker constant
   */
  final val TYPE_BOOLEAN: Byte = 0x01
  /**
   * String marker constant
   */
  final val TYPE_STRING: Byte = 0x02
  /**
   * Object marker constant
   */
  final val TYPE_OBJECT: Byte = 0x03
  /**
   * MovieClip marker constant
   */
  final val TYPE_MOVIECLIP: Byte = 0x04
  /**
   * Null marker constant
   */
  final val TYPE_NULL: Byte = 0x05
  /**
   * Undefined marker constant
   */
  final val TYPE_UNDEFINED: Byte = 0x06
  /**
   * Object reference marker constant
   */
  final val TYPE_REFERENCE: Byte = 0x07
  /**
   * Mixed array marker constant
   */
  final val TYPE_MIXED_ARRAY: Byte = 0x08
  /**
   * End of object marker constant
   */
  final val TYPE_END_OF_OBJECT: Byte = 0x09
  /**
   * Array marker constant
   */
  final val TYPE_ARRAY: Byte = 0x0A
  /**
   * Date marker constant
   */
  final val TYPE_DATE: Byte = 0x0B
  /**
   * Long string marker constant
   */
  final val TYPE_LONG_STRING: Byte = 0x0C
  /**
   * Unsupported type marker constant
   */
  final val TYPE_UNSUPPORTED: Byte = 0x0D
  /**
   * Recordset marker constant
   */
  final val TYPE_RECORDSET: Byte = 0x0E
  /**
   * XML marker constant
   */
  final val TYPE_XML: Byte = 0x0F
  /**
   * Class marker constant
   */
  final val TYPE_CLASS_OBJECT: Byte = 0x10
  /**
   * Object marker constant (for AMF3)
   */
  final val TYPE_AMF3_OBJECT: Byte = 0x11

}
