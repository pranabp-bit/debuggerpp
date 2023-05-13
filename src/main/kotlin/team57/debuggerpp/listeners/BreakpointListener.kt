package team57.debuggerpp.listeners

import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import java.time.LocalDateTime

class BreakpointListener : XBreakpointListener<XBreakpoint<*>> {
    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        log("Breakpoint added", breakpoint)
    }

    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        log("Breakpoint removed", breakpoint)
    }
open
    override fun breakpointChanged(breakpoint: XBreakpoint<*>) {
        log("Breakpoint changed", breakpoint)
    }

    private fun log(action: String, breakpoint: XBreakpoint<*>) {
        val type = breakpoint.javaClass.simpleName
        val lineNumber = breakpoint.sourcePosition?.line ?: -1
        val timestamp = LocalDateTime.now().toString()
        println("$timestamp - $action $type at line $lineNumber")
    }
}