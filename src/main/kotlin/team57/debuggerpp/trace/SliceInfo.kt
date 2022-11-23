package team57.debuggerpp.trace

import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer.SELECTION
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import java.awt.Color
import java.awt.Font
import javax.swing.*


class SliceInfo: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        grayOutNonSliceLines(e)
        createSlicerInfoView(e)
    }

    private fun createSlicerInfoView(event: AnActionEvent) {
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val editor = event.getRequiredData(CommonDataKeys.EDITOR)

        val session: XDebugSession? = XDebuggerManager.getInstance(project).currentSession
        val ui: RunnerLayoutUi = session!!.ui
        ui.defaults.initTabDefaults(1000, "Slicer4J", null)

//        val view = SlicerInfoTab()
        // hard-coded data just for demo purposes
        val dataLabel = JLabel("<html>From: <br/> x: Main.java at 3<br/> y: Main.java at 4<br/><br/>To: <br/> z: Main.java at 12</html>",
                    SwingConstants.LEFT)
        val controlLabel = JLabel("<html>From: <br/> x: Main.java at 7<br/><br/>To: <br/></html>",
                SwingConstants.LEFT)
        val dataPanel = JPanel()
        val controlPanel = JPanel()
        val graphPanel = JPanel()
        dataPanel.add(dataLabel)
        controlPanel.add(controlLabel)
        val dataDependencies: Content = ui.createContent(
                "SlicerContentId",
                dataPanel,
                "Data Dep.", null, null
        )
        val controlDependencies: Content = ui.createContent(
                "SlicerContentId",
                controlPanel,
                "Control Dep.", null, null
        )
        val graph: Content = ui.createContent(
                "SlicerContentId",
                graphPanel,
                "Graph", null, null
        )
        ui.addContent(dataDependencies)
        ui.addContent(controlDependencies)
        ui.addContent(graph)
        dataDependencies.isCloseable = false
        controlDependencies.isCloseable = false
        graph.isCloseable = false
//        Disposer.register(session.runContentDescriptor, view)
    }

    private fun grayOutNonSliceLines(event: AnActionEvent) {
        // TO-DO: fix always select project file - right now it chooses the editor window that is selected
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val document: Document = editor.getDocument()
        val markupModel: MarkupModel = editor.getMarkupModel();

        val attributes: TextAttributes = TextAttributes()
        val sliceHighlightingColor = Color(77, 77, 77)
        attributes.setForegroundColor(sliceHighlightingColor)

        var nonSliceLines = arrayOf(5, 6, 9, 10, 11)
        for (line in nonSliceLines) {
            markupModel.addLineHighlighter(line - 1, SELECTION + 1, attributes)
        }

//        markupModel.removeAllHighlighters()
    }
}