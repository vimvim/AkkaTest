package jcodec

import org.jcodec.codecs.h264.mp4.AvcCBox
import org.jcodec.containers.mp4.muxer.MP4Muxer
import org.jcodec.common.NIOUtils._
import org.jcodec.containers.mp4.{MP4Packet, TrackType, Brand}

import java.nio.ByteBuffer
import org.jcodec.codecs.h264.io.model.{NALUnitType, NALUnit}
import org.jcodec.codecs.h264.H264Utils

/**
 * Created by vim on 5/11/14.
 */
class MP4Writer(filename:String, avcBox: AvcCBox) {

  val spsList = avcBox.getSpsList
  val ppsList = avcBox.getPpsList

  val muxer = new MP4Muxer(writableFileChannel(filename), Brand.MP4)
  val videoTrack = muxer.addTrack(TrackType.VIDEO, 25000)

  var frameIdx = 0

  def writeFrame(frame:MP4Frame) = {

    // val filteredNalUnits = new java.util.ArrayList[ByteBuffer]()

    val movPacket = ByteBuffer.allocate(1024*100)

    for( nalUnitData <- frame.nalUnits ) {

      val nalUnit = NALUnit.read(nalUnitData)
      println("   NAL:"+nalUnit.`type`)
      nalUnit.`type` match {

        case NALUnitType.PPS =>
          println("   remove PPS")
          ppsList.add(nalUnitData)

        case NALUnitType.SPS =>
          println("   remove SPS")
          spsList.add(nalUnitData)

        case _ =>
          // filteredNalUnits.add(nalUnitData)
          movPacket.putInt(nalUnitData.limit())
          movPacket.put(nalUnitData)
      }
    }

    movPacket.flip()

    // H264Utils.joinNALUnits(filteredNalUnits, movPacket)
    // H264Utils.encodeMOVPacket(movPacket)
    // for( nalUnitData <- filteredNalUnits ) {
    //   movPacket.putInt(nalUnitData.)
    // }


    // new MP4Packet(
    //  result,      Bytebuffer that contains encoded frame
    //  i,           Presentation timestamp ( think seconds ) expressed in timescale units ( just multiply second by
    //               timescale value below. This is to avoid floats.
    //                   Example: timescale = 25, pts = 0, 1, 2, 3, 4, 5 .... ( PAL 25 fps )
    //                   Example: timescale = 30000, pts = 1001, 2002, 3003, 4004, 5005, 6006, 7007 ( NTSC 29.97 fps )
    //                            timescale, // See above
    //  1,           Duration of a frame in timescale units ( think seconds multiplied by number above)
    //                   Examlle: timescale = 25, duration = 1 ( PAL 25 fps )
    //                   Example: timescale = 30000, duration = 1001 ( NTSC 29.97 fps )
    //  frameNo,     Just a number of frame, doesn't have anything to do with timing
    //  true,        Is it an I-frame, i.e. is this a seek point? Players use this information to instantly know where to seek
    //  null,        just ignore, should be null. This is used by the older brother of MP4 - Apple Quicktime which supports
    //               tape timecode
    //  i,           just put the same as pts above
    //  0            sample entry, should be 0
    // )

    // val mp4Packet = new MP4Packet(new Packet(frame, data), frame.getPts(), 0)
    val mp4Packet = new MP4Packet(movPacket, frame.pts, frame.ts, frame.duration, frameIdx, frame.keyFrame, null, frame.pts, 0)

    videoTrack.addFrame(mp4Packet)

    frameIdx = frameIdx + 1
  }

  def complete() = {

    val sampleEntry = H264Utils.createMOVSampleEntry(spsList, ppsList)
    videoTrack.addSampleEntry(sampleEntry)

    muxer.writeHeader()
  }

}
