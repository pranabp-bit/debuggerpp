package team57.debuggerpp.trace;

import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team57.debuggerpp.slicer.ProgramSlice;

public class SliceJavaDebugProcess extends JavaDebugProcess {
    public final ProgramSlice slice;
    boolean slicing;

    protected SliceJavaDebugProcess(@NotNull XDebugSession session, @NotNull DebuggerSession javaSession, ProgramSlice slice) {
        super(session, javaSession);
        this.slice = slice;
        this.slicing = true;
    }

    public static SliceJavaDebugProcess create(@NotNull final XDebugSession session, @NotNull final DebuggerSession javaSession, ProgramSlice slice) {
        SliceJavaDebugProcess res = new SliceJavaDebugProcess(session, javaSession, slice);
        javaSession.getProcess().setXDebugProcess(res);
        return res;
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
//        getDebuggerSession().runToCursor(position, false);
        super.startStepOver(context);
    }
}
