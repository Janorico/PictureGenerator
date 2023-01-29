package de.janorico.picturegenerator.gui.components.drawing

import java.awt.Dimension

class PixelButtonGroup(
    private val changedListener: PixelButtonChangedListener? = null,
    val buttonSize: Dimension = Dimension(20, 20),
    val minButtonSize: Dimension = Dimension(8, 8),
) {
    var drawing = false
    var drawingState = PixelButton.WHITE

    private val buttons = ArrayList<PixelButton>()

    fun addButton(button: PixelButton): Boolean {
        if (changedListener != null) button.addChangedListener(changedListener)
        return buttons.add(button)
    }

    // fun removeButton(button: PixelButton): Boolean = buttons.remove(button)
}
