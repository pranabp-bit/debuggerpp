package team57.debuggerpp.execute

import com.intellij.execution.Executor
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.openapi.wm.ToolWindowId
import team57.debuggerpp.ui.Icons
import javax.swing.Icon

class DynamicSliceDebuggerExecutor : Executor() {
    companion object {
        const val ID = "DynamicSliceDebuggerExecutor"
    }

    override fun getToolWindowId(): String = ToolWindowId.RUN

    override fun getToolWindowIcon(): Icon = Icons.Logo

    override fun getIcon(): Icon = Icons.Logo

    override fun getDisabledIcon(): Icon = IconLoader.getDisabledIcon(icon)

    override fun getDescription(): String = "Debug selected configuration with dynamic slicing using Debugger++"

    override fun getActionName(): String = "Debugger++"

    override fun getId(): String = ID

    override fun getStartActionText(): String = "Debug with Dynamic Slicing using Debugger++"

    override fun getStartActionText(configurationName: String): String =
        TextWithMnemonic.parse("Debug '%s' with Dynamic Slicing using Debugger++").replaceFirst("%s", configurationName).toString()

    override fun getContextActionId(): String = "DebugWithDynamicSlicing"

    override fun getHelpId(): String? = null
}