package de.janorico.picturegenerator.data.pgp

import de.janorico.picturegenerator.exception
import org.w3c.dom.*
import org.xml.sax.SAXException
import java.io.*
import javax.swing.filechooser.FileNameExtensionFilter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object PGPFileType {
    val FILTER = FileNameExtensionFilter("PictureGenerator pictures (*.pgp)", "pgp")
    private val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun readXMLFile(file: File): PGPObject? {
        var stream: FileInputStream? = null
        try {
            stream = FileInputStream(file)
            val document = builder.parse(stream)
            return fromXML(document.documentElement)
        } catch (e: SAXException) {
            exception(e)
        } catch (e: IOException) {
            exception(e)
        } finally {
            try {
                stream?.close()
            } catch (e: IOException) {
                exception(e)
            }
        }
        return null
    }


    fun writeXMLFile(file: File, pgpObject: PGPObject) {
        if (!file.isFile) file.createNewFile()
        val document = toXML(pgpObject)
        // Write
        val stream = FileOutputStream(file)
        val streamResult = StreamResult(stream)
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.transform(DOMSource(document), streamResult)
        stream.close()
    }

    private fun fromXML(element: Element): PGPObject {
        val name = element.getAttribute("name")
        val version = element.getAttribute("version")
        val width = element.getAttribute("width").toInt()
        val height = element.getAttribute("height").toInt()
        val data = parseDataString(element.textContent, width, height)
        return PGPObject(name, version, data)
    }

    private fun toXML(pgpObject: PGPObject): Document {
        val document = builder.newDocument()
        val element = document.createElement("PictureGeneratorPicture")
        element.setAttribute("name", pgpObject.name)
        element.setAttribute("version", pgpObject.version)
        element.setAttribute("width", pgpObject.getWidth().toString())
        element.setAttribute("height", pgpObject.getHeight().toString())
        element.textContent = storeDataString(pgpObject.data)
        document.appendChild(element)
        return document
    }

    @Throws(IOException::class)
    private fun parseDataString(dataString: String, width: Int, height: Int): Array<Array<Boolean>> {
        val lines = dataString.split('\n', '\r')
        if (height != lines.size) throw IOException("Can't parse data string!")
        return Array(height) { row: Int ->
            return@Array Array(width) InnerArray@{ column: Int ->
                return@InnerArray lines[row][column] == '1'
            }
        }
    }

    private fun storeDataString(rows: Array<Array<Boolean>>): String = buildString {
        for (row in rows) {
            for (bool in row) {
                append(if (bool) '1' else '0')
            }
            append('\n')
        }
    }.removeSuffix("\n")
}
