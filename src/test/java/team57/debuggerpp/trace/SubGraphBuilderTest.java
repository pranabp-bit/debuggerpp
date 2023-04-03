package team57.debuggerpp.trace;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Rasterizer;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static org.junit.Assert.*;

/* Important: each test has to be run separately, otherwise some tests will fail, because they cannot run in parallel */
public class SubGraphBuilderTest {
    public static final String subGraphFilePath = System.getProperty("java.io.tmpdir") + "\\slice-subgraph.png";
    public static final String subGraphDotFilePath = System.getProperty("java.io.tmpdir") + "\\slice-subgraph.dot";
    public static final String graphDotFilePath = System.getProperty("java.io.tmpdir") + "\\slice-graph.dot";
    public static final String sliceLogFilePath = System.getProperty("java.io.tmpdir") + "\\slice.log";

    public void setUp() throws IOException {
        File subGraphFile = new File(subGraphFilePath);
        // Delete the subgraph file before each test
        if (subGraphFile.exists()) {
            subGraphFile.delete();
        }
        // Write sample slice log data to sliceLogFilePath
        List<String> lines = List.of(
                "Main:5",
                "Main:6",
                "Main:9",
                "Main:14"
        );
        try {
            Files.write(Path.of(sliceLogFilePath), lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Build a sample graph and write it to subGraphDotFilePath
        MutableGraph g = mutGraph("example").setDirected(true);
        MutableNode node1 = mutNode("Main:5:").add(Label.of("Main:5:"));
        MutableNode node2 = mutNode("Main:6:").add(Label.of("Main:6:"));
        MutableNode node3 = mutNode("Main:9:").add(Label.of("Main:9:"));
        MutableNode node4 = mutNode("Main:14:").add(Label.of("Main:14:"));

        g.add(node1);
        g.add(node2);
        g.add(node3);
        g.add(node4);
        g.addLink(mutNode("Main:5:").addLink(mutNode("Main:6:")).addLink(mutNode("Main:9:")).addLink(mutNode("Main:14:")));
        Graphviz.fromGraph(g).rasterize(Rasterizer.builtIn("dot")).toFile(new File(graphDotFilePath));
    }

    @Test
    // Testing that the subgraph file is generated successfully for a valid line number.
    public void testGenerateSubGraph() throws IOException {
        setUp();
        SubGraphBuilder subGraphBuilder = new SubGraphBuilder();
        subGraphBuilder.generateSubGraph(14);
        File subGraphFile = new File(subGraphFilePath);
        assertTrue(subGraphFile.exists());
        assertTrue(subGraphFile.length() > 0);
    }

    @Test
    // Testing that the subgraph file is not generated for an invalid line number (negative value).
    public void testGenerateSubGraphWithInvalidLine() throws IOException {
        setUp();
        SubGraphBuilder subGraphBuilder = new SubGraphBuilder();
        subGraphBuilder.generateSubGraph(-1);
        File subGraphFile = new File(subGraphFilePath);
        assertFalse(subGraphFile.exists());
    }

    @Test
    // Testing that the subgraph file is not generated for an invalid line number (not in slice).
    public void testGenerateSubGraphWithInvalidLine2() throws IOException {
        setUp();
        SubGraphBuilder subGraphBuilder = new SubGraphBuilder();
        subGraphBuilder.generateSubGraph(3);
        File subGraphFile = new File(subGraphFilePath);
        assertFalse(subGraphFile.exists());
    }

    @Test
    // Check if the number of nodes in the subgraph is correct. Current lineNum is 5.
    public void testGenerateSubGraphNodeNum1() throws IOException {
        setUp();
        SubGraphBuilder subGraphBuilder = new SubGraphBuilder();
        subGraphBuilder.generateSubGraph(5);
        Parser parser = new Parser();
        MutableGraph g = parser.read(new File(subGraphDotFilePath));
        assertEquals(1, g.nodes().size());
    }

    @Test
    // Check if the number of nodes in the subgraph is correct. Current lineNum is 6.
    public void testGenerateSubGraphNodeNum2() throws IOException {
        setUp();
        SubGraphBuilder subGraphBuilder = new SubGraphBuilder();
        subGraphBuilder.generateSubGraph(6);
        Parser parser = new Parser();
        MutableGraph g = parser.read(new File(subGraphDotFilePath));
        assertEquals(2, g.nodes().size());
    }

    @Test
    // Check if the number of nodes in the subgraph is correct. Current lineNum is 9.
    public void testGenerateSubGraphNodeNum3() throws IOException {
        setUp();
        SubGraphBuilder subGraphBuilder = new SubGraphBuilder();
        subGraphBuilder.generateSubGraph(9);
        Parser parser = new Parser();
        MutableGraph g = parser.read(new File(subGraphDotFilePath));
        assertEquals(3, g.nodes().size());
    }

    @Test
    // Check if the number of nodes in the subgraph is correct. Current lineNum is 14.
    public void testGenerateSubGraphNodeNum4() throws IOException {
        setUp();
        SubGraphBuilder subGraphBuilder = new SubGraphBuilder();
        subGraphBuilder.generateSubGraph(14);
        Parser parser = new Parser();
        MutableGraph g = parser.read(new File(subGraphDotFilePath));
        assertEquals(4, g.nodes().size());
    }
}