package team57.debuggerpp.ui.dependencies

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StatusText
import team57.debuggerpp.slicer.ProgramSlice
import team57.debuggerpp.util.Utils
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.*


abstract class DependenciesPanel(protected val project: Project) : JPanel() {
    companion object {
        val YELLOW: Color = Color.decode("#FFC000")
        val GREEN: Color = Color.decode("#00B050")
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
    }

    protected fun addTitleLabel(title: String, foreground: Color) {
        val l = JLabel("${title}:")
        l.foreground = foreground
        l.border = BorderFactory.createEmptyBorder(5, 0, 5, 0)
        add(l)
    }

    protected fun addEmptyLabel() {
        val l = JLabel("None")
        l.foreground = JBColor.GRAY
        l.border = BorderFactory.createEmptyBorder(0, 10, 0, 0)
        add(l)
    }

    protected fun addNoDependenciesMessage(name: String) {
        val l = JLabel("$name dependencies of this line is unavailable")
        l.border = BorderFactory.createEmptyBorder(5, 0, 5, 0)
        add(l)
    }

    protected fun addDependencyLine(prefix: String, dependency: ProgramSlice.Dependency) {
        val l = JButton()
        var displayName = dependency.location.clazz
        var lineText = ""

        Utils.findPsiFile(dependency.location.clazz, project)?.let { file ->
            val logicalLineNo = dependency.location.lineNo - 1
            displayName = file.name
            l.addActionListener {
                OpenFileDescriptor(project, file.virtualFile, logicalLineNo, Int.MAX_VALUE)
                    .navigate(false)
            }
            val document = FileDocumentManager.getInstance().getDocument(file.virtualFile)
            if (document != null) {
                val start = document.getLineStartOffset(logicalLineNo)
                val end = document.getLineEndOffset(logicalLineNo)
                lineText = document.getText(TextRange(start, end))
            }
        }

        l.text = "<html>${prefix}" +
                "<font color='#5693E2'>Line ${dependency.location.lineNo} ($displayName)</font>" +
                "&nbsp&nbsp" +
                "<font color='#999999'>$lineText</font>" +
                "</html>"

        l.isFocusPainted = false
        l.margin = JBUI.emptyInsets()
        l.isContentAreaFilled = false
        l.isBorderPainted = false
        l.isOpaque = false
        l.border = BorderFactory.createEmptyBorder(0, 10, 0, 0)
        l.horizontalAlignment = SwingConstants.LEFT
        l.maximumSize = Dimension(l.preferredSize.width, 18)

        add(l)
    }

    fun emptyPanel(text: String) {
        removeAll()
        val statusText = object : StatusText(this) {
            override fun isStatusVisible(): Boolean {
                return true
            }
        }
        statusText.text = text
        layout = GridBagLayout()
        add(statusText.component)
        updateUI()
    }
}