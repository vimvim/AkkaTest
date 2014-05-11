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
import org.jcodec.common.model.{Size, Packet, ColorSpace, Picture}
import org.jcodec.common.{DemuxerTrack, JCodecUtil}

import rtmp.tests.StreamDumpReader
import org.jcodec.containers.mp4._
import org.jcodec.containers.mp4.boxes.{VideoSampleEntry, Box}
import org.jcodec.codecs.h264.io.model.{NALUnitType, SeqParameterSet, NALUnit}
import org.jcodec.containers.mp4.muxer.{FramesMP4MuxerTrack, MP4Muxer}
import org.jcodec.containers.mp4.demuxer.MP4Demuxer

import org.jcodec.codecs.h264.H264Utils.getPicHeightInMbs
import java.util
import org.jcodec.codecs.raw.V210Encoder


/**
 *
 */
object JCodecTest extends App {

  /**
   * Method will read frames ( NAL units ) from one MP4 file and pack into another
   *
   */

  def rebuildMP4() = {

    val reader = new MP4Reader("streams/test_sd.mp4")
    val writer = new MP4Writer("streams/out.mp4", reader.avcBox)

    println(s"Total frames: ${reader.framesCount}")

    for( frameIdx <- 1.toLong to reader.framesCount) {

      println(s"Process frame: $frameIdx")
      // TODO: Needs to return frame record. Keep pts  ??
      // TODO: Go thru all file , index frames, get positions offset, timestamp marks and keyframe marks
      val frame = reader.readFrame()
      writer.writeFrame(frame)
    }

    writer.complete()

    println(s"Finished")
  }

  /**
   * Method will parse MP4 file,  extract SPS,PPS NALU and other information
   * required for streaming purposes.
   *
   */
  def parseMP4() = {

    val demuxer = new MP4Demuxer(readableFileChannel("streams/test_sd.mp4"))
    val videoTrack1 = demuxer.getVideoTrack
    val sampleEntries = videoTrack1.getSampleEntries
    val sampleEntry:VideoSampleEntry = sampleEntries.head.asInstanceOf[VideoSampleEntry]
    val codecName = sampleEntry.getFourcc

    // SampleEntry se = videoTrack1.getSampleEntries()[0];

    val avcBox:AvcCBox = H264Utils.parseAVCC(sampleEntry)

    // val avcBox = Box.findFirst(sampleEntry, classOf[AvcCBox], AvcCBox.fourcc())

    val decoder = new H264Decoder()
    decoder.addSps(avcBox.getSpsList)
    decoder.addPps(avcBox.getPpsList)

    for ( i  <- 0.toLong to videoTrack1.getFrameCount ) {

      val packet = videoTrack1.nextFrame()
      val data = packet.getData

      // TODO: NALU in the MP4/MOV composed in the same way ( length followed by NAL ) as in the RTp/RTMP packets so we can
      // TODO: use this function to split it's
      val nalUnits = H264Utils.splitMOVPacket(data, avcBox)

      val buffer  = Picture.create(1920, 1088, ColorSpace.YUV444)
      val frame = decoder.decodeFrame(nalUnits, buffer.getData)
      // val image = JCodecUtil.toBufferedImage(frame)

      // ImageIO.write(image, "png", new File(System.getProperty("user.home")+"/frame_1"))
    }
  }

  /**
   * Compose MP4 file from H.264 stream
   *
   */
  def composeMP4() = {

    val dumpReader = new StreamDumpReader("dump", "video", "bin.rtmp", 0)

    // REad first packet which is contain sps/pps
    val firstPacket = dumpReader.readAsBuffer()
    readPacketHeader(firstPacket)

    val avcBox = new AvcCBox()
    avcBox.parse(firstPacket)

    val spsList = avcBox.getSpsList
    val ppsList = avcBox.getPpsList

    val muxer = new MP4Muxer(writableFileChannel("streams/out.mp4"), Brand.MP4)
    val videoTrack = muxer.addTrack(TrackType.VIDEO, 25)

    var frameIdx = 0

    while (dumpReader.haveNext) {

      val packet = dumpReader.readAsBuffer()
      readPacketHeader(packet)

      val filteredNalUnits = new java.util.ArrayList[ByteBuffer]()

      val nalUnits = H264Utils.splitMOVPacket(packet, avcBox)
      for( nalUnitData <- nalUnits ) {

        val nalUnit = NALUnit.read(nalUnitData)
        println("NAL:"+nalUnit.`type`)
        nalUnit.`type` match {

          case NALUnitType.PPS =>
            println("   remove PPS")
            ppsList.add(nalUnitData)

          case NALUnitType.SPS =>
            println("   remove SPS")
            spsList.add(nalUnitData)

          case _ => filteredNalUnits.add(nalUnitData)
        }
      }

      // val nalLenBytes = avcBox.getNalLengthSize

      // val nalUnits = getNalus(packet, nalLenBytes)

      // val avcFrame = ByteBuffer.allocate(1024)
      // H264Utils.joinNALUnits(nalUnits, avcFrame)

      // H264Utils.wipePS(avcFrame, spsList, ppsList)

      // H264Utils.encodeMOVPacket(avcFrame)



      val movPacket = ByteBuffer.allocate(1024)
      H264Utils.joinNALUnits(filteredNalUnits, movPacket)
      H264Utils.encodeMOVPacket(movPacket)

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
      val mp4Packet = new MP4Packet(movPacket, frameIdx, 25, 1, frameIdx, true, null, frameIdx, 0)

      videoTrack.addFrame(mp4Packet)

      frameIdx = frameIdx + 1
    }

    // val sps:SeqParameterSet = SeqParameterSet.read(spsList.head)
    // val size:Size = new Size((sps.pic_width_in_mbs_minus1 + 1) << 4, getPicHeightInMbs(sps) << 4)

    // val sampleEntry = MP4Muxer.videoSampleEntry("avc1", size, "JCodec")
    val sampleEntry = H264Utils.createMOVSampleEntry(spsList, ppsList)

    // val avcC = new AvcCBox(sps.profile_idc, 0, sps.level_idc, write(spss), write(ppss))
    // sampleEntry.add(avcBox)

    videoTrack.addSampleEntry(sampleEntry)

    muxer.writeHeader()
  }

  def mp4Generator() = {

    val width = 640
    val height = 480

    def createPicture:Picture = {

      def drawGrad(y:Array[Int], ySize:Size) {

        val blockX = ySize.getWidth / 10
        val blockY = ySize.getHeight / 7

        fillGrad(y, ySize.getWidth, blockX, blockY, 9 * blockX, 3 * blockY, 0.2, 0.1, 8)
        fillGrad(y, ySize.getWidth, blockX, 4 * blockY, 9 * blockX, 6 * blockY, 0.2, 0.1, 10)
      }

      def fillGrad(y:Array[Int], stride:Int, left:Int, top:Int, right:Int, bottom:Int, from:Double, to:Double, quant:Int) {

        val step = stride + left - right
        var off = top * stride + left

        for( j <- top to bottom ) {

          for ( i <- left to right ) {

            // y[off] = colr((i - left) / (right - left), (j - top) / (bottom - top), from, to, quant)
            off = off + 1
          }
          off = off + step
        }
      }

      def colr(i:Double, j:Double, from:Double, to:Double, quant:Int):Int = {

        val v1:Int = ((1 << quant) * (from + (to - from) * i)).toInt
        val v2 = 10 - quant

        v1 << v2
      }

      val pic = Picture.create(width, height, ColorSpace.YUV422_10)
      util.Arrays.fill(pic.getPlaneData(1), 512)
      util.Arrays.fill(pic.getPlaneData(2), 512)

      drawGrad(pic.getPlaneData(0), new Size(pic.getWidth, pic.getHeight))

      pic
    }

    val encoder:V210Encoder = new V210Encoder()
    val muxer:MP4Muxer = new MP4Muxer(writableFileChannel("streams/generated.mp4"))

    val videoTrack:FramesMP4MuxerTrack = muxer.addVideoTrack("v210", new Size(width, height), "jcodec", 24000)

    for( frameIdx <- 0 to 1000 ) {

      val picture = createPicture

      val buffer = ByteBuffer.allocate(width * height * 10)
      val frame = encoder.encodeFrame(buffer, picture)

      videoTrack.addFrame(new MP4Packet(frame, frameIdx * 1001, 24000, 1001, frameIdx, true, null, frameIdx * 1001, 0))
    }

    muxer.writeHeader()
  }

  def testDecodeStream() = {

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
      // val image = JCodecUtil.toBufferedImage(frame)

      // ImageIO.write(image, "png", new File(System.getProperty("user.home")+"/frame_1"))
    }
  }

  def testDecodeFile() = {

    val startSec = 1.632
    val grab = new FrameGrab(readableFileChannel("/home/vim/Videos/test_video.mp4"))
    val frame = grab.seekToSecondPrecise(startSec)

    // ImageIO.write(frame, "png", new File(System.getProperty("user.home")+"/frame_1"))
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

  // testDecodeFile()
  // testDecodeStream()
  // composeMP4()
  // parseMP4()
  rebuildMP4()
}
