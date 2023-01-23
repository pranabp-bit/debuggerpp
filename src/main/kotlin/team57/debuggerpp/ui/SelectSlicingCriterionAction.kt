package team57.debuggerpp.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class SelectSlicingCriterionAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // Selects the line the cursor is currently on, regardless of any highlighting
        val editor = e.getData(CommonDataKeys.EDITOR)
        val offset = editor?.caretModel?.offset
        val document = editor?.document
        val lineNo = offset?.let { document?.getLineNumber(it) }?.plus(1)
        println("Selected slicing criterion line number is $lineNo")

        val fileName = e.getData(CommonDataKeys.VIRTUAL_FILE)?.name
        println("File Name is $fileName")
    }
}