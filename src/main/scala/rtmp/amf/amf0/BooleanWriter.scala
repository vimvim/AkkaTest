package rtmp.amf.amf0

import akka.util.ByteStringBuilder

import rtmp.amf.AmfObjectWriter

/**
 * AMF0 boolean writer
 */
class BooleanWriter extends AmfObjectWriter[Boolean] {

  override def write(builder: ByteStringBuilder, obj: Boolean): Unit = {



  }
}
