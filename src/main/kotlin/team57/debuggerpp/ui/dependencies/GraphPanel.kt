package team57.debuggerpp.ui.dependencies

import com.intellij.util.ui.StatusText
import java.awt.Dimension
import java.awt.GridBagLayout
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
        val graphImage = ImageIcon(depGraph).image
        val scaledImage = graphImage.getScaledInstance(graphImage.getWidth(null) / 2, graphImage.getHeight(null)/2, java.awt.Image.SCALE_SMOOTH)
        val graphLabel = JLabel(ImageIcon(scaledImage))
        panel.removeAll()
        panel.add(graphLabel)
        updateUI()
    }

    private fun getGraph(currentLineNum: Int): BufferedImage {
        val subGraph: SubGraphBuilder = SubGraphBuilder()
        subGraph.generateSubGraph(currentLineNum)
        return ImageIO.read(File(System.getProperty("java.io.tmpdir") + "\\slice-subgraph.png"))
    }

    fun emptyPanel() {
        panel.removeAll()
        val statusText = object : StatusText(this) {
            override fun isStatusVisible(): Boolean {
                return true
            }
        }
        statusText.text = "Dependencies Graph is not available"
        panel.layout = GridBagLayout()
        panel.add(statusText.component)
        updateUI()
    }

}