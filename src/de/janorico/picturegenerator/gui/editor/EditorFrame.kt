package de.janorico.picturegenerator.gui.editor

import de.janorico.jgl.JGL
import de.janorico.jgl.helpers.*
import de.janorico.jgl.helpers.Button
import de.janorico.jgl.helpers.Dialog
import de.janorico.picturegenerator.data.pgp.*
import de.janorico.picturegenerator.gui.components.*
import de.janorico.picturegenerator.gui.components.drawing.*
import de.janorico.picturegenerator.logos
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.system.exitProcess

class EditorFrame(val pgpObject: PGPObject, file: File? = null) : JFrame() {
    private var changed = false
    private var path: String? = null
    private var saved = false

    // GUI Components
    private val statusBar = StatusBar()

    init {
        JGL.dialogOwner = this
        if (file != null) {
            path = file.path
            saved = true
        }
        jMenuBar = EditorMenuBar(this)
        add(JPanel(GridBagLayout()).apply {
            add(JPanel(GridLayout(pgpObject.getHeight(), pgpObject.getWidth())).apply {
                val group = PixelButtonGroup(object : PixelButtonChangedListener {
                    override fun changed(x: Int, y: Int, value: Int) {
                        change()
                        statusBar.displayMessage("Button $x | $y has changed to $value.")
                        pgpObject.setPixel(x, y, value == PixelButton.BLACK)
                    }
                })
                for (y: Int in 0 until pgpObject.getHeight()) {
                    for (x: Int in 0 until pgpObject.getWidth()) {
                        val button = PixelButton(group, x, y, initialState = (if (pgpObject.getPixel(x, y)) PixelButton.BLACK else PixelButton.WHITE))
                        this.add(button)
                    }
                }
            })
        })
        add(statusBar, BorderLayout.SOUTH)

        this.pack()
        this.iconImages = logos
        this.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                exit()
            }
        })
        this.extendedState = MAXIMIZED_BOTH
        this.setLocationRelativeTo(null)
        this.title = "PictureGenerator Editor"
        this.isVisible = true
        statusBar.displayMessage("Initialized")
    }

    fun changeName() {
        val newName = OptionPane.showPromptDialog("Type new name:", "Change name", pgpObject.name)
        if (newName != null) {
            pgpObject.name = newName
            change()
            statusBar.displayMessage("Name has changed to ${pgpObject.name}.")
        }
    }

    fun changeVersion() {
        val newVersion = OptionPane.showPromptDialog("Type new version:", "Change version", pgpObject.version)
        if (newVersion != null) {
            pgpObject.version = newVersion
            change()
            statusBar.displayMessage("Version has changed to ${pgpObject.version}.")
        }
    }

    fun change() {
        if (!changed) changed = true
    }

    fun exportToPicture() {
        val whitePicker = ColorPicker(Color.WHITE)
        val blackPicker = ColorPicker(Color.BLACK)
        val chooser = FileChooser.createSaveFile(arrayOf(FileNameExtensionFilter("Portable Network Groups (*.png)", "png")))
        chooser.accessory = JPanel().apply {
            minimumSize = add(JPanel(GridLayout(2, 2)).apply {
                add(JLabel("White:"), BorderLayout.WEST)
                add(whitePicker)
                add(JLabel("Black:"), BorderLayout.WEST)
                add(blackPicker)
            }).minimumSize
        }
        val result = chooser.showSaveDialog(JGL.dialogOwner)
        if (result == JFileChooser.APPROVE_OPTION) {
            val bi = BufferedImage(pgpObject.getWidth(), pgpObject.getHeight(), BufferedImage.TYPE_INT_ARGB)
            val raster = bi.raster
            val model = bi.colorModel
            val whiteColor = model.getDataElements(whitePicker.color.rgb, null)
            val blackColor = model.getDataElements(blackPicker.color.rgb, null)
            for (x in 0 until pgpObject.getWidth()) {
                for (y in 0 until pgpObject.getHeight()) {
                    raster.setDataElements(x, y, if (pgpObject.getPixel(x, y)) blackColor else whiteColor)
                }
            }
            ImageIO.write(bi, "png", chooser.selectedFile)
        }
    }

    fun arduino() {
        if (((pgpObject.getWidth() * pgpObject.getHeight()) % 8) == 0) {
            val arrayName = OptionPane.showPromptDialog("Type array name:", "PictureGenerator: Generate Arduino Code")
            if (arrayName != null) {
                var currentBits = 0
                var currentBytes = 0
                val prefixString = "byte $arrayName[] = { "
                val indent = buildString {
                    for (i in prefixString.indices) append(" ")
                }
                val generatedCode = prefixString + buildString {
                    for (row in pgpObject.data) {
                        for (col in row) {
                            if (currentBits % 8 == 0) {
                                if (currentBytes % 2 == 0) append(",\n${indent}0B") else append(", 0B")
                                currentBytes++
                            }
                            append(if (col) "1" else "0")
                            currentBits++
                        }
                    }
                }.removePrefix(",\n$indent") + " };"
                Dialog.showDialog("Arduino code", { _: JDialog ->
                    return@showDialog JTextArea(generatedCode).apply {
                        isEditable = false
                        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
                    }
                }, { dialog: JDialog ->
                    return@showDialog JPanel(GridLayout(1, 2)).apply {
                        add(Button.create("Copy") {
                            val ss = StringSelection(generatedCode)
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(ss, ss)
                        })
                        add(Button.create("Close") { dialog.dispose() })
                    }
                })
            }
        } else {
            OptionPane.showInformation("((Width * Height) % 8) is not 0 (% stands for modulo.)!")
        }
    }

    fun exit() {
        if (changed) {
            val result = OptionPane.showSaveOnCloseDialog()
            if (result == JOptionPane.YES_OPTION) {
                save(false)
                if (saved && path != null) exitProcess(0)
            }
            if (result == JOptionPane.NO_OPTION) exitProcess(0)
        } else exitProcess(0)
    }

    fun save(saveAs: Boolean) {
        if (saveAs || !saved || path == null) {
            FileChooser.saveFile({ selectedFile: File?, result: Int ->
                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    if (selectedFile.isFile) {
                        if (OptionPane.overwriteFileDialog() == JOptionPane.YES_OPTION) {
                            path = selectedFile.path
                            save()
                        }
                    } else {
                        path = selectedFile.path
                        save()
                    }
                }
            }, PGPFileType.FILTER)
        } else {
            save()
        }
    }

    private fun save() {
        if (path != null) {
            statusBar.displayMessage("Saving file $path...")
            PGPFileType.writeXMLFile(File(path ?: "${System.getProperty("user.home")}/PictureGeneratorError.pgp"), pgpObject)
            saved = true
            changed = false
            statusBar.displayMessage("File $path has been saved.")
        }
    }
}
