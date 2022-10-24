package team57.debuggerpp.trace

import com.intellij.execution.Executor
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.openapi.wm.ToolWindowId
import javax.swing.Icon

class TraceExecutor : Executor() {
    companion object {
        const val EXECUTOR_ID = "Trace"
    }

    override fun getToolWindowId(): String = ToolWindowId.RUN

    override fun getToolWindowIcon(): Icon = AllIcons.Toolwindows.ToolWindowRun

    override fun getIcon(): Icon = AllIcons.Actions.Run_anything

    override fun getDisabledIcon(): Icon = IconLoader.getDisabledIcon(icon)

    override fun getDescription(): String = "Run selected configuration with tracing"

    override fun getActionName(): String = "Trace"

    override fun getId(): String = EXECUTOR_ID

    override fun getStartActionText(): String = "Run with Tracing"

    override fun getStartActionText(configurationName: String): String =
        TextWithMnemonic.parse("Run '%s' with Tracing").replaceFirst("%s", configurationName).toString()

    override fun getContextActionId(): String = "Trace"

    override fun getHelpId(): String? = null
}