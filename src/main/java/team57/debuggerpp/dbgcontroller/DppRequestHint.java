package team57.debuggerpp.dbgcontroller;

import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.engine.MethodFilter;
import com.intellij.debugger.engine.RequestHint;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiFile;
import com.sun.jdi.Location;
import com.sun.jdi.request.StepRequest;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import team57.debuggerpp.slicer.ProgramSlice;
import team57.debuggerpp.util.Utils;

import java.util.Set;

public class DppRequestHint extends RequestHint {
    public DppRequestHint(ThreadReferenceProxyImpl stepThread,
                          SuspendContextImpl suspendContext,
                          @MagicConstant(intValues = {StepRequest.STEP_MIN, StepRequest.STEP_LINE}) int stepSize,
                          @MagicConstant(intValues = {StepRequest.STEP_INTO, StepRequest.STEP_OVER, StepRequest.STEP_OUT}) int depth,
                          @Nullable MethodFilter methodFilter,
                          @Nullable RequestHint parentHint) {
        super(stepThread, suspendContext, stepSize, depth, methodFilter, parentHint);
    }

    @Override
    public Integer checkCurrentPosition(SuspendContextImpl context, Location location) {
        JavaDebugProcess debugProcess = context.getDebugProcess().getXdebugProcess();
        if (debugProcess instanceof DppJavaDebugProcess) {
            ProgramSlice slice = ((DppJavaDebugProcess) debugProcess).slice;
            if (slice != null && (getDepth() == StepRequest.STEP_OVER || getDepth() == StepRequest.STEP_INTO)) {
                SourcePosition position = context.getDebugProcess().getPositionManager().getSourcePosition(location);
                if (position != null) {
                    PsiFile file = position.getFile();
                    FileIndex fileIndex = ProjectRootManager.getInstance(file.getProject()).getFileIndex();
                    if (fileIndex.isInContent(file.getVirtualFile())) {
                        String clazz = Utils.findClassName(file, position.getOffset());
                        Set<Integer> lines = slice.getSliceLinesUnordered().get(clazz);
                        if (lines == null || !lines.contains(position.getLine())) {
                            return StepRequest.STEP_OVER; // Step until a slice line is reached
                        }
                    }
                }
            }
        }
        return super.checkCurrentPosition(context, location);
    }
}
