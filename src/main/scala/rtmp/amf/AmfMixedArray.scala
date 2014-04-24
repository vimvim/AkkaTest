package rtmp.amf

/**
 * Represents AMF Mixed array
 */
abstract class AmfMixedArray {

  /**
   * Max value of the int key. -1 if there is no int keys.
   * @return
   */
  def maxKey:Int

  /**
   * Iterate over all array entries
   * @param f     Function which will be called for every entry
   */
  def iterateEntries(f:(String, Any)=>Unit)
}

case class AmfMixedList(array:List[Any] = List[Any]()) extends AmfMixedArray {

  override def iterateEntries(f: (String, Any) => Unit): Unit = {
    array.foldLeft(0)((idx, element)=>{
      f(idx.toString, element)
      idx+1
    })
  }

  override def maxKey: Int = array.length
}

case class AmfMixedMap(map:Map[String,Any] = Map[String,Any]()) extends AmfMixedArray {

  override def iterateEntries(f: (String, Any) => Unit): Unit = {
    map.foreach(keyVal => {
      f(keyVal._1, keyVal._2)
    })
  }

  override def maxKey: Int = map.foldLeft(-1)((v:Int, entry)=>{

    val key = entry._1
    if (key forall Character.isDigit) {

      val intKey = key.toInt
      if (intKey>v) intKey else v
    } else {
      v
    }
  })
}
