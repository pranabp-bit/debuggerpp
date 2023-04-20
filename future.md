# Future Development

## Features
- Reduce the time it takes to start a debugging session (e.g. skip slicing and reuse slices from previous debugging 
  sessions if the program/inputs have not been changed).
- Do instrumentation at the start of the IDE; do slicing on starting the debugging session.
- Change the dependencies graph to be clickable to allow a user to jump back to the debugging session state at which the
  graph node was added.
- Change the dependencies graph to have toggle-able dropdowns for each level, and optionally remove the control and data
  dependencies tabs which only showcase direct and not transitive dependencies (1st level of the graph).
- Map the Jimple statements to the source code and show nodes at the source code level in the graph.
- Get dependencies (control, data, graph, etc.) and skip lines from the *statement instance* level, using the current 
  state of execution.
  - One possible way to achieve this is to let the program produce the trace *incrementally* as the user steps over it 
    (online trace collection), and use the incrementally produced trace to obtain the current statement instance.
    If future developers would like to implement this, the most significant challenge they will face is to let the 
    debugger work correctly on instrumented programs. Commit 0705a48, which lets soot retain debugging symbols during 
    instrumentation, might be part of the solution to this.  
  - Another way to implement this is by having a variable that points to the current statement instance in the complete 
    trace, and update the variable as the user use the stepping/continue button. However, there are some corner cases 
    that are difficult to handle. For example, consider the following senario: in code snippet below, while the program 
    is currently running the loop, and the user put a breakpoint on the if statement. Then the breakpoint is triggered
    and the program pauses on the if statement. At this point, because we do not know how many times the loops have been
    run, we do not know which statement instance we are currently on, andd we do not know if the body of the if
    branch is the next statement.
    ```java
    for (int i = 0; i < 4230553203; i++) {
        if (i > 523234923) // The user adds a breakpoint to this line while the loop is running
            System.out.println("ERROR!");
    }
    ```
  - This is also related to Issue #44.
- Hide variable not in the slice in the variable window.
  - They key challenge of this is to avoid confusing multiple variables with the same name.
- Add visuals to indicate breakpoints on non-slice lines have been muted.
- Disable any "live" actions that can affect the slice.
- Rename the option to select slicing criteria to "change slicing criterion" if a Debugger++ debugging session is 
  actively running.
  - Issue #21.

## Limitations
- The plugin only works on deterministic programs.

## Bugs
- [SubGraphBuilder](src/main/java/team57/debuggerpp/trace/SubGraphBuilder.java) might produce incorrect outputs for 
  projects with multiple files.
