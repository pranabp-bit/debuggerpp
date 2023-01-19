package team57.debuggerpp.listeners

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Key
import com.intellij.ui.content.Content
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManagerListener
import com.intellij.xdebugger.XSourcePosition
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


class DebuggerListener: XDebuggerManagerListener {

    override fun processStarted(debugProcess: XDebugProcess) {
        val debugSession: XDebugSession = debugProcess.session
        debugProcess.processHandler.addProcessListener(object : ProcessListener {
            override fun startNotified(processEvent: ProcessEvent) {
                initDebuggerUI(debugSession)
                grayOutNonSliceLines(debugSession)
            }

            override fun processTerminated(processEvent: ProcessEvent) {}
            override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {
                removeLineGraying(debugSession)
            }
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {}
        })
    }

    private fun initDebuggerUI(debugSession: XDebugSession) {
        val ui: RunnerLayoutUi = debugSession.ui
        ui.defaults.initTabDefaults(1000, "Slicer4J", null)

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
    }

    private fun grayOutNonSliceLines(debugSession: XDebugSession) {

//        val position: XSourcePosition? = debugSession.topFramePosition
//
//        if (position == null) {
//            return
//        }
//
//        val editor = FileEditorManager.getInstance(debugSession.project).getSelectedEditor(position.file)

        val editor = FileEditorManager.getInstance(debugSession.project).getSelectedEditor()
        if (editor is TextEditor) {
            val markupModel: MarkupModel = editor.editor.markupModel

            val attributes = TextAttributes()
            val sliceHighlightingColor = Color(77, 77, 77)
            attributes.foregroundColor = sliceHighlightingColor

            var nonSliceLines = arrayOf(7, 8, 10, 11, 13, 15)
            for (line in nonSliceLines) {
                markupModel.addLineHighlighter(line - 1, HighlighterLayer.SELECTION + 1, attributes)
            }
//                markupModel.removeAllHighlighters()
            return
        }
    }

    private fun removeLineGraying(debugSession: XDebugSession) {
        val position: XSourcePosition = debugSession.topFramePosition ?: return
        val editor = FileEditorManager.getInstance(debugSession.project).getSelectedEditor(position.file)

        if (editor is TextEditor) {
            val markupModel: MarkupModel = editor.editor.markupModel
            markupModel.removeAllHighlighters()
        }
    }
}