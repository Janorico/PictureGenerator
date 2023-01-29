package de.janorico.picturegenerator.gui.components

import de.janorico.jgl.JGL
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.LineBorder

class ColorPicker(initialColor: Color) : JLabel() {
    var color = initialColor
        set(value) {
            field = value
            background = value
            updateText()
        }

    init {
        border = LineBorder(Color.BLACK)
        background = initialColor
        foreground = Color.GRAY
        isOpaque = true
        updateText()
        val size = Dimension(64, 24)
        setSize(size)
        minimumSize = size
        preferredSize = size
        maximumSize = size
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                val result = JColorChooser.showDialog(JGL.dialogOwner, "Choose color", color)
                if (result != null) color = result
            }
        })
    }

    fun updateText() {
        text = "#" + Integer.toHexString(color.rgb)
    }
}
