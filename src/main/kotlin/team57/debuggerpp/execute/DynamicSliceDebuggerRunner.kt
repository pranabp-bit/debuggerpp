package team57.debuggerpp.execute

import com.intellij.debugger.DebugEnvironment
import com.intellij.debugger.DebuggerManagerEx
import com.intellij.debugger.DefaultDebugEnvironment
import com.intellij.debugger.impl.GenericDebuggerRunner
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.impl.XDebugSessionImpl
import team57.debuggerpp.slicer.JavaSlicer
import team57.debuggerpp.slicer.ProgramSlice
import team57.debuggerpp.trace.SliceJavaDebugProcess
import java.awt.Desktop
import java.util.concurrent.atomic.AtomicReference

class DynamicSliceDebuggerRunner : GenericDebuggerRunner() {
    companion object {
        const val ID = "DynamicSliceDebuggerRunner"
        private val SLICE_KEY = Key.create<ProgramSlice>("debuggerpp.programs-slice")
        private val LOG = Logger.getInstance(DynamicSliceDebuggerRunner::class.java)
    }

    private val slicer = JavaSlicer()

    override fun getRunnerId() = ID

    override fun canRun(executorId: String, profile: RunProfile) = executorId == DynamicSliceDebuggerExecutor.ID

    override fun createContentDescriptor(
        state: RunProfileState,
        env: ExecutionEnvironment
    ): RunContentDescriptor? {
        val programSlice = runDynamicSlicing(env)
        env.putUserData(SLICE_KEY, programSlice)
        return super.createContentDescriptor(state, env)
    }

    @Throws(ExecutionException::class)
    override fun attachVirtualMachine(
        state: RunProfileState?,
        env: ExecutionEnvironment,
        connection: RemoteConnection?,
        pollTimeout: Long
    ): RunContentDescriptor? {
        val ex = AtomicReference<ExecutionException?>()
        val result = AtomicReference<RunContentDescriptor>()
        ApplicationManager.getApplication().invokeAndWait {
            val environment: DebugEnvironment = DefaultDebugEnvironment(env, state!!, connection, pollTimeout)
            try {
                val debuggerSession =
                    DebuggerManagerEx.getInstanceEx(env.project).attachVirtualMachine(environment)
                        ?: return@invokeAndWait
                val debugProcess = debuggerSession.process
                result.set(
                    XDebuggerManager.getInstance(env.project).startSession(env, object : XDebugProcessStarter() {
                        override fun start(session: XDebugSession): XDebugProcess {
                            val sessionImpl = session as XDebugSessionImpl
                            val executionResult = debugProcess.executionResult
                            sessionImpl.addExtraActions(*executionResult.actions)
                            if (executionResult is DefaultExecutionResult) {
                                sessionImpl.addRestartActions(*executionResult.restartActions)
                            }
                            val slice = env.getUserData(SLICE_KEY)
                            return SliceJavaDebugProcess.create(session, debuggerSession, slice)
                        }
                    }).runContentDescriptor
                )
            } catch (e: ExecutionException) {
                ex.set(e)
            }
        }
        if (ex.get() != null)
            throw ex.get()!!
        return result.get()
    }

    private fun runDynamicSlicing(env: ExecutionEnvironment): ProgramSlice {
        val task =
            object : Task.WithResult<ProgramSlice, Exception>(env.project, "Executing Dynamic Slicing", true) {
                override fun compute(indicator: ProgressIndicator): ProgramSlice {
                    val slicingCriteriaFile = "Main"
                    val slicingCriteriaLineNo = 6

                    val outputDirectory = kotlin.io.path.createTempDirectory("slicer4j-outputs-")
                    val staticLog = outputDirectory.resolve("slicer4j-static.log")
                    val icdgLog = outputDirectory.resolve("icdg.log")
                    Desktop.getDesktop().open(outputDirectory.toFile())

                    indicator.text = "Instrumenting"
                    val (instrumentedState, processDirs) = slicer.instrument(env, outputDirectory, staticLog)

                    indicator.text = "Collecting trace"
                    val executionResult = instrumentedState.execute(env.executor, env.runner)!!
                    val trace = slicer.collectTrace(executionResult, outputDirectory, staticLog)

                    indicator.text = "Creating dynamic control flow graph"
                    val graph = slicer.createDynamicControlFlowGraph(icdgLog, trace, processDirs)

                    indicator.text = "Locating slicing criteria"
                    val slicingCriteria =
                        slicer.locateSlicingCriteria(graph, slicingCriteriaFile, slicingCriteriaLineNo)
                    if (slicingCriteria.isEmpty()) {
                        throw ExecutionException(
                            "Unable to locate $slicingCriteriaFile:$slicingCriteriaLineNo " +
                                    "in the dynamic control flow graph"
                        )
                    }

                    indicator.text = "Slicing"
                    return slicer.slice(graph, slicingCriteria, processDirs, outputDirectory)
                }
            }
        task.queue() // This runs synchronously for modal tasks
        return task.result!!
    }
}