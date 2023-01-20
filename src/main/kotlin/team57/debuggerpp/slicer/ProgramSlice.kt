package team57.debuggerpp.slicer

import ca.ubc.ece.resess.slicer.dynamic.core.slicer.DynamicSlice
import com.intellij.openapi.diagnostic.Logger

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
}
