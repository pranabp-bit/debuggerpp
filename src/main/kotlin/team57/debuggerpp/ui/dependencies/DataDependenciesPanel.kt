package team57.debuggerpp.ui.dependencies

import com.intellij.openapi.project.Project
import team57.debuggerpp.slicer.ProgramSlice
import team57.debuggerpp.util.SourceLocation

class DataDependenciesPanel(project: Project) : DependenciesPanel(project) {
    fun updateDependencies(dependencies: ProgramSlice.DataDependencies?, location: SourceLocation?) {
        removeAll()
        if (dependencies == null) {
            addNoDependenciesMessage("Data")
        } else {
            if (location == null) return
            addTitleLabel(location)
            updateDependencies(dependencies.from)
        }
        updateUI()
    }

    private fun updateDependencies(dependencies: Collection<ProgramSlice.DataDependency>) {
        for (dependency in dependencies) {
            if (dependency.variableName.isEmpty())
                continue
            addDependencyLine("${dependency.variableName}: ", dependency)
        }
        if (dependencies.isEmpty())
            addEmptyLabel()
    }

    fun emptyPanel() {
        emptyPanel("Data Dependencies are not available")
    }
}