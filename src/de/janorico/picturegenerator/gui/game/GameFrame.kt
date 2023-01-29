package de.janorico.picturegenerator.gui.game

import de.janorico.jgl.JGL
import de.janorico.jgl.helpers.OptionPane
import de.janorico.picturegenerator.data.pgp.PGPObject
import de.janorico.picturegenerator.gui.components.drawing.*
import de.janorico.picturegenerator.logos
import java.awt.*
import javax.swing.*

class GameFrame(private val pgpObject: PGPObject) : JFrame() {
    init {
        JGL.dialogOwner = this
        minimumSize = add(JPanel(GridLayout(2, 2)).apply RootPanel@{
            add(JLabel("<html>Width: ${pgpObject.getWidth()}<br>Height: ${pgpObject.getHeight()}</html>", JLabel.CENTER))
            val columns = getColumns()
            val rows = getRows()
            add(JPanel(GridLayout(1, columns.size)).apply ColumnsPanel@{
                for (column in columns) {
                    add(JLabel(buildString {
                        append("<html>")
                        for (item in column) append("$item<br>")
                    }.removeSuffix("<br>") + "</html>", JLabel.CENTER).apply { verticalAlignment = JLabel.BOTTOM })
                }
            })
            add(JPanel(GridLayout(rows.size, 1)).apply RowPanel@{
                for (row in rows) {
                    add(JLabel(buildString {
                        for (item in row) append("$item  ")
                    }.removeSuffix("  "), JLabel.RIGHT).apply { verticalAlignment = JLabel.CENTER })
                }
            })
            add(JPanel(GridLayout(pgpObject.getHeight(), pgpObject.getWidth())).apply GamePanel@{
                val gameData = PGPObject.emptyArray(pgpObject.getWidth(), pgpObject.getHeight())
                val group = PixelButtonGroup(object : PixelButtonChangedListener {
                    override fun changed(x: Int, y: Int, value: Int) {
                        if (value == PixelButton.BLACK || value == PixelButton.WHITE) gameData[y][x] = (value == PixelButton.BLACK)
                        if (gameData.contentEquals(pgpObject.data)) OptionPane.showInformation("Ready! You make the picture correct!")
                    }
                }, Dimension(15, 15))
                for (y: Int in 0 until pgpObject.getHeight()) {
                    for (x: Int in 0 until pgpObject.getWidth()) {
                        val button = PixelButton(group, x, y, true)
                        this.add(button)
                    }
                }
            })
        }).minimumSize

        this.pack()
        this.iconImages = logos
        this.defaultCloseOperation = DISPOSE_ON_CLOSE
        //this.extendedState = MAXIMIZED_BOTH
        this.setLocationRelativeTo(null)
        this.title = "PictureGenerator Game"
        this.isVisible = true
    }

    fun getRows(): Array<ArrayList<Int>> {
        val height = pgpObject.getHeight()
        return Array(height) {
            val column = pgpObject.data[it]
            val list = ArrayList<Int>()
            var current = 0
            for (boolIdx in column.indices) {
                val bool = column[boolIdx]
                if (bool) {
                    current++
                }
                if (((boolIdx == (column.size - 1)) || !bool) && current > 0) {
                    list.add(current)
                    current = 0
                }
            }
            return@Array list
        }

    }

    fun getColumns(): Array<ArrayList<Int>> {
        val width = pgpObject.getWidth()
        val height = pgpObject.getHeight()
        val columns = Array(width) { x: Int ->
            return@Array Array(height) SubArray@{ y: Int ->
                return@SubArray pgpObject.data[y][x]
            }
        }
        return Array(width) {
            val row = columns[it]
            val list = ArrayList<Int>()
            var current = 0
            for (boolIdx in row.indices) {
                val bool = row[boolIdx]
                if (bool) {
                    current++
                }
                if (((boolIdx == (row.size - 1)) || !bool) && current > 0) {
                    list.add(current)
                    current = 0
                }
            }
            return@Array list
        }

    }
}
