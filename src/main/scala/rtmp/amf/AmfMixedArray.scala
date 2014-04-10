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
  def iterateEntries(f:(String, AnyRef)=>Unit)
}

class AmfMixedList(val array:List[AnyRef] = List[AnyRef]()) extends AmfMixedArray {

  override def iterateEntries(f: (String, AnyRef) => Unit): Unit = array.fold(0)((idx:Int, element)=>{
    f(idx.toString, element)
    idx+1
  })

  override def maxKey: Int = array.length
}

class AmfMixedMap(val map:Map[String,AnyRef] = Map[String,AnyRef]()) extends AmfMixedArray {

  override def iterateEntries(f: (String, AnyRef) => Unit): Unit = map.foreach(f)
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
