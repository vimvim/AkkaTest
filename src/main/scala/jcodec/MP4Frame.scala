package jcodec

import java.nio.ByteBuffer

/**
 *
 * @param pts         Presentation timestamp
 * @param ts          Timescale
 * @param duration    Duration of the frame
 * @param keyFrame    Keyframe
 * @param nalUnits    H.264 encoder NAL units
 */
class MP4Frame(pts:Long, ts:Long, duration:Long, keyFrame:Boolean, val nalUnits:List[ByteBuffer]) extends Frame(pts, ts, duration, keyFrame) {


}
