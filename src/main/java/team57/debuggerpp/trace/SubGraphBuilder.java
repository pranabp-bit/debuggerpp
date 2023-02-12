package team57.debuggerpp.trace;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.graphviz.attribute.Color.TRANSPARENT;
import static guru.nidi.graphviz.model.Factory.mutGraph;

public class SubGraphBuilder {
    public void generateSubGraph(int currentLine) throws IOException {
        Parser parser = new Parser();
        // Read the full graph
        MutableGraph g = parser.read(new File(System.getProperty("java.io.tmpdir") + "\\slice-graph.dot"));
        MutableGraph subGraph = mutGraph("Subgraph").setDirected(true);
        System.out.println("Reading dot file success");

        // Get the list of lines in the slice
        String sliceFilePath = System.getProperty("java.io.tmpdir") + "slice.log";
        List<Integer> sliceLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(sliceFilePath))) {
            String line;
            Pattern pattern = Pattern.compile("Main:(\\d+)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    sliceLines.add(Integer.parseInt(matcher.group(1)));
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the log file: " + e.getMessage());
        }

        // Loop through the nodes in the graph. For each node, check if the line# appears before the current line in sliceLines
        // If it does, keep it; if not, remove the node and corresponding links
        for (MutableNode node : g.nodes()) {
            System.out.println("Node name :" + node.name());
            String nodeName = String.valueOf(node.name());
            Pattern pattern = Pattern.compile("Main:(\\d+):");
            Matcher matcher = pattern.matcher(nodeName);
            if (matcher.find()) {
                Integer NodeLineNum = Integer.parseInt(matcher.group(1));
                for (Integer sliceLine : sliceLines) {
                    if (Objects.equals(sliceLine, NodeLineNum)) {
                        subGraph.add(node);
                        break;
                    }
                    if (Objects.equals(sliceLine, currentLine)) {
                        break;
                    }
                }
            }
        }
        
        subGraph.nodeAttrs().add(Color.WHITE).linkAttrs().add(Color.WHITE).graphAttrs().add(Color.WHITE);
        subGraph.nodeAttrs().add(Color.WHITE.font()).linkAttrs().add(Color.WHITE.font());
        subGraph.graphAttrs().add(TRANSPARENT.background());
        Graphviz.fromGraph(subGraph).width(1200).render(Format.PNG).toFile(new File(System.getProperty("java.io.tmpdir") + "\\slice-subgraph.png"));
    }
}
