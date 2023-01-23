package team57.debuggerpp.listeners

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.openapi.util.Key
import com.intellij.ui.content.Content
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManagerListener
import team57.debuggerpp.trace.SliceJavaDebugProcess
import team57.debuggerpp.ui.EditorSliceVisualizer
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class DebuggerListener : XDebuggerManagerListener {
    override fun processStarted(debugProcess: XDebugProcess) {
        if (debugProcess !is SliceJavaDebugProcess) {
            return
        }
        val debugSession: XDebugSession = debugProcess.session
        val sliceVisualizer = EditorSliceVisualizer(debugProcess.session.project, debugProcess.slice)
        debugProcess.processHandler.addProcessListener(object : ProcessListener {
            override fun startNotified(processEvent: ProcessEvent) {
                initDebuggerUI(debugSession)
                sliceVisualizer.start()
            }

            override fun processTerminated(processEvent: ProcessEvent) {}
            override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {
                sliceVisualizer.stop()
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {}
        })
    }

    private fun initDebuggerUI(debugSession: XDebugSession) {
        val ui: RunnerLayoutUi = debugSession.ui
        ui.defaults.initTabDefaults(1000, "Slicer", null)

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
}