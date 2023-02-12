package team57.debuggerpp.dbgcontroller;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.MethodFilter;
import com.intellij.debugger.engine.RequestHint;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.impl.JvmSteppingCommandProvider;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.sun.jdi.request.StepRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DppJvmSteppingCommandProvider extends JvmSteppingCommandProvider {
    @Override
    public DebugProcessImpl.ResumeCommand getStepIntoCommand(SuspendContextImpl suspendContext, boolean ignoreFilters, MethodFilter smartStepFilter, int stepSize) {
        return new StepIntoCommand(suspendContext, ignoreFilters, smartStepFilter, stepSize);
    }

    @Override
    public DebugProcessImpl.ResumeCommand getStepOverCommand(SuspendContextImpl suspendContext, boolean ignoreBreakpoints, int stepSize) {
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
}
