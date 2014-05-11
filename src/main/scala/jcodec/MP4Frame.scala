package jcodec

import java.nio.ByteBuffer

/**
 *
 * @param pts         Presentation timestamp
 * @param ts          Timescale
 * @param keyFrame    Keyframe
 * @param nalUnits    H.264 encoder NAL units
 */
class MP4Frame(pts:Long, ts:Int, keyFrame:Boolean, val nalUnits:List[ByteBuffer]) extends Frame(pts, ts, keyFrame) {


}
