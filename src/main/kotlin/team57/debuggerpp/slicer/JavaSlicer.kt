package team57.debuggerpp.slicer

import ca.ubc.ece.resess.slicer.dynamic.core.graph.Parser
import ca.ubc.ece.resess.slicer.dynamic.core.graph.Trace
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
import team57.debuggerpp.util.Utils
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.jar.JarInputStream
import java.util.zip.InflaterOutputStream
import java.util.zip.ZipInputStream
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

class JavaSlicer {
    companion object {
        private val LOG = Logger.getInstance(JavaSlicer::class.java)
    }

    private val loggerPath: String
    private val modelsPath: String
    private val stubDroidPath: String
    private val taintWrapperPath: String

    init {
        val loggerFile = kotlin.io.path.createTempFile("slicer4-logger-", ".jar")
        val loggerJar = Slicer::class.java.getResourceAsStream("/DynamicSlicingLogger.jar")!!
        Files.copy(loggerJar, loggerFile, StandardCopyOption.REPLACE_EXISTING)
        loggerPath = loggerFile.toString()

        val modelsDirectory = kotlin.io.path.createTempDirectory("slicer4-models-")
        val modelsZip = Slicer::class.java.getResourceAsStream("/models.zip")!!
        Utils.unzipAll(ZipInputStream(modelsZip), modelsDirectory)
        modelsPath = modelsDirectory.toString()
        stubDroidPath = modelsDirectory.resolve("summariesManual").toString()
        taintWrapperPath = modelsDirectory.resolve("EasyTaintWrapperSource.txt").toString()
    }

    /*
     * TODO: Find a way for IntelliJ to consider this a build task that does not need to be repeated if we've already
     * instrumented this JAR before.
     */
    fun instrument(
        env: ExecutionEnvironment,
        outputDirectory: Path,
        staticLog: Path
    ): Pair<RunProfileState, List<String>> {
        val state = env.state!!
        val processingDirectory: List<String>
        val sootOutputDirectory = outputDirectory.resolve("instrumented")
        val instrumentationOptions = ""
        when (state) {
            is JarApplicationCommandLineState -> {
                val params = state.javaParameters
                val outJarPath = outputDirectory.resolve("instrumented.jar").pathString
                params.mainClass = JarInputStream(BufferedInputStream(FileInputStream(params.jarPath)))
                    .manifest.mainAttributes.getValue("Main-Class")
                JavaInstrumenter(outJarPath)
                    .instrumentJar(
                        instrumentationOptions,
                        staticLog.pathString,
                        params.jarPath,
                        loggerPath,
                        sootOutputDirectory.pathString
                    )
                params.classPath.add(outJarPath)
                processingDirectory = Collections.singletonList(params.jarPath)
            }

            is JavaCommandLineState -> {
                val params = state.javaParameters
                val jdkPath = params.jdkPath
                val instrumentClassPaths = params.classPath.pathList.filterNot { it.startsWith(jdkPath) }
                val instrumentedClasPaths = JavaInstrumenter()
                    .instrumentClassPaths(
                        instrumentationOptions,
                        staticLog.pathString,
                        instrumentClassPaths,
                        loggerPath,
                        sootOutputDirectory.pathString
                    )
                instrumentClassPaths.forEach { params.classPath.remove(it) }
                params.classPath.addAll(instrumentedClasPaths)
                processingDirectory = instrumentClassPaths
            }

            else -> throw ExecutionException("Unable to instrument this type of RunProfileState")
        }
        return Pair(state, processingDirectory)
    }

    fun collectTrace(executionResult: ExecutionResult, outputDirectory: Path, staticLog: Path): Trace {
        val stdoutLog = outputDirectory.resolve("instrumented-stdout.log")

        stdoutLog.bufferedWriter().use { writer ->
            executionResult.processHandler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (outputType === ProcessOutputTypes.STDOUT) {
                        writer.write(event.text)
                    }
                }
            })
            executionResult.processHandler.startNotify()
            executionResult.processHandler.waitFor()
        }

        val trace = Parser.readFile(stdoutLog.pathString, staticLog.pathString)
        saveTrace(trace, outputDirectory)
        extractRawTrace(stdoutLog, outputDirectory)

        return trace
    }

    fun slice(trace: Trace, processingDirectory: List<String>, outputDirectory: Path): ProgramSlice {
        // TODO
//        val icdgPath = outputDirectory.resolve("icdg.log")
//        val backwardSlicePositions: List<Int> = listOf(3)
//
//        Slicer.slice(
//            outputDirectory.pathString, trace, icdgPath.pathString, processingDirectory,
//            backwardSlicePositions, stubDroidPath, taintWrapperPath,
//            null, null,
//            true, false, false, false
//        )
        return ProgramSlice()
    }

    private fun saveTrace(trace: Trace, outputDirectory: Path) {
        outputDirectory.resolve("trace.log")
            .bufferedWriter().use { writer ->
                for (statement in trace) {
                    writer.write(statement.toString())
                    writer.write("\n")
                }
            }
    }

    private fun extractRawTrace(stdoutLog: Path, outputDirectory: Path) {
        val rawTraceLog = outputDirectory.resolve("raw-trace.log")
        rawTraceLog.outputStream().use { os ->
            val inflaterOutputStream = InflaterOutputStream(os)
            stdoutLog.bufferedReader().useLines { lines ->
                for (line in lines) {
                    if (line.contains(" ZLIB: ")) {
                        val encoded = line.split(" ZLIB: ")[1]
                        val decoded = Base64.getDecoder().decode(encoded)
                        inflaterOutputStream.write(decoded)
                    }
                }
            }
        }
    }
}