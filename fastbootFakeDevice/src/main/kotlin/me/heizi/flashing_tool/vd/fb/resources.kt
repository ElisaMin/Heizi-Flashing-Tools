import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

object Resources {
    private fun getResources(name:String): URL? = this::class.java.classLoader.getResource(name)
    object Urls {
        val disconnect get() = getResources("ic_disconnect.png")
        val connected get() = getResources("ic_connected.png")
        val fastboot get() = getResources("ic_fastboot.png")
    }
//    object Images {
//        val disconnect by lazy {
//            ImageIO.read(Urls.connected)
//        }
//    }
}
val fastbootIconBuffered: BufferedImage = ImageIO.read(Resources.Urls.fastboot!!)