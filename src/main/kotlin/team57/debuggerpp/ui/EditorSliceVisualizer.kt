package team57.debuggerpp.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import team57.debuggerpp.slicer.ProgramSlice
import java.awt.Color


class EditorSliceVisualizer(private val project: Project, private val slice: ProgramSlice) {
    companion object {
        private val LOG = Logger.getInstance(EditorSliceVisualizer::class.java)
        private val greyOutAttributes = TextAttributes()

        init {
            greyOutAttributes.foregroundColor = Color(77, 77, 77)
        }
    }

    private val messageBusConnection = project.messageBus.connect()
    private val psiManager = PsiManager.getInstance(project)

    fun start() {
        LOG.info("Start")
        visualizeInExistingEditors()
        messageBusConnection
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                override fun fileOpenedSync(
                    source: FileEditorManager, file: VirtualFile,
                    editorsWithProviders: MutableList<FileEditorWithProvider>
                ) {
                    super.fileOpenedSync(source, file, editorsWithProviders)
                    for (fileEditor in editorsWithProviders.map { x -> x.fileEditor }) {
                        if (fileEditor is TextEditor)
                            visualizeInEditor(fileEditor)
                    }
                }
            })
    }

    fun stop() {
        LOG.info("Stop")
        messageBusConnection.disconnect()
        ApplicationManager.getApplication().invokeAndWait { removeAllGreyOuts() }
    }

    private fun visualizeInExistingEditors() {
        for (fileEditor in FileEditorManager.getInstance(project).allEditors) {
            if (fileEditor is TextEditor)
                visualizeInEditor(fileEditor)
        }
    }

    private fun removeAllGreyOuts() {
        for (fileEditor in FileEditorManager.getInstance(project).allEditors) {
            if (fileEditor is TextEditor) {
                val toRemove = ArrayList<RangeHighlighter>()
                for (highlighter in fileEditor.editor.markupModel.allHighlighters) {
                    if (highlighter.getTextAttributes(null) == greyOutAttributes) {
                        toRemove.add(highlighter)
                    }
                }
                for (highlighter in toRemove) {
                    fileEditor.editor.markupModel.removeHighlighter(highlighter)
                }
            }
        }
    }

    private fun visualizeInEditor(textEditor: TextEditor) {
        LOG.info("visualizeInEditor $textEditor")
        val file = psiManager.findFile(textEditor.file)
        if (file !is PsiJavaFile)
            return

        val sliceLines = HashSet<Int>()
        for (clazz in file.classes) {
            slice.sliceLinesUnordered[clazz.qualifiedName]?.let { lines ->
                sliceLines.addAll(lines)
            }
        }

        for (line in 1..textEditor.editor.document.lineCount) {
            if (sliceLines.contains(line))
                continue
            textEditor.editor.markupModel.addLineHighlighter(
                line - 1,
                HighlighterLayer.SELECTION + 1,
                greyOutAttributes
            )
        }
    }
}
