package Holt.graph;



import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphParserCSV {

    private GraphParserCSV() {}

    /**
     * Reads the CSV file and generates a graph from it.
     * Returns the external entities as they can be used as entry points
     * for the program.
     * @param csvPath The path for the csv file that represents a PA-DFD.
     * @return The nodes that have the node type external entity
     */
    public static List<Node> readGraph(Path csvPath) {
        return generatePADFD(readCSV(csvPath));
    }

    private record CSV(List<Row> data) {
        private record Row(
                int id,
                String value,
                String style,
                String source,
                String target,
                NodeType type,
                Integer for_process,
                Integer for_DB
        ) { }
    }

    private static class NodeDraft {
        CSV.Row row;
        List<NodeDraft> outputs;

        public NodeDraft(CSV.Row row) {
            this.row = row;
            this.outputs = new ArrayList<>();
        }
    }

    private static List<Node> generatePADFD(CSV csv) {
        List<Node> externalEntityNodes = new ArrayList<>();
        Map<Integer, Node> nodes = new HashMap<>();
        Map<String, CSV.Row> nodeToRow = new HashMap<>();

        //First, all node rows
        for (CSV.Row row : csv.data()) {
            //If there's no source, then it's a node
            if (row.source().equals("null")) {
                Node node = new Node(row.value, row.type);
                nodes.put(row.id, node);
                nodeToRow.put(row.value, row);

                if (row.type.equals(NodeType.EXTERNAL_ENTITY)) {
                    externalEntityNodes.add(node);
                }
            }
        }

        for (CSV.Row row : csv.data()) {
            // data flows
            if (!row.source().equals("null")) {
                Node node = nodes.get(Integer.valueOf(row.source()));

                try {
                    node.addOutput(nodes.get(Integer.valueOf(row.target())));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        //Go through the proc
        for(Node node : nodes.values()) {
            if (node.nodeType().equals(NodeType.REASON)) {
                // Custom Process basically, adds a data flow between.
                Node processNode = nodes.get(nodeToRow.get(node.name()).for_process);
                processNode.addOutput(node);
            }
        }

        return externalEntityNodes;
    }

    public static CSV readCSV(Path csvPath) {
        ArrayList<CSV.Row> allData = new ArrayList<>();

        // create a reader
        try (Reader reader = Files.newBufferedReader(csvPath)) {

            // read library.csv file
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            for (CSVRecord record : records) {
                if (record.get(0).equals("id")) {
                    continue;
                }

                allData.add(
                        new CSV.Row(
                                Integer.parseInt(record.get("id")),
                                record.get("value"),
                                record.get("style"),
                                record.get("source"),
                                record.get("target"),
                                NodeType.get(record.get("type")),
                                "".equals(record.get("for_process")) ? null : Integer.valueOf(record.get("for_process")),
                                "".equals(record.get("for_DB")) ? null : Integer.valueOf(record.get("get_DB"))
                        )
                );
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new CSV(allData);
    }

}
