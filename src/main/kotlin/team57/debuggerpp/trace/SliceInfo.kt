package team57.debuggerpp.trace
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
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

class SliceInfo: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        grayOutNonSliceLines(e)
        createSlicerInfoView(e)
    }

    fun createSlicerInfoView(event: AnActionEvent) {
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val editor = event.getRequiredData(CommonDataKeys.EDITOR)

        val session: XDebugSession? = XDebuggerManager.getInstance(project).getCurrentSession()
        val ui: RunnerLayoutUi = session!!.getUI()
        ui.defaults.initTabDefaults(1000, "Slicer4J", null)

        val view = SlicerInfoTab()
        val content: Content = ui.createContent(
                "SlicerContentId",
                view,
                "Slicer4J", null, null
        )
        content.isCloseable = false
        ui.addContent(content, 1000, PlaceInGrid.left, false)
//        Disposer.register(session.runContentDescriptor, view)
    }

    fun grayOutNonSliceLines(event: AnActionEvent) {
        // TO-DO: fix always select project file - right now it chooses the editor window that is selected
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val document: Document = editor.getDocument()
        val markupModel: MarkupModel = editor.getMarkupModel();

        val attributes: TextAttributes = TextAttributes()
        val sliceHighlightingColor = Color(77, 77, 77)
        attributes.setForegroundColor(sliceHighlightingColor)

        var nonSliceLines = arrayOf(3, 6, 8, 9, 14)
        for (line in nonSliceLines) {
            markupModel.addLineHighlighter(line - 1, SELECTION + 1, attributes)
        }

//        markupModel.removeAllHighlighters()
    }
}