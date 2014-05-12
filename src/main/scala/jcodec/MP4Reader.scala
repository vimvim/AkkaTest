package jcodec

import org.jcodec.containers.mp4.demuxer.MP4Demuxer
import org.jcodec.common.NIOUtils._
import org.jcodec.containers.mp4.boxes.VideoSampleEntry
import org.jcodec.codecs.h264.mp4.AvcCBox
import org.jcodec.codecs.h264.H264Utils

import java.nio.ByteBuffer
import org.jcodec.containers.mp4.MP4Packet
import jcodec.MP4Frame


/**
 * Created by vim on 5/11/14.
 */
class MP4Reader(filename:String) {

  val demuxer = new MP4Demuxer(readableFileChannel(filename))
  val videoTrack1 = demuxer.getVideoTrack
  val sampleEntries = videoTrack1.getSampleEntries
  val sampleEntry:VideoSampleEntry = sampleEntries.head.asInstanceOf[VideoSampleEntry]
  val codecName = sampleEntry.getFourcc

  val avcBox:AvcCBox = H264Utils.parseAVCC(sampleEntry)

  val framesCount = videoTrack1.getFrameCount

  /**
   * Read next frame and return NAL units for it
   *
   * @return
   */
  def readFrame():MP4Frame = {

    val packet = videoTrack1.nextFrame().asInstanceOf[MP4Packet]
    val data = packet.getData

    println(s" MediaPTS:${packet.getMediaPts} PTS:${packet.getPts} TS:${packet.getTimescale} Duration:${packet.getDuration} FrameNO:${packet.getFrameNo} KeyFrame:${packet.isKeyFrame} offset:${packet.getFileOff} size:${packet.getSize}")

    val nalUnits = H264Utils.splitMOVPacket(data, avcBox).toArray.toList.asInstanceOf[List[ByteBuffer]]

    new MP4FileFrame(packet.getFileOff, packet.getSize, packet.getPts, packet.getTimescale, packet.getDuration, packet.isKeyFrame, nalUnits)
  }
}
