package Holt.processor;



import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class GraphParserCSV {

    private GraphParserCSV() {}

    /**
     * Reads the CSV file from the given input stream and generates a graph from it.
     * Returns the external entities as they can be used as entry points
     * for the program.
     * @param inputStream The stream from which the csv comes from
     * @return The nodes that have the node type external entity
     */
    public static List<Node> readGraph(InputStream inputStream) {
        return generatePADFD(readCSV(inputStream));
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

    private static List<Node> generatePADFD(CSV csv) {
        List<Node> externalEntityNodes = new ArrayList<>();
        Map<Integer, Node> nodes = new HashMap<>();
        Map<String, CSV.Row> nodeToRow = new HashMap<>();

        //First, all node rows. Go through the csv once and add them to nodes and external entity nodes
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

        // Go through the csv again, this time connecting the nodes by adding outputs
        for (CSV.Row row : csv.data()) {
            // data flows, if source is not null then target is not either.
            if (!row.source().equals("null")) {
                Node node = nodes.get(Integer.valueOf(row.source()));
                node.addOutput(nodes.get(Integer.valueOf(row.target())));
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

    public static CSV readCSV(InputStream inputStream) {
        ArrayList<CSV.Row> allData = new ArrayList<>();

        // create a reader
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {

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
