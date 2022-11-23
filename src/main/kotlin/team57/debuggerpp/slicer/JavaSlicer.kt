package team57.debuggerpp.slicer

import ca.ubc.ece.resess.slicer.dynamic.slicer4j.Slicer
import ca.ubc.ece.resess.slicer.dynamic.slicer4j.instrumenter.JavaInstrumenter
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.JavaCommandLineState
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.jar.JarApplicationCommandLineState
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.util.io.readText
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.jar.JarInputStream
import kotlin.io.path.bufferedWriter
import kotlin.io.path.pathString

class JavaSlicer {
    companion object {
        private val LOG = Logger.getInstance(JavaSlicer::class.java)
    }

    private val loggerPath: String

    init {
        val loggerFile = kotlin.io.path.createTempFile("slicer4-logger-", ".jar")
        val loggerJar = Slicer::class.java.getResourceAsStream("/DynamicSlicingLogger.jar")!!
        Files.copy(loggerJar, loggerFile, StandardCopyOption.REPLACE_EXISTING)
        loggerPath = loggerFile.toString()
    }

    /*
     * TODO: Find a way for IntelliJ to consider this a build task that does not need to be repeated if we've already
     * instrumented this JAR before.
     */
    fun instrument(env: ExecutionEnvironment): RunProfileState {
        val state = env.state!!
        when (state) {
            is JarApplicationCommandLineState -> {
                val params = state.javaParameters
                val outPath = kotlin.io.path.createTempFile("slicer4j-instrumented-", ".jar")
                val staticLogFile = kotlin.io.path.createTempFile("slicer4j-static-", ".log")
                params.mainClass = JarInputStream(BufferedInputStream(FileInputStream(params.jarPath)))
                    .manifest.mainAttributes.getValue("Main-Class")
                JavaInstrumenter(outPath.pathString)
                    .start("", staticLogFile.pathString, params.jarPath, loggerPath)
                params.classPath.add(outPath.pathString)
            }

            is JavaCommandLineState -> {
                val params = state.javaParameters
                val jdkPath = params.jdkPath
                val instrumentClassPaths = params.classPath.pathList.filterNot { it.startsWith(jdkPath) }
                val staticLogFile = kotlin.io.path.createTempFile("slicer4j-static-", ".log")
                val instrumentedClasPaths = JavaInstrumenter()
                    .instrumentClassPaths("", staticLogFile.pathString, instrumentClassPaths, loggerPath)
                instrumentClassPaths.forEach { params.classPath.remove(it) }
                params.classPath.addAll(instrumentedClasPaths)
            }

            else -> throw ExecutionException("Unable to instrument this type of RunProfileState")
        }
        return state
    }

    fun collectTrace(executionResult: ExecutionResult): Path {
        val stdoutPath = kotlin.io.path.createTempFile("slicer4j-instrumented-stdout-", ".log")
        val stdoutWriter = stdoutPath.bufferedWriter()
        executionResult.processHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (outputType === ProcessOutputTypes.STDOUT) {
                    stdoutWriter.write(event.text)
                }
            }
        })
        executionResult.processHandler.startNotify()
        executionResult.processHandler.waitFor()
        stdoutWriter.close()
        return stdoutPath
    }

    fun slice(trace: Path): ProgramSlice {
        // TODO
        LOG.info(trace.readText())
        return ProgramSlice()
    }
}