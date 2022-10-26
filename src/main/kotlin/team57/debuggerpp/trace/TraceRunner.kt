package team57.debuggerpp.trace

import ca.ubc.ece.resess.slicer.dynamic.slicer4j.Slicer
import ca.ubc.ece.resess.slicer.dynamic.slicer4j.instrumenter.JavaInstrumenter
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.jar.JarApplicationConfiguration
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.jar.JarInputStream
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString

@Suppress("UnstableApiUsage")
class TraceRunner : DefaultJavaProgramRunner() {
    companion object {
        const val ID = "Trace"
        private val LOG = Logger.getInstance(TraceRunner::class.java)
    }

    override fun getRunnerId(): String = ID

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        executorId == "Trace" && profile is JarApplicationConfiguration


    /*
     * TODO: Find a way for IntelliJ to consider this a build task that does not need to be repeated if we've already
     * instrumented this JAR before.
     */
    override fun patch(
        javaParameters: JavaParameters, settings: RunnerSettings?, runProfile: RunProfile, beforeExecution: Boolean
    ) {
        if (runProfile !is JarApplicationConfiguration) throw ExecutionException(
            "Run with Tracing is only supported on JAR applications"
        )
        super.patch(javaParameters, settings, runProfile, beforeExecution)
        val task = object : Task.Modal(runProfile.project, "Instrumenting JAR", false) {
            override fun run(indicator: ProgressIndicator) {
                // TODO: All of this is temporary.  We don't want to be leaving temporary files everywhere, but it
                //  will work for the demo.
                val inJar = JarInputStream(BufferedInputStream(FileInputStream(runProfile.jarPath)))
                val mainClass = inJar.manifest.mainAttributes.getValue("Main-Class")
                val outPath = createTempFile("slicer4j-instrumented", ".jar")
                val loggerPath = createTempFile("slicer4-logger", ".jar")
                val loggerJar = Slicer::class.java.getResourceAsStream("/DynamicSlicingLogger.jar")
                Files.copy(loggerJar, loggerPath, StandardCopyOption.REPLACE_EXISTING)
                val instrumenter = JavaInstrumenter(outPath.pathString)
                instrumenter.start("", "/tmp/slicer4j-static.log", javaParameters.jarPath, loggerPath.toString())
                javaParameters.classPath.add(outPath.pathString)
                javaParameters.mainClass = mainClass
                loggerPath.deleteIfExists()
            }
        }
        task.queue()
    }
}