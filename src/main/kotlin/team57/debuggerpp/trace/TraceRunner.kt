package team57.debuggerpp.trace

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.jar.JarApplicationConfiguration
import com.intellij.execution.runners.ExecutionEnvironment

class TraceRunner : DefaultJavaProgramRunner() {
    companion object {
        const val ID = "Trace"
    }

    override fun getRunnerId(): String = ID

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        executorId == "Trace" && profile is JarApplicationConfiguration

    override fun execute(environment: ExecutionEnvironment) {
        println("Executing profile with tracing: ${environment.runProfile}")
        super.execute(environment)
    }
}