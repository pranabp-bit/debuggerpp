package team57.debuggerpp.ui.dependencies

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*

class GraphPanel: JScrollPane(){
    private val panel = JPanel()

    init {
        preferredSize = Dimension(100, 100)
        border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        setViewportView(panel)
        updateGraph()
    }

    private fun updateGraph() {
        val depGraph: BufferedImage = getGraph()
        val graphLabel = JLabel(ImageIcon(depGraph))
        panel.removeAll()
        panel.add(graphLabel)
        updateUI()
    }

    private fun getGraph(): BufferedImage {
        return ImageIO.read(File(System.getProperty("java.io.tmpdir") + "\\slice-graph.png"))
    }

}