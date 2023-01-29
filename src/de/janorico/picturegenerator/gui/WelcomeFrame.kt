package de.janorico.picturegenerator.gui

import de.janorico.jgl.JGL
import de.janorico.jgl.helpers.*
import de.janorico.jgl.helpers.Button
import de.janorico.jgl.helpers.Dialog
import de.janorico.picturegenerator.*
import de.janorico.picturegenerator.data.pgp.*
import de.janorico.picturegenerator.gui.editor.EditorFrame
import de.janorico.picturegenerator.gui.game.GameFrame
import java.awt.*
import java.io.File
import java.util.*
import javax.swing.*

class WelcomeFrame : JFrame {
    private val copyright = """<html><pre>
PictureGenerator is a program to create and edit *.pgp files.
Copyright (C) 2023 Janosch Lion

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see &lt;https://www.gnu.org/licenses/&gt;.
</pre></html>""".trimIndent()
    private val about = """<html>
<h1 align="center">PictureGenerator</h1>
<h2 align="center">By Janorico</h2>
<pre>
Author: Janosch Lion
Website: https://github.com/Janorico/PictureGenerator
E-Mail: janorico@posteo.de
License: GNU General Public License v3.0
<i>
THIS PROGRAM USE JGL 1.0 (https://github.com/Janorico/JGL) AND IS COMPILED WITH
THE '-include-runtime' PARAMETER (kotlinc 1.8.0, https://gituhb.com/JetBrains/Kotlin).
</i>
</pre></html>""".trimIndent()

    constructor() {
        JGL.dialogOwner = this
        add(JLabel("Welcome to PictureGenerator!", JLabel.CENTER).apply { font = Font(null, Font.PLAIN, 40) }, BorderLayout.NORTH)
        add(JPanel(GridBagLayout()).apply {
            add(Button.create(ResourceManager.getIcon("NewIcon"), "Create a new project") { new() })
            add(Button.create(ResourceManager.getIcon("OpenIcon"), "Open a project file") {
                FileChooser.openFiles({ selectedFiles: Array<File>, result: Int ->
                    if (result == JFileChooser.APPROVE_OPTION && selectedFiles.isNotEmpty()) open(Vector(selectedFiles.toMutableList()))
                }, PGPFileType.FILTER)
            })
            add(Button.create(ResourceManager.getIcon("InfoIcon"), "Info about PictureGenerator.") {
                Dialog.showDialog("${JGL.programName}: About", { dialog: JDialog ->
                    dialog.minimumSize = Dimension(700, 525)
                    dialog.preferredSize = Dimension(700, 525)
                    return@showDialog JTabbedPane().apply {
                        addTab("Copyright", JLabel(copyright, JLabel.CENTER))
                        add("About", JLabel(about, JLabel.CENTER))
                        addTab("License", JScrollPane(JEditorPane(ResourceManager.getResourceURL("gpl.html")).apply {
                            isEditable = false
                        }))
                    }
                }, { dialog: JDialog -> Button.create("Close") { dialog.dispose() } })
            })
        }, BorderLayout.CENTER)

        this.pack()
        this.iconImages = logos
        this.defaultCloseOperation = DISPOSE_ON_CLOSE
        this.extendedState = MAXIMIZED_BOTH
        this.setLocationRelativeTo(null)
        this.title = "PictureGenerator"
        this.isVisible = true
    }

    constructor(args: Array<String>) : this() {
        val files = ArrayList<File>()
        for (arg in args) {
            val file = File(arg)
            if (file.isFile) files.add(file)
        }
        open(Vector(files))
    }

    private fun new() {
        val name = JTextField(20)
        val version = JTextField(20)
        val width = SpinnerNumberModel(16, 1, 100, 1)
        val height = SpinnerNumberModel(16, 1, 100, 1)
        Dialog.showDialog("PictureGenerator: Create", { _: JDialog ->
            return@showDialog JPanel(GridLayout(4, 2)).apply {
                add(Label("Name:"))
                add(name)
                add(Label("Version:"))
                add(version)
                add(Label("Width:"))
                add(JSpinner(width))
                add(Label("Height:"))
                add(JSpinner(height))
            }
        }, {
            this.dispose()
            EditorFrame(PGPObject.empty(name.text, version.text, width.number.toInt(), height.number.toInt()))
        }, {})
    }

    private fun open(files: Vector<File>) {
        Dialog.showDialog("PictureManager: Open file(s)", { dialog: JDialog ->
            dialog.preferredSize = Dimension(600, 300)
            return@showDialog JPanel(BorderLayout()).apply {
                val list = JList(files)
                fun tryOpen(onOpen: (pgpObject: PGPObject, theFile: File) -> Unit) {
                    val file = files.removeAt(list.selectedIndex)
                    val pgpObject = PGPFileType.readXMLFile(file)
                    if (files.isEmpty()) {
                        dialog.dispose()
                        this@WelcomeFrame.dispose()
                    }
                    if (pgpObject != null) onOpen(pgpObject, file)
                }
                list.selectedIndex = 0
                add(JScrollPane(list))
                add(JPanel().apply {
                    add(JPanel(GridLayout(3, 1)).apply {
                        add(Label("Actions:"))
                        add(Button.create("Editor") { tryOpen { pgpObject: PGPObject, theFile: File -> openInEditor(pgpObject, theFile) } })
                        add(Button.create("Game") { tryOpen { pgpObject: PGPObject, _: File -> openAsGame(pgpObject) } })
                    })
                }, BorderLayout.EAST)
            }
        }, { dialog: JDialog ->
            Button.create("Close", "Close the dialog.") {
                dialog.dispose()
            }
        })
    }

    private fun openInEditor(pgpObject: PGPObject, file: File) {
        EditorFrame(pgpObject, file).toString()
    }

    private fun openAsGame(pgpObject: PGPObject) {
        GameFrame(pgpObject)
    }
}
