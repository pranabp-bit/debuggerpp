package team57.debuggerpp.trace

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.impl.DebuggerSupport
import com.intellij.xdebugger.impl.actions.DebuggerActionHandler
import com.intellij.xdebugger.impl.actions.XDebuggerActionBase

class TestButtonJava : XDebuggerActionBase() {

//    override fun actionPerformed(e: AnActionEvent) {
//
//    }

    override fun getHandler(debuggerSupport: DebuggerSupport): DebuggerActionHandler {
        return debuggerSupport.stepOverHandler
    }

}