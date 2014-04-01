package jcodec

import java.io.File

import javax.imageio.ImageIO

import org.jcodec.api.FrameGrab
import org.jcodec.codecs.h264.mp4.AvcCBox
import org.jcodec.codecs.h264.H264Decoder

import org.jcodec.common.NIOUtils.readableFileChannel
import org.jcodec.common.model.{ColorSpace, Picture}
import org.jcodec.common.JCodecUtil

/**
 *
 */
object JCodecTest extends App {

  def testDecodeStream = {

    // This is AVC config !!! . Use avcBox.parse(...)
    val avcBox = new AvcCBox()
    avcBox.parse()

    val decoder = new H264Decoder()
    decoder.addSps(avcBox.getSpsList)
    decoder.addPps(avcBox.getPpsList)

    val buffer  = Picture.create(1920, 1088, ColorSpace.YUV444)
    val frame = decoder.decodeFrame(data)
    val image = JCodecUtil.toBufferedImage(frame)

    ImageIO.write(frame, "png", new File(System.getProperty("user.home")+"/frame_1"))
  }

  def testDecodeFile = {

    val startSec = 1.632
    val grab = new FrameGrab(readableFileChannel("/home/vim/Videos/test_video.mp4"))
    grab.seek(startSec)

    val frame = grab.getFrame

    ImageIO.write(frame, "png", new File(System.getProperty("user.home")+"/frame_1"))
  }
}
