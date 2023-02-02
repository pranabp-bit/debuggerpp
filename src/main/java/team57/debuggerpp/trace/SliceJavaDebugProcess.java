package team57.debuggerpp.trace;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.impl.actions.XDebuggerActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team57.debuggerpp.slicer.ProgramSlice;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

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

    private VirtualFile getVirtualFile(XSuspendContext context){
        VirtualFile virtualFile = context.getActiveExecutionStack().getTopFrame().getSourcePosition().getFile();
        return virtualFile;
    }
    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        this.slice.getSliceLinesUnordered().forEach((a, b) -> {
            b.forEach(lineNumber -> {
                XDebuggerUtil.getInstance().toggleLineBreakpoint(getSession().getProject(), getVirtualFile(context), lineNumber-1, true);
            });
        });
        getDebuggerSession().resume();
    }

    int currentPosition = 0; // TODO: update this to initial breakpoint position
    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        this.slice.getSliceLinesUnordered().forEach((a, b) -> {
            int realLinePosition = position.getLine() + 1;
            XSourcePosition myPosition = position;
            if(b.contains(realLinePosition)) {
                currentPosition = myPosition.getLine();
                myPosition = XDebuggerUtil.getInstance().createPosition(getVirtualFile(context), realLinePosition-1);
                super.runToPosition(myPosition, context);
            }
            else{
                Messages.showErrorDialog("The line you selected is out of the slice, please try again!", UIUtil.removeMnemonic(ActionsBundle.actionText(XDebuggerActions.RUN_TO_CURSOR)));
//                myPosition = XDebuggerUtil.getInstance().createPosition(getVirtualFile(context), currentPosition);
            }

        });
//
//        try {
//            DebugProcessImpl.ResumeCommand runToCursorCommand = getDebuggerSession().getProcess().createRunToCursorCommand(getDebuggerSession().getContextManager().getContext().getSuspendContext(), position, false);
//                    myDebugProcess.createRunToCursorCommand(getSuspendContext(), position, ignoreBreakpoints);
//            setSteppingThrough(runToCursorCommand.getContextThread());
//            resumeAction(runToCursorCommand, DebuggerSession.Event.STEP);
//        }
//        catch (EvaluateException e) {
//            Messages.showErrorDialog(e.getMessage(), UIUtil.removeMnemonic(ActionsBundle.actionText(XDebuggerActions.RUN_TO_CURSOR)));
//        }
    }
    private void addLineBreakpoint(final Project project, final String fileUrl, final int line) {
        class MyBreakpointProperties extends XBreakpointProperties<MyBreakpointProperties> {
            public String myOption;

            public MyBreakpointProperties() {}

            @Override
            public MyBreakpointProperties getState() {
                return this;
            }

            @Override
            public void loadState(final MyBreakpointProperties state) {
                myOption = state.myOption;
            }
        }

        class MyLineBreakpointType extends XLineBreakpointType<MyBreakpointProperties> {
            public MyLineBreakpointType() {
                super("testId", "testTitle");
            }

            @Override
            public MyBreakpointProperties createProperties() {
                return new MyBreakpointProperties();
            }

            @Override
            public @Nullable MyBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
                return null;
            }
        }

        final XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
        final MyLineBreakpointType MY_LINE_BREAKPOINT_TYPE = new MyLineBreakpointType();
        final MyBreakpointProperties MY_LINE_BREAKPOINT_PROPERTIES = new MyBreakpointProperties();

        // add new line break point
        Runnable runnable = () -> breakpointManager.addLineBreakpoint(
                MY_LINE_BREAKPOINT_TYPE,
                fileUrl,
                line,
                MY_LINE_BREAKPOINT_PROPERTIES
        );
        WriteCommandAction.runWriteCommandAction(project, runnable);

        // toggle breakpoint to activate
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(fileUrl));
        XDebuggerUtil.getInstance().toggleLineBreakpoint(project, virtualFile, line);
    }
}

