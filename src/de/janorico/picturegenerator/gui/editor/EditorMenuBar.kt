package de.janorico.picturegenerator.gui.editor

import de.janorico.jgl.helpers.MenuItem
import de.janorico.picturegenerator.gui.WelcomeFrame
import java.awt.event.*
import javax.swing.*

class EditorMenuBar(editor: EditorFrame) : JMenuBar() {
    init {
        add(JMenu("File").apply {
            setMnemonic('F')
            add(MenuItem.create("Save", 'S', KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)) { editor.save(false) })
            add(MenuItem.create("Save as...", 'a', KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK)) {
                editor.save(true)
            })
            add(JMenu("Export").apply {
                setMnemonic('E')
                add(MenuItem.create("Picture...", "Export the picture to an image.") { editor.exportToPicture() })
                add(MenuItem.create("Arduino...", "Create a byte array in Arduino language.") { editor.arduino() })
            })
            add(MenuItem.create("Close", 'C', KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK)) {
                editor.dispose()
                WelcomeFrame()
                System.gc()
            })
            add(MenuItem.create("Exit") { editor.exit() })
        })
        add(JMenu("Project").apply {
            setMnemonic('P')
            add(MenuItem.create("Change name...", "Change projects name.", 'n') { editor.changeName() })
            add(MenuItem.create("Change version...", "Change projects version.", 'v') { editor.changeVersion() })
        })
    }
}
