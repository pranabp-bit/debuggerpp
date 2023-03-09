package team57.debuggerpp.ui.dependencies

import com.intellij.openapi.project.Project
import team57.debuggerpp.slicer.ProgramSlice
import team57.debuggerpp.util.SourceLocation


class ControlDependenciesPanel(project: Project) : DependenciesPanel(project) {
    fun updateDependencies(dependencies: ProgramSlice.ControlDependencies?, location: SourceLocation?) {
        removeAll()
        if (dependencies == null) {
            addNoDependenciesMessage("Control")
        } else {
            if (location == null) return
            addTitleLabel(location)
            updateDependencies(dependencies.from)
        }
        updateUI()
    }

    private fun updateDependencies(dependencies: Collection<ProgramSlice.ControlDependency>) {
        for (dependency in dependencies) {
            addDependencyLine("", dependency)
        }
        if (dependencies.isEmpty())
            addEmptyLabel()
    }

    fun emptyPanel() {
        emptyPanel("Control Dependencies are not available")
    }
}