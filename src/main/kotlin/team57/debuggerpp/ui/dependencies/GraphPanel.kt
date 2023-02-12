package team57.debuggerpp.ui.dependencies

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import team57.debuggerpp.trace.SubGraphBuilder

class GraphPanel: JScrollPane(){
    private val panel = JPanel()

    init {
        preferredSize = Dimension(100, 100)
        border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        setViewportView(panel)
        val depGraph: BufferedImage = ImageIO.read(File(System.getProperty("java.io.tmpdir") + "\\slice-graph.png"))
        val graphLabel = JLabel(ImageIcon(depGraph))
        panel.removeAll()
        panel.add(graphLabel)
        updateUI()
    }

    fun updateGraph(currentLineNum: Int) {
        val depGraph: BufferedImage = getGraph(currentLineNum)
        val graphLabel = JLabel(ImageIcon(depGraph))
        panel.removeAll()
        panel.add(graphLabel)
        updateUI()
    }

    private fun getGraph(currentLineNum: Int): BufferedImage {
        val subGraph: SubGraphBuilder = SubGraphBuilder()
        subGraph.generateSubGraph(currentLineNum)
        return ImageIO.read(File(System.getProperty("java.io.tmpdir") + "\\slice-subgraph.png"))
    }

}