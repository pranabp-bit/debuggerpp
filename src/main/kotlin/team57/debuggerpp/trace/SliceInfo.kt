package team57.debuggerpp.trace
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer.*
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import java.awt.Color

class SliceInfo: AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
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