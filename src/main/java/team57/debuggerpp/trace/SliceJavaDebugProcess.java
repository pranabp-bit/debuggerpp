package team57.debuggerpp.trace;

import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.impl.actions.XDebuggerActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team57.debuggerpp.slicer.ProgramSlice;

public class SliceJavaDebugProcess extends JavaDebugProcess {
    public final ProgramSlice slice;
    boolean slicing;
    int sliceLineIndex = 0;

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
    public void startStepInto(@Nullable XSuspendContext context) {
        XSourcePosition sourcePosition = context.getActiveExecutionStack().getTopFrame().getSourcePosition();
        VirtualFile virtualFile = sourcePosition.getFile();
        int line = sourcePosition.getLine();
        var rawSlice = this.slice.getSliceLinesOrdered();

        for (var i = 0; i < rawSlice.size(); i++) {
            if (i < sliceLineIndex) {
                continue;
            }
            if (rawSlice.get(i) -1 == line ) {
                sliceLineIndex++;
            } else if (sliceLineIndex == i) {
                var newSourcePosition = XDebuggerUtil.getInstance().createPosition(virtualFile, rawSlice.get(i) - 1);
                getSession().runToPosition(newSourcePosition, false);
                sliceLineIndex++;
                break;
            }
        }

    }

    // TODO: FIX RUNTOPOSITION HANDLER USING ORDERED SLICE LINES
//    int currentPosition = 0; // TODO: update this to initial breakpoint position
//    @Override
//    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
//        this.slice.getSliceLinesUnordered().forEach((a, b) -> {
//            int realLinePosition = position.getLine() + 1;
//            XSourcePosition myPosition = position;
//            if(b.contains(realLinePosition)) {
//                currentPosition = myPosition.getLine();
//                myPosition = XDebuggerUtil.getInstance().createPosition(getVirtualFile(context), realLinePosition-1);
//                super.runToPosition(myPosition, context);
//            }
//            else{
//                Messages.showErrorDialog("The line you selected is out of the slice, please try again!", UIUtil.removeMnemonic(ActionsBundle.actionText(XDebuggerActions.RUN_TO_CURSOR)));
//            }
//        });
//    }
}

