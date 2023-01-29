package de.janorico.picturegenerator.gui.components.drawing

import java.awt.*
import java.awt.event.*
import javax.swing.JComponent
import kotlin.math.roundToInt

class PixelButton(
    group: PixelButtonGroup,
    private val pixelX: Int,
    private val pixelY: Int,
    private val threeState: Boolean = false,
    initialState: Int = if (threeState) UNDEFINED else WHITE,
    private val circleSizeDivider: Float = 3f,
) : JComponent() {
    companion object {
        const val WHITE = 0
        const val BLACK = 1
        const val UNDEFINED = 2
    }

    private val changedListeners = ArrayList<PixelButtonChangedListener>()
    var state = initialState
        set(value) {
            field = value
            for (changedListener in changedListeners) {
                changedListener.changed(pixelX, pixelY, value)
            }
            repaint()
        }

    init {
        minimumSize = group.minButtonSize
        preferredSize = group.buttonSize
        size = group.buttonSize
        group.addButton(this)
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                state = (state + 1) % (if (threeState) 3 else 2)
                group.drawing = true
                group.drawingState = state
            }

            override fun mouseReleased(e: MouseEvent?) {
                group.drawing = false
            }

            override fun mouseEntered(e: MouseEvent?) {
                if (group.drawing) state = group.drawingState
            }
        })
    }

    fun addChangedListener(changedListener: PixelButtonChangedListener): Boolean = changedListeners.add(changedListener)

    override fun paint(g: Graphics?) {
        if (g != null) {
            g.color = when (state) {
                WHITE -> Color.WHITE
                BLACK -> Color.BLACK
                UNDEFINED -> Color.WHITE
                else -> Color.RED
            }
            g.fillRect(0, 0, width - 1, height - 1)
            if (threeState && state == WHITE) {
                g.color = Color.BLACK
                val circleWidth = width.toFloat() / circleSizeDivider
                val circleHeight = height.toFloat() / circleSizeDivider
                g.fillOval(
                    ((width - circleWidth) / 2.0f).roundToInt(),
                    ((height - circleHeight) / 2.0f).roundToInt(),
                    circleWidth.roundToInt(),
                    circleHeight.roundToInt()
                )
            }
        }
    }
}
