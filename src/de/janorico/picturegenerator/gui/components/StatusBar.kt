package de.janorico.picturegenerator.gui.components

import java.awt.*
import javax.swing.*

class StatusBar(delay: Int = 5000) : JPanel(BorderLayout()) {
    private val statusLabel = JLabel()

    private val timer = Timer(delay) {
        statusLabel.text = ""
    }.apply {
        isRepeats = false
    }


    init {
        add(statusLabel)
        minimumSize = Dimension(minimumSize.width, 24)
        preferredSize = Dimension(minimumSize.width, 24)
        size = Dimension(minimumSize.width, 24)
        background = Color.LIGHT_GRAY
        // border = LineBorder(Color.BLACK)
    }

    fun displayMessage(message: String) {
        statusLabel.text = message
        timer.restart()
    }
}
