package rtmp.amf.amf0

import scala.collection.mutable

/**
 * Store references to the objects created during deserialization.
 */
class ObjectReferences {

  var idx = 1
  var references = new mutable.HashMap[Int, AnyRef]()

  def storeReference(obj:AnyRef) = {
    references.put(idx, obj)
    idx = idx + 1
  }

}
