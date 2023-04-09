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
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.xdebugger.*
import com.intellij.xdebugger.impl.XDebugSessionImpl
import team57.debuggerpp.dbgcontroller.DppJavaDebugProcess
import team57.debuggerpp.slicer.JavaSlicer
import team57.debuggerpp.slicer.ProgramSlice
import team57.debuggerpp.ui.SelectSlicingCriterionAction
import team57.debuggerpp.util.Patch
import team57.debuggerpp.util.SourceLocation
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

class DynamicSliceDebuggerRunner : GenericDebuggerRunner() {
    companion object {
        const val ID = "DynamicSliceDebuggerRunner"
        private val SLICE_KEY = Key.create<ProgramSlice>("debuggerpp.programs-slice")
        private val LOG = Logger.getInstance(DynamicSliceDebuggerRunner::class.java)
    }

    private val slicer = JavaSlicer()
    private var lastSlicingCriteria: SourceLocation? = null

    override fun getRunnerId() = ID

    override fun canRun(executorId: String, profile: RunProfile) =
        executorId == DynamicSliceDebuggerExecutor.EXECUTOR_ID

    override fun execute(environment: ExecutionEnvironment) {
        LOG.info("Version: ${PluginManagerCore.getPlugin(PluginId.getId("team57.debuggerpp"))!!.version}")
        Patch.forceSetDelegatedRunProfile(environment.runProfile, environment.runProfile)
        super.execute(environment)
    }

    override fun createContentDescriptor(
        state: RunProfileState,
        env: ExecutionEnvironment
    ): RunContentDescriptor? {
        val programSlice = runDynamicSlicing(env)
            ?: return null
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
                val session =
                    XDebuggerManager.getInstance(env.project).startSession(env, object : XDebugProcessStarter() {
                        override fun start(session: XDebugSession): XDebugProcess {
                            val sessionImpl = session as XDebugSessionImpl
                            val executionResult = debuggerSession.process.executionResult
                            sessionImpl.addExtraActions(*executionResult.actions)
                            if (executionResult is DefaultExecutionResult) {
                                sessionImpl.addRestartActions(*executionResult.restartActions)
                            }
                            val slice = env.getUserData(SLICE_KEY)
                            return DppJavaDebugProcess.create(session, debuggerSession, slice)
                        }
                    })
                result.set(session.runContentDescriptor)
            } catch (e: ExecutionException) {
                ex.set(e)
            }
        }
        if (ex.get() != null)
            throw ex.get()!!
        return result.get()
    }

    private fun runDynamicSlicing(env: ExecutionEnvironment): ProgramSlice? {
        val task =
            object : Task.WithResult<ProgramSlice, Exception>(env.project, "Executing Dynamic Slicing", true) {
                override fun compute(indicator: ProgressIndicator): ProgramSlice? {
                    val outputDirectory = kotlin.io.path.createTempDirectory("slicer4j-outputs-")
//                    Desktop.getDesktop().open(outputDirectory.toFile())

                    // *** for temp test used only ***
//                    val testOutputDirectory = Files.createDirectories(Paths.get("src\\test\\kotlin\\team57\\debuggerpp\\execute\\generatedFile"));
//                    getProgramSlice(indicator, testOutputDirectory);

                    return getProgramSlice(indicator, outputDirectory)
                }

                private fun getProgramSlice(indicator: ProgressIndicator, outputDirectory: Path): ProgramSlice? {
                    val slicingCriteriaLocation = (env.dataContext as UserDataHolder)
                        .getUserData(SelectSlicingCriterionAction.SLICING_CRITERIA_KEY)
                        ?: lastSlicingCriteria
                        ?: run {
                            ApplicationManager.getApplication().invokeLater {
                                Messages.showErrorDialog(
                                    project,
                                    "Please select a slicing criteria by right-clicking on the line",
                                    "No Slicing Criteria"
                                )
                            }
                            return null
                        }

                    lastSlicingCriteria = slicingCriteriaLocation

                    val staticLog = outputDirectory.resolve("slicer4j-static.log")
                    val icdgLog = outputDirectory.resolve("icdg.log")

                    indicator.text = "Instrumenting"
                    val (instrumentedState, processDirs) = slicer.instrument(env, outputDirectory, staticLog)

                    indicator.text = "Collecting trace"
                    val executionResult = instrumentedState.execute(env.executor, env.runner)!!
                    val trace = slicer.collectTrace(executionResult, outputDirectory, staticLog)

                    indicator.text = "Creating dynamic control flow graph"
                    val graph = slicer.createDynamicControlFlowGraph(icdgLog, trace, processDirs)

                    indicator.text = "Locating slicing criteria"
                    val slicingCriteria =
                        slicer.locateSlicingCriteria(graph, slicingCriteriaLocation)
                    if (slicingCriteria.isEmpty()) {
                        throw ExecutionException(
                            "Unable to locate $slicingCriteriaLocation in the dynamic control flow graph"
                        )
                    }

                    indicator.text = "Slicing"
                    return slicer.slice(project, graph, slicingCriteria, processDirs, outputDirectory)
                }
            }
        task.queue() // This runs synchronously for modal tasks
        return task.result
    }
}