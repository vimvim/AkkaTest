package jcodec

import java.io.File
import java.nio.ByteBuffer

import javax.imageio.ImageIO

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import org.jcodec.api.FrameGrab
import org.jcodec.codecs.h264.mp4.AvcCBox
import org.jcodec.codecs.h264.{H264Utils, H264Decoder}

import org.jcodec.common.NIOUtils.readableFileChannel
import org.jcodec.common.NIOUtils.writableFileChannel
import org.jcodec.common.model.{Packet, ColorSpace, Picture}
import org.jcodec.common.{DemuxerTrack, JCodecUtil}

import rtmp.tests.StreamDumpReader
import org.jcodec.containers.mp4._
import org.jcodec.containers.mp4.boxes.Box
import org.jcodec.codecs.h264.io.model.NALUnit


/**
 *
 */
object JCodecTest extends App {


  /**
   * Method will parse MP4 file,  extract SPS,PPS NALU and other information
   * required for streaming purposes.
   *
   */
  def parseMP4 = {

    val demuxer = new MP4Demuxer(readableFileChannel("streams/test_sd.mp4"))
    val videoTrack1 = demuxer.getVideoTrack
    val sampleEntries = videoTrack1.getSampleEntries
    val sampleEntry = sampleEntries.head
    val codecName = sampleEntry.getFourcc

    val avcBox = Box.findFirst(sampleEntry, classOf[AvcCBox], AvcCBox.fourcc())

    val decoder = new H264Decoder()
    decoder.addSps(avcBox.getSpsList)
    decoder.addPps(avcBox.getPpsList)

    for ( i  <- 0 to videoTrack1.getFrameCount ) {

      val packet = videoTrack1.getFrames(1)
      val data = packet.getData

      val buffer  = Picture.create(1920, 1088, ColorSpace.YUV444)
      val frame = decoder.decodeFrame(data, buffer.getData)
      val image = JCodecUtil.toBufferedImage(frame)

      ImageIO.write(image, "png", new File(System.getProperty("user.home")+"/frame_1"))

      /*
      while (data.remaining()>0) {
        val nalUnit = NALUnit.read(data)
      }
      */
    }
  }

  /**
   * Compose MP4 file from H.264 stream
   *
   */
  def composeMP4 = {

    val dumpReader = new StreamDumpReader("dump", "video", "bin.rtmp", 0)

    // REad first packet which is contain sps/pps
    val firstPacket = dumpReader.readAsBuffer()
    readPacketHeader(firstPacket)

    val avcBox = new AvcCBox()
    avcBox.parse(firstPacket)

    val spsList = avcBox.getSpsList
    val ppsList = avcBox.getPpsList

    val muxer = new MP4Muxer(writableFileChannel("streams/out.mp4"), Brand.MOV)

    val videoTrack = muxer.addTrackForCompressed(TrackType.VIDEO, 25)

    while (dumpReader.haveNext) {

      val packet = dumpReader.readAsBuffer()
      readPacketHeader(packet)

      val nalLenBytes = avcBox.getNalLengthSize

      val nalUnits = getNalus(packet, nalLenBytes)

      val avcFrame = ByteBuffer.allocate(1024)
      H264Utils.joinNALUnits(nalUnits, avcFrame)

      H264Utils.wipePS(avcFrame, spsList, ppsList)
      H264Utils.encodeMOVPacket(avcFrame, spsList, ppsList)
      val mp4Packet = new MP4Packet(new Packet(frame, data), frame.getPts(), 0)

      videoTrack.addFrame(mp4Packet)
    }

    val sampleEntry = MP4Muxer.videoSampleEntry("avc1", size, "JCodec")

    val avcC = new AvcCBox(sps.profile_idc, 0, sps.level_idc, write(spss), write(ppss));
    se.add(avcC);
    track.addSampleEntry(se);

  }

  def testDecodeStream = {

    // This is AVC config !!! . Use avcBox.parse(...)

    val dumpReader = new StreamDumpReader("dump", "video", "bin.rtmp", 0)

    val firstPacket = dumpReader.readAsBuffer()
    readPacketHeader(firstPacket)

    val avcBox = new AvcCBox()
    avcBox.parse(firstPacket)

    val decoder = new H264Decoder()
    decoder.addSps(avcBox.getSpsList)
    decoder.addPps(avcBox.getPpsList)

    while (dumpReader.haveNext) {

      val packet = dumpReader.readAsBuffer()
      readPacketHeader(packet)

      val nalLenBytes = avcBox.getNalLengthSize

      val nalUnits = getNalus(packet, nalLenBytes)

      val buffer  = Picture.create(1920, 1088, ColorSpace.YUV444)
      // val frame = decoder.decodeFrame(packet, buffer.getData)
      val frame = decoder.decodeFrame(nalUnits, buffer.getData)
      val image = JCodecUtil.toBufferedImage(frame)

      ImageIO.write(image, "png", new File(System.getProperty("user.home")+"/frame_1"))
    }
  }

  def testDecodeFile = {

    val startSec = 1.632
    val grab = new FrameGrab(readableFileChannel("/home/vim/Videos/test_video.mp4"))
    grab.seek(startSec)

    val frame = grab.getFrame

    ImageIO.write(frame, "png", new File(System.getProperty("user.home")+"/frame_1"))
  }

  private def getNalus(packet:ByteBuffer, nalLenBytes:Int):List[ByteBuffer] = {

    def getNaluLen:Int = {

      nalLenBytes match {

        case 4 =>
          val bytes = new Array[Byte](4)
          packet.get(bytes)
          ((bytes(0) & 0xff) << 24) + ((bytes(1) & 0xff) << 16) + ((bytes(2) & 0xff) << 8) + (bytes(3) & 0xff)

        case 3 =>
          val bytes = new Array[Byte](3)
          packet.get(bytes)
          ((bytes(0) & 0xff) << 16) + ((bytes(1) & 0xff) << 8) + (bytes(2) & 0xff)

        case 2 =>
          val bytes = new Array[Byte](2)
          packet.get(bytes)
          ((bytes(0) & 0xff) << 8) + (bytes(1) & 0xff)

        case 1 =>
          val bytes = new Array[Byte](1)
          packet.get(bytes)
          bytes(1) & 0xff
      }
    }

    def getNalusInternal(nalus: List[ByteBuffer]):List[ByteBuffer] = {

      if (packet.hasRemaining) {

        val len = getNaluLen
        val nalu = new Array[Byte](len)
        packet.get(nalu)

        getNalusInternal(ByteBuffer.wrap(nalu) :: nalus)
      } else {

        nalus
      }
    }

    getNalusInternal(List[ByteBuffer]())
  }

  private def readPacketHeader(packet:ByteBuffer):Unit = {

    val header = new Array[Byte](5)
    packet.get(header)

    // public static const FRAME_TYPE_KEYFRAME:int = 1;
    // public static const FRAME_TYPE_INTER:int = 2;
    // public static const FRAME_TYPE_DISPOSABLE_INTER:int = 3;
    // public static const FRAME_TYPE_GENERATED_KEYFRAME:int = 4;
    // public static const FRAME_TYPE_INFO:int = 5;
    val frameType = header(0) >> 4

    // public static const CODEC_ID_JPEG:int = 1;
    // public static const CODEC_ID_SORENSON:int = 2;
    // public static const CODEC_ID_SCREEN:int = 3;
    // public static const CODEC_ID_VP6:int = 4;
    // public static const CODEC_ID_VP6_ALPHA:int = 5;
    // public static const CODEC_ID_SCREEN_V2:int = 6;
    // public static const CODEC_ID_AVC:int = 7;         <==== H264
    val codecId = header(0) & 0x0f

    // public static const AVC_PACKET_TYPE_SEQUENCE_HEADER:int = 0;
    // public static const AVC_PACKET_TYPE_NALU:int = 1;
    // public static const AVC_PACKET_TYPE_END_OF_SEQUENCE:int = 2;
    val packetType = header(1)

    val compositionTime = ((header(2) & 0xff) << 16) + ((header(3) & 0xff) << 8) + (header(4) & 0xff)
  }

  // testDecodeFile
  testDecodeStream
}
