package team57.debuggerpp.ui.dependencies

import com.intellij.openapi.project.Project
import team57.debuggerpp.slicer.ProgramSlice

class DataDependenciesPanel(project: Project) : DependenciesPanel(project) {
    fun updateDependencies(dependencies: ProgramSlice.DataDependencies?) {
        removeAll()
        if (dependencies == null) {
            addNoDependenciesMessage("Data")
        } else {
            addTitleLabel("From", YELLOW)
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