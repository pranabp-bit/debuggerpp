package team57.debuggerpp.execute

import com.intellij.execution.Executor
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.openapi.wm.ToolWindowId
import javax.swing.Icon

class DynamicSliceDebuggerExecutor : Executor() {
    companion object {
        const val ID = "DynamicSliceDebuggerExecutor"
    }

    override fun getToolWindowId(): String = ToolWindowId.RUN

    override fun getToolWindowIcon(): Icon = AllIcons.Actions.Run_anything

    override fun getIcon(): Icon = AllIcons.Actions.Run_anything

    override fun getDisabledIcon(): Icon = IconLoader.getDisabledIcon(icon)

    override fun getDescription(): String = "Debug selected configuration with dynamic slicing"

    override fun getActionName(): String = "Slicer4J"

    override fun getId(): String = ID

    override fun getStartActionText(): String = "Debug with Dynamic Slicing"

    override fun getStartActionText(configurationName: String): String =
        TextWithMnemonic.parse("Debug '%s' with Dynamic Slicing").replaceFirst("%s", configurationName).toString()

    override fun getContextActionId(): String = "DebugWithDynamicSlicing"

    override fun getHelpId(): String? = null
}