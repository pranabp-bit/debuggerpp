package team57.debuggerpp.ui

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiTreeUtil
import team57.debuggerpp.execute.DynamicSliceDebuggerExecutor
import team57.debuggerpp.util.SourceLocation

class SelectSlicingCriterionAction : AnAction() {
    companion object {
        private val LOG = Logger.getInstance(SelectSlicingCriterionAction::class.java)
        val SLICING_CRITERIA_KEY = Key.create<SourceLocation>("debuggerpp.slicing-criteria")
    }

    override fun actionPerformed(e: AnActionEvent) {
        // Selects the line the cursor is currently on, regardless of any highlighting
        val editor = e.getData(CommonDataKeys.EDITOR)!!
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)!!
        val project = editor.project!!
        val offset = editor.caretModel.offset
        val document = editor.document
        val lineNo = document.getLineNumber(offset) + 1
        LOG.info("Selected slicing criterion line number is $lineNo")

        // TODO: handle these situations
        val element = psiFile.findElementAt(offset)
            ?: throw ExecutionException("Cannot find any element at this location")
        val clazz = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
            ?: throw ExecutionException("This location is not inside a Java class")

        val fileName = e.getData(CommonDataKeys.VIRTUAL_FILE)?.name
        println("File Name is $fileName")
        LOG.info("Class Name is ${clazz.qualifiedName}")
        startSliceDebugger(project, SourceLocation(clazz.qualifiedName!!, lineNo))
    }

    private fun startSliceDebugger(project: Project, criteria: SourceLocation) {
        // TODO: notify the user
        val configuration: RunnerAndConfigurationSettings = RunManager.getInstance(project).selectedConfiguration
            ?: throw IllegalStateException("no selectedConfiguration")

        val builder = ExecutionEnvironmentBuilder.create(
            project,
            DynamicSliceDebuggerExecutor.instance!!,
            configuration.configuration
        )
        val env = builder.build()
        env.putUserData(SLICING_CRITERIA_KEY, criteria)
        ExecutionManager.getInstance(project).restartRunProfile(env)
    }
}