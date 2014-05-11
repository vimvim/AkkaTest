package jcodec

import java.nio.ByteBuffer

/**
 *
 * @param fileOffset  Position in the file
 * @param size        Size in the bytes
 * @param pts         Presentation timestamp
 * @param ts          Timescale
 * @param keyFrame    Keyframe
 * @param nalUnits    H.264 encoder NAL units
 */
class MP4FileFrame(val fileOffset:Long, val size:Int, pts:Long, ts:Long, keyFrame:Boolean, nalUnits:List[ByteBuffer])
  extends MP4Frame(pts, ts, keyFrame, nalUnits) {



}
