package jcodec

/**
 *
 * @param pts         Presentation timestamp
 * @param ts          Timescale
 * @param keyFrame    Keyframe
 */
class Frame(val pts:Long, val ts:Long, val duration:Long, val keyFrame:Boolean) {


}
