package rtmp.amf

import akka.util.ByteStringBuilder

/**
 *
 */
abstract class AmfObjectWriter[T] {

  def write(builder:ByteStringBuilder, obj:T)

}
