package team57.debuggerpp.dbgcontroller;

import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.*;
import com.intellij.debugger.impl.JvmSteppingCommandProvider;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiFile;
import com.sun.jdi.Location;
import com.sun.jdi.request.StepRequest;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team57.debuggerpp.slicer.ProgramSlice;
import team57.debuggerpp.util.Utils;

import java.util.Set;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
public class DppJvmSteppingCommandProvider extends JvmSteppingCommandProvider {
    public static HashMap<String, Integer> actionCounts = new HashMap<>();
    @Override
    public DebugProcessImpl.ResumeCommand getStepIntoCommand(SuspendContextImpl suspendContext, boolean ignoreFilters, MethodFilter smartStepFilter, int stepSize) {
        incrementActionCount("step_into");
        return new StepIntoCommand(suspendContext, ignoreFilters, smartStepFilter, stepSize);
    }

    @Override
    public DebugProcessImpl.ResumeCommand getStepOverCommand(SuspendContextImpl suspendContext, boolean ignoreBreakpoints, int stepSize) {
        incrementActionCount("step_over");
        return new StepOverCommand(suspendContext, ignoreBreakpoints, stepSize);
    }

    class StepIntoCommand extends DebugProcessImpl.StepIntoCommand {
        public StepIntoCommand(SuspendContextImpl suspendContext, boolean ignoreFilters, @Nullable MethodFilter methodFilter, int stepSize) {
            suspendContext.getDebugProcess().super(suspendContext, ignoreFilters, methodFilter, stepSize);
        }

        @Override
        public @NotNull RequestHint getHint(SuspendContextImpl suspendContext, ThreadReferenceProxyImpl stepThread, @Nullable RequestHint parentHint) {
            final RequestHint hint = new DppRequestHint(stepThread, suspendContext, StepRequest.STEP_LINE, StepRequest.STEP_INTO, myMethodFilter, parentHint);
            hint.setResetIgnoreFilters(myMethodFilter != null && !suspendContext.getDebugProcess().getSession().shouldIgnoreSteppingFilters());
            return hint;
        }
    }

    class StepOverCommand extends DebugProcessImpl.StepOverCommand {
        private final boolean ignoreBreakpoints;

        public StepOverCommand(SuspendContextImpl suspendContext, boolean ignoreBreakpoints, int stepSize) {
            suspendContext.getDebugProcess().super(suspendContext, ignoreBreakpoints, stepSize);
            this.ignoreBreakpoints = ignoreBreakpoints;
        }

        @Override
        public @NotNull RequestHint getHint(SuspendContextImpl suspendContext, ThreadReferenceProxyImpl stepThread, @Nullable RequestHint parentHint) {
            RequestHint hint = new DppRequestHint(stepThread, suspendContext, StepRequest.STEP_LINE, StepRequest.STEP_OVER, myMethodFilter, parentHint);
            hint.setRestoreBreakpoints(ignoreBreakpoints);
            hint.setIgnoreFilters(ignoreBreakpoints || suspendContext.getDebugProcess().getSession().shouldIgnoreSteppingFilters());
            return hint;
        }
    }

    private void incrementActionCount(String action) {
        int count = actionCounts.getOrDefault(action, 0);
        actionCounts.put(action, count + 1);
        System.out.println(action+": "+actionCounts.get(action));
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        // TODO: update the counter in a remote database or file
        try {
            // create a FileWriter object to write to the file
            FileWriter writer = new FileWriter("output.txt");
            // create a BufferedWriter object to improve performance
            BufferedWriter buffer = new BufferedWriter(writer);

            // write to the file
            buffer.write("Hello World!");
            buffer.newLine(); // add a new line
            buffer.write("This is a test file.");

            // close the buffer and writer
            buffer.close();
            writer.close();

            System.out.println("Data written to file with kotlin changes successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class DppRequestHint extends RequestHint {
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
}