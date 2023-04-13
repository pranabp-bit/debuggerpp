package team57.debuggerpp.slicer

import ca.ubc.ece.resess.slicer.dynamic.core.slicer.DynamicSlice
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parents
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import team57.debuggerpp.util.SourceLocation
import java.util.*


class ProgramSlice(private val project: Project?, private val dynamicSlice: DynamicSlice) {
    companion object {
        private val LOG = Logger.getInstance(ProgramSlice::class.java)
    }

    val sliceLinesUnordered: Map<String, Set<Int>> by lazy {
        if (project == null) {
            throw IllegalStateException()
        }
        ReadAction.compute<Map<String, Set<Int>>, Throwable> {
            val map = HashMap<String, MutableSet<Int>>()
            val documentManager = PsiDocumentManager.getInstance(project)
            val searchScope = GlobalSearchScope.allScope(project)
            val psiFacade = JavaPsiFacade.getInstance(project)
            for (sliceNode in dynamicSlice.map { x -> x.o1.o1 }) {
                if (sliceNode.javaSourceLineNo < 0)
                    continue
                val set = map.getOrPut(sliceNode.javaSourceFile) { HashSet() }
                val line = sliceNode.javaSourceLineNo - 1
                val clazz = psiFacade.findClass(sliceNode.javaSourceFile, searchScope)
                val file = clazz?.containingFile
                val document = file?.let { documentManager.getDocument(file) }
                if (document != null) {
                    val lineOffset = (document.getLineStartOffset(line) + document.getLineEndOffset(line)) / 2
                    val element = file.findElementAt(lineOffset)
                    element?.parents(false)
                        ?.forEach {
                            if (it !is PsiIfStatement) {
                                set.add(document.getLineNumber(it.startOffset))
                                set.add(document.getLineNumber(it.endOffset))
                            }
                        }
                }
                set.add(line)
            }
            return@compute map
        }
    }

    val dependencies: Map<SourceLocation, Dependencies> by lazy {
        val map = HashMap<SourceLocation, Dependencies>()
        val entriesSeen = HashSet<Triple<String, SourceLocation, SourceLocation>>()
        for (entry in dynamicSlice) {
            val fromNode = entry.o1.o1
            val toNode = entry.o2.o1
            val fromLocation = SourceLocation(fromNode.javaSourceFile, fromNode.javaSourceLineNo)
            val toLocation = SourceLocation(toNode.javaSourceFile, toNode.javaSourceLineNo)
            val type = dynamicSlice.getEdges(entry.o1.o1.lineNo, entry.o2.o1.lineNo)
            if (!entriesSeen.add(Triple(type, fromLocation, toLocation)))
                continue

            val dependenciesFrom = map.getOrPut(fromLocation) { Dependencies() }
            val dependenciesTo = map.getOrPut(toLocation) { Dependencies() }
            when (type) {
                "data" -> run {
                    val variableName = entry.o2.o2.pathString
                    if (variableName.isNotBlank()) {
                        (dependenciesFrom.data.to as ArrayList).add(DataDependency(toLocation, variableName))
                        (dependenciesTo.data.from as ArrayList).add(DataDependency(fromLocation, variableName))
                    }
                }

                "control" -> {
                    (dependenciesFrom.control.to as ArrayList).add(ControlDependency(toLocation))
                    (dependenciesTo.control.from as ArrayList).add(ControlDependency(fromLocation))
                }

                else -> throw IllegalStateException("Unknown dependency type $type")
            }
        }
        return@lazy map
    }

    val firstLine: SourceLocation? by lazy {
        dynamicSlice.order.getOrNull(0)?.o1?.let {
            SourceLocation(it.javaSourceFile, it.javaSourceLineNo - 1)
        }
    }

    class Dependencies(
        val data: DataDependencies = DataDependencies(),
        val control: ControlDependencies = ControlDependencies()
    ) {
        override fun equals(other: Any?) = (other is Dependencies) && data == other.data && control == other.control
        override fun hashCode() = Objects.hash(data, control)
    }

    class DataDependencies(val from: List<DataDependency> = ArrayList(), val to: List<DataDependency> = ArrayList()) {
        override fun equals(other: Any?) = (other is DataDependencies) && from == other.from && to == other.to
        override fun hashCode() = Objects.hash(from, to)
    }

    class ControlDependencies(
        val from: List<ControlDependency> = ArrayList(),
        val to: List<ControlDependency> = ArrayList()
    ) {
        override fun equals(other: Any?) = (other is ControlDependencies) && from == other.from && to == other.to
        override fun hashCode() = Objects.hash(from, to)
    }

    abstract class Dependency(val location: SourceLocation) {
        override fun equals(other: Any?) = (other is Dependency) && location == other.location
        override fun hashCode() = location.hashCode()
    }

    class ControlDependency(location: SourceLocation) : Dependency(location)

    class DataDependency(location: SourceLocation, val variableName: String) : Dependency(location) {
        override fun equals(other: Any?) = (other is DataDependency)
                && location == other.location && variableName == other.variableName

        override fun hashCode() = Objects.hash(location, variableName)
    }
}
