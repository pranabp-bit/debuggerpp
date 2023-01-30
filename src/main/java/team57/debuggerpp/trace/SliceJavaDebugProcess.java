package team57.debuggerpp.trace;

import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team57.debuggerpp.slicer.ProgramSlice;

import java.io.File;

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

        final String fileUrl = "C:\\Users\\robin\\Documents\\CPEN 491\\test\\src\\Main.java"; //TODO: Change this dir

//        addLineBreakpoint(getSession().getProject(), fileUrl, 21);

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(fileUrl));

        this.slice.getSliceLinesUnordered().forEach((a, b) -> {
            b.forEach(lineNumber -> {
                XSourcePositionImpl position = XSourcePositionImpl.create(virtualFile, lineNumber);
                XDebuggerUtil.getInstance().toggleLineBreakpoint(getSession().getProject(), virtualFile, lineNumber);
                assert position != null;
                getDebuggerSession().runToCursor(position, false);
            });
//
        });
        super.startStepOver(context);
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

