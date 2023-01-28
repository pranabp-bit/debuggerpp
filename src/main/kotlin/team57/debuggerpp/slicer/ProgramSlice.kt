package team57.debuggerpp.slicer

import ca.ubc.ece.resess.slicer.dynamic.core.slicer.DynamicSlice
import com.intellij.openapi.diagnostic.Logger
import team57.debuggerpp.util.SourceLocation
import java.util.*

class ProgramSlice(private val dynamicSlice: DynamicSlice) {
    companion object {
        private val LOG = Logger.getInstance(ProgramSlice::class.java)
    }

    val sliceLinesUnordered: Map<String, Set<Int>> = run {
        val map = HashMap<String, MutableSet<Int>>()
        for (sliceNode in dynamicSlice.map { x -> x.o1.o1 }) {
            val set = map.getOrPut(sliceNode.javaSourceFile) { HashSet() }
            set.add(sliceNode.javaSourceLineNo)
            set.add(sliceNode.method.javaSourceStartLineNumber)
//            set.add(sliceNode.method.declaringClass.javaSourceStartLineNumber)
        }
        return@run map
    }

    val dependencies: Map<SourceLocation, Dependencies> = run {
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
        return@run map
    }

    class Dependencies(
        val data: DataDependencies = DataDependencies(),
        val control: ControlDependencies = ControlDependencies()
    )

    class DataDependencies(val from: List<DataDependency> = ArrayList(), val to: List<DataDependency> = ArrayList())

    class ControlDependencies(
        val from: List<ControlDependency> = ArrayList(),
        val to: List<ControlDependency> = ArrayList()
    )

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
