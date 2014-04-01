package jcodec

import java.io.File
import javax.imageio.ImageIO

import org.jcodec.api.FrameGrab

/**
 *
 */
object JCodecTest extends App {

  val startSec = 51.632
  val grab = new FrameGrab(new File("filename.mp4"))
  grab.seek(startSec)
  ImageIO.write(grab.getFrame(), "png", new File(System.getProperty("user.home"), String.format("frame_%08d.png", 1)))
}
