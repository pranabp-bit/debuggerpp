package team57.debuggerpp.trace

import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


class SliceInfo : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
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
}