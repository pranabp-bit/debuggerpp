package team57.debuggerpp.trace

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionManager
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.jar.JarApplicationCommandLineState
import com.intellij.execution.jar.JarApplicationConfiguration
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProcessProxyFactory
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import java.util.concurrent.atomic.AtomicReference

@Suppress("UnstableApiUsage")
class TraceRunner : DefaultJavaProgramRunner() {
    companion object {
        const val ID = "Trace"
        private val LOG = Logger.getInstance(TraceRunner::class.java)
    }

    override fun getRunnerId(): String = ID

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        executorId == "Trace" && profile is JarApplicationConfiguration

    override fun execute(environment: ExecutionEnvironment) {

        val profile = environment.runProfile
        if (profile !is JarApplicationConfiguration) return
        val state = environment.state
        if (state !is JarApplicationCommandLineState) throw ExecutionException("Run with Tracing is supported on the local machine only")

        val executionManager = ExecutionManager.getInstance(environment.project)
        executionManager.startRunProfileWithPromise(environment, state) {
            FileDocumentManager.getInstance().saveAllDocuments()
            state.prepareTargetToCommandExecution(
                environment, LOG, "Failed to execute tracing run configuration async"
            ) {
                val proxy = ProcessProxyFactory.getInstance().createCommandLineProxy(state)
                val executionResult = state.execute(environment.executor, this)
                val handler = executionResult.processHandler
                if (proxy != null) {
                    if (handler != null) {
                        proxy.attach(handler)
                        handler.addProcessListener(object : ProcessAdapter() {
                            override fun processTerminated(event: ProcessEvent) {
                                proxy.destroy()
                                handler.removeProcessListener(this)
                            }
                        })
                    }
                }
                val result = AtomicReference<RunContentDescriptor>()
                ApplicationManager.getApplication().invokeAndWait {
                    val contentBuilder = RunContentBuilder(executionResult, environment)
                    result.set(contentBuilder.showRunContent(environment.contentToReuse))
                }
                result.get()
            }
        }
    }
}