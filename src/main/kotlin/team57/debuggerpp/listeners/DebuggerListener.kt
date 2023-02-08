package team57.debuggerpp.listeners

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.Content
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XDebuggerManagerListener
import team57.debuggerpp.slicer.ProgramSlice
import team57.debuggerpp.stepping.DppJavaDebugProcess
import team57.debuggerpp.ui.EditorSliceVisualizer
import team57.debuggerpp.ui.Icons
import team57.debuggerpp.ui.dependencies.ControlDependenciesPanel
import team57.debuggerpp.ui.dependencies.DataDependenciesPanel
import team57.debuggerpp.ui.dependencies.GraphPanel
import team57.debuggerpp.util.SourceLocation
import javax.swing.*


class DebuggerListener : XDebuggerManagerListener {
    override fun processStarted(debugProcess: XDebugProcess) {
        if (debugProcess !is DppJavaDebugProcess) {
            return
        }
        val session: XDebugSession = debugProcess.session
        val project = session.project
        val sliceVisualizer = EditorSliceVisualizer(project, debugProcess.slice)
        val dataDepPanel = DataDependenciesPanel(project)
        val controlDepPanel = ControlDependenciesPanel(project)
        val graphPanel = GraphPanel()

        session.addSessionListener(object : XDebugSessionListener {
            override fun sessionPaused() {
                ApplicationManager.getApplication().invokeAndWait {
                    updateDependenciesTabs(session, debugProcess.slice, dataDepPanel, controlDepPanel)
                }
            }
        })

        debugProcess.processHandler.addProcessListener(object : ProcessListener {
            override fun startNotified(processEvent: ProcessEvent) {
                initDebuggerUI(session, dataDepPanel, controlDepPanel, graphPanel)
                sliceVisualizer.start()
            }

            override fun processTerminated(processEvent: ProcessEvent) {}
            override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {
                sliceVisualizer.stop()
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {}
        })
    }

    private fun initDebuggerUI(
        debugSession: XDebugSession,
        dataDepComponent: JComponent,
        controlDepComponent: JComponent,
        graphComponent: JComponent
    ) {
        val ui: RunnerLayoutUi = debugSession.ui

        val sliceInfoComponent = JBTabbedPane()
        val sliceInfoTab: Content = ui.createContent(
                "sliceInfoTab",
                sliceInfoComponent,
                "Slicer4J",
                Icons.Logo,
                null
        )

        sliceInfoComponent.addTab("Data Dep", dataDepComponent)
        sliceInfoComponent.addTab("Control Dep", controlDepComponent)
        sliceInfoComponent.addTab("Graph", graphComponent)
        ui.addContent(sliceInfoTab)
        sliceInfoTab.isCloseable = false
    }

    private fun updateDependenciesTabs(
        session: XDebugSession, slice: ProgramSlice,
        dataPanel: DataDependenciesPanel, controlPanel: ControlDependenciesPanel
    ) {
        // Get current position
        val currentPosition = session.currentPosition ?: return
        // Find class name
        val file = PsiManager.getInstance(session.project).findFile(currentPosition.file)
            ?: return
        val element = file.findElementAt(currentPosition.offset)
        val className = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)?.qualifiedName
        // Get dependencies
        val location = className?.let { SourceLocation(it, currentPosition.line + 1) }
        val dependencies = slice.dependencies[location]
        val dataDependencies = dependencies?.data
        val controlDependencies = dependencies?.control
        // Update UI
        dataPanel.updateDependencies(dataDependencies)
        controlPanel.updateDependencies(controlDependencies)
    }
}