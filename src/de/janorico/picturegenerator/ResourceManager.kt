package de.janorico.picturegenerator

import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon

object ResourceManager {
    fun getImage(name: String): BufferedImage = ImageIO.read(getResourceURL("images/$name.png"))
    fun getIcon(name: String): ImageIcon = ImageIcon(getResourceURL("images/$name.png"))

    fun getResourceURL(name: String): URL = ResourceManager::class.java.classLoader.getResource(name) ?: throw IOException("Could not find resource $name!")
}
