package team57.debuggerpp.execute

import junit.framework.TestCase

class DynamicSliceDebuggerExecutorTest : TestCase() {
    private val executor = DynamicSliceDebuggerExecutor()

    fun testGetStartActionTextBasic() {
        val expected = "Debug 'Basic' with Dynamic Slicing using Debugger++"
        val actual = executor.getStartActionText("Basic")
        assertEquals(expected, actual)
    }

    fun testGetStartActionTextEmpty() {
        val expected = "Debug '' with Dynamic Slicing using Debugger++"
        val actual = executor.getStartActionText("")
        assertEquals(expected, actual)
    }
}