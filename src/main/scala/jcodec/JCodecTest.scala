package jcodec

import java.io.File
import java.nio.ByteBuffer

import javax.imageio.ImageIO

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import org.jcodec.api.FrameGrab
import org.jcodec.codecs.h264.mp4.AvcCBox
import org.jcodec.codecs.h264.H264Decoder

import org.jcodec.common.NIOUtils.readableFileChannel
import org.jcodec.common.model.{ColorSpace, Picture}
import org.jcodec.common.JCodecUtil

import rtmp.tests.StreamDumpReader


/**
 *
 */
object JCodecTest extends App {

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
