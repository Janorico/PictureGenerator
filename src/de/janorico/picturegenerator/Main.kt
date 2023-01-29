package de.janorico.picturegenerator

import de.janorico.jgl.JGL
import de.janorico.jgl.helpers.OptionPane
import de.janorico.picturegenerator.gui.WelcomeFrame
import java.awt.Desktop
import java.io.*
import java.net.*
import java.util.*
import javax.swing.*
import kotlin.system.exitProcess

// KEYS
private const val REPOSITORY_PAGE_KEY = "pages.repository"
private const val DOWNLOAD_PAGE_KEY = "pages.download"
private const val NEWEST_VERSION_KEY_KEY = "newest-version.key"
private const val NEWEST_VERSION_DISPLAY_NAME_KEY = "newest-version.display-name"
private const val NEWEST_VERSION_WINDOWS_INSTALLER_KEY = "newest-version.windows-installer"
val logos = listOf(
    ResourceManager.getImage("PictureGeneratorLogo16"),
    ResourceManager.getImage("PictureGeneratorLogo20"),
    ResourceManager.getImage("PictureGeneratorLogo32"),
    ResourceManager.getImage("PictureGeneratorLogo40"),
    ResourceManager.getImage("PictureGeneratorLogo64"),
)

fun main(args: Array<String>) {
    JGL.programName = "PictureGenerator"
    if (args.isNotEmpty()) WelcomeFrame(args)
    else WelcomeFrame()
    checkForUpdates()
}

fun exception(e: Exception) {
    e.printStackTrace()
    OptionPane.showException(e)
}

fun checkForUpdates() {
    try {
        val address = InetAddress.getByName("github.com")
        if (address.isReachable(5000)) {
            val properties = Properties()
            val stream = URL("https://github.com/Janorico/Versions/raw/main/PictureGenerator.properties").openStream()
            properties.load(stream)
            stream.close()
            // Updates loaded successful.
            val newestVersion = (properties.getProperty(NEWEST_VERSION_KEY_KEY) ?: throw IOException("Can't get key \"$NEWEST_VERSION_KEY_KEY\"!")).toFloat()
            if (newestVersion > 1.0f) {
                val repositoryPage = properties.getProperty(REPOSITORY_PAGE_KEY) ?: throw IOException("Can't get key \"$REPOSITORY_PAGE_KEY\"!")
                val downloadPage = properties.getProperty(DOWNLOAD_PAGE_KEY) ?: throw IOException("Can't get key \"$DOWNLOAD_PAGE_KEY\"!")
                val newestVersionDisplayName =
                    properties.getProperty(NEWEST_VERSION_DISPLAY_NAME_KEY) ?: throw IOException("Can't get key \"$NEWEST_VERSION_DISPLAY_NAME_KEY\"!")
                // Show message
                val action = JOptionPane.showOptionDialog(
                    JGL.dialogOwner,
                    "$newestVersionDisplayName is available.",
                    "Update",
                    JOptionPane.CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    arrayOf("Update", "Open download page", "Open repository page", "Do nothing"),
                    "Update"
                )
                when (action) {
                    0 -> {
                        println("Update")
                        if (System.getProperty("os.name").startsWith("Windows", true)) {
                            val file = File("${System.getProperty("user.home")}/Downloads", "PictureGenerator${newestVersion}Installer.exe")
                            val newestVersionWindowsInstaller = properties.getProperty(NEWEST_VERSION_WINDOWS_INSTALLER_KEY)
                                ?: throw IOException("Can't get key \"$NEWEST_VERSION_WINDOWS_INSTALLER_KEY\"!")
                            if (download(URL(newestVersionWindowsInstaller), file)) {
                                Desktop.getDesktop().open(file)
                                exitProcess(0)
                            }
                        } else {
                            OptionPane.showInformation("This actually only works on Windows!")
                        }
                    }

                    1 -> {
                        println("Open download page")
                        Desktop.getDesktop().browse(URI(downloadPage))
                    }

                    2 -> {
                        println("Open repository page")
                        Desktop.getDesktop().browse(URI(repositoryPage))
                    }
                }
            }
        } else {
            throw IOException("Host \"github.com\" isn't reachable.")
        }
    } catch (e: IOException) {
        exception(e)
    }
}

fun download(url: URL, target: File): Boolean {
    val httpConnection = url.openConnection() as HttpURLConnection
    val completeFileSize = httpConnection.contentLength
    val monitor = ProgressMonitor(JGL.dialogOwner, "PictureGenerator Update", "Downloading installer...", 0, completeFileSize)
    val bis = BufferedInputStream(httpConnection.inputStream)
    // Create the file, if it not already exist.
    if (!target.isFile) target.createNewFile()
    val fos = FileOutputStream(target)
    val bos = BufferedOutputStream(fos, 1024)
    val data = ByteArray(1024)
    var downloadedFileSize = 0
    var x: Int
    while (bis.read(data, 0, 1024).also { x = it } >= 0) {
        downloadedFileSize += x
        // GUI
        monitor.setProgress(downloadedFileSize)
        if (monitor.isCanceled) return false
        // Write to file
        bos.write(data, 0, x)
    }
    bos.close()
    bis.close()
    return true
}
