package rtmp.amf.amf3

/**
 *
 */
object Amf3Types {

  /**
   * Undefined marker
   */
  final val TYPE_UNDEFINED: Byte = 0x00
  /**
   * Null marker
   */
  final val TYPE_NULL: Byte = 0x01
  /**
   * Boolean false marker
   */
  final val TYPE_BOOLEAN_FALSE: Byte = 0x02
  /**
   * Boolean true marker
   */
  final val TYPE_BOOLEAN_TRUE: Byte = 0x03
  /**
   * Integer marker
   */
  final val TYPE_INTEGER: Byte = 0x04
  /**
   * Number / Double marker
   */
  final val TYPE_NUMBER: Byte = 0x05
  /**
   * String marker
   */
  final val TYPE_STRING: Byte = 0x06
  /**
   * XML document marker
   * <br />
   * This is for the legacy XMLDocument type is retained in the language
   * as flash.xml.XMLDocument. Similar to AMF 0, the structure of an
   * XMLDocument needs to be flattened into a string representation for
   * serialization. As with other strings in AMF, the content is encoded in
   * UTF-8.
   * XMLDocuments can be sent as a reference to a previously occurring
   * XMLDocument instance by using an index to the implicit object reference
   * table.
   */
  final val TYPE_XML_DOCUMENT: Byte = 0x07
  /**
   * Date marker
   */
  final val TYPE_DATE: Byte = 0x08
  /**
   * Array start marker
   */
  final val TYPE_ARRAY: Byte = 0x09
  /**
   * Object start marker
   */
  final val TYPE_OBJECT: Byte = 0x0A
  /**
   * XML start marker
   */
  final val TYPE_XML: Byte = 0x0B
  /**
   * ByteArray marker
   */
  final val TYPE_BYTEARRAY: Byte = 0x0C
  /**
   * Vector<int> marker
   */
  final val TYPE_VECTOR_INT: Byte = 0x0D
  /**
   * Vector<uint> marker
   */
  final val TYPE_VECTOR_UINT: Byte = 0x0E
  /**
   * Vector<Number> marker
   */
  final val TYPE_VECTOR_NUMBER: Byte = 0x0F
  /**
   * Vector<Object> marker
   */
  final val TYPE_VECTOR_OBJECT: Byte = 0x10
  /**
   * Dictionary
   */
  final val TYPE_DICTIONARY: Byte = 0x11
  /**
   * Property list encoding.
   *
   * The remaining integer-data represents the number of class members
   * that exist. The property names are read as string-data. The values
   * are then read as AMF3-data.
   */
  final val TYPE_OBJECT_PROPERTY: Byte = 0x00
  /**
   * Externalizable object.
   *
   * What follows is the value of the "inner" object, including type code.
   * This value appears for objects that implement IExternalizable, such
   * as ArrayCollection and ObjectProxy.
   */
  final val TYPE_OBJECT_EXTERNALIZABLE: Byte = 0x01
  /**
   * Name-value encoding.
   *
   * The property names and values are encoded as string-data followed by
   * AMF3-data until there is an empty string property name. If there is
   * a class-def reference there are no property names and the number of
   * values is equal to the number of properties in the class-def.
   */
  final val TYPE_OBJECT_VALUE: Byte = 0x02
  /**
   * Proxy object.
   */
  final val TYPE_OBJECT_PROXY: Byte = 0x03


}
