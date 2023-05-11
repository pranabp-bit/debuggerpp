package team57.debuggerpp.listeners

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
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
import com.mongodb.client.MongoClients
import org.bson.Document
import team57.debuggerpp.dbgcontroller.DppJavaDebugProcess
import team57.debuggerpp.dbgcontroller.DppJvmSteppingCommandProvider
import team57.debuggerpp.slicer.ProgramSlice
import team57.debuggerpp.ui.EditorSliceVisualizer
import team57.debuggerpp.ui.Icons
import team57.debuggerpp.ui.dependencies.ControlDependenciesPanel
import team57.debuggerpp.ui.dependencies.DataDependenciesPanel
import team57.debuggerpp.ui.dependencies.GraphPanel
import team57.debuggerpp.util.SourceLocation
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0
class DebuggerListener : XDebuggerManagerListener {
    companion object {
        private val LOG = Logger.getInstance(DebuggerListener::class.java)
    }

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

        // Update Debugger++ tabs when paused
        session.addSessionListener(object : XDebugSessionListener {
            override fun sessionPaused() {
                ApplicationManager.getApplication().invokeAndWait {
                    updateDependenciesTabs(session, debugProcess.slice, dataDepPanel, controlDepPanel, graphPanel)
                }
            }
        })

        // Listen to process events to enable/disable line greying
        debugProcess.processHandler.addProcessListener(object : ProcessListener {
            override fun startNotified(processEvent: ProcessEvent) {
                initDebuggerUI(session, dataDepPanel, controlDepPanel, graphPanel)
                sliceVisualizer.start()
            }

            override fun processTerminated(processEvent: ProcessEvent) {}
            override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {
                sliceVisualizer.stop()
                emptyDependenciesTabs(dataDepPanel, controlDepPanel, graphPanel)
                val kotlinMap1: KMutableProperty0<HashMap<String, Int>> = DppJvmSteppingCommandProvider::actionCounts
                val javaMap: java.util.HashMap<String, Int> = kotlinMap1.get()
                val kotlinMap: MutableMap<String, Int> = javaMap.toMutableMap()

                for ((key, value) in kotlinMap) {
                    println("$key = $value")
                }

                val connectionString = "mongodb+srv://anyuser:anyuser@cluster0.w95f5mz.mongodb.net/?retryWrites=true&w=majority"
                val client = MongoClients.create(connectionString)
                val database = client.getDatabase("test")
                val collection = database.getCollection("myCollection")
                val document = Document(kotlinMap)
                collection.insertOne(document)
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
                "Debugger++",
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
        dataPanel: DataDependenciesPanel, controlPanel: ControlDependenciesPanel,
        graphPanel: GraphPanel
    ) {
        // Get current position
        val currentPosition = session.currentPosition ?: return
        val currentLineNum: Int = currentPosition.line + 1
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
        dataPanel.updateDependencies(dataDependencies, location)
        controlPanel.updateDependencies(controlDependencies, location)
        graphPanel.updateGraph(currentLineNum, slice)
    }

    private fun emptyDependenciesTabs(
        dataPanel: DataDependenciesPanel,
        controlPanel: ControlDependenciesPanel,
        graphPanel: GraphPanel
    ) {
        dataPanel.emptyPanel()
        controlPanel.emptyPanel()
        graphPanel.emptyPanel()
    }
}