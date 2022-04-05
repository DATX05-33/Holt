package holt.processor;


import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;


public final class DFDParser {

    private DFDParser() {}

    public record DFD(List<Node> externalEntities,
                      List<Node> processes,
                      List<Node> databases,
                      Map<String, Dataflow> dataflows) { }

    public static DFD loadDfd(InputStream csvInputStream) {
        DFDTable table = csvToTable(csvInputStream);

        Map<Integer, Node> idToNodeMap = new HashMap<>();
        Map<String, Dataflow> dataflows = new HashMap<>();

        //First, all node rows. Go through the table once and add them to idToNodeMap and external entity idToNodeMap
        for (DFDTable.Row row : table.data()) {
            //If there's no from, then it's a node
            if (row.fromId().equals("null")) {
                Node node = new Node(row.id, row.name, row.type);
                idToNodeMap.put(row.id, node);
            }
        }

        // Second, all dataflow rows
        for (DFDTable.Row row : table.data()) {
            //If there's a from, then it's a dataflow
            if (!row.fromId().equals("null")) {
                Node source = idToNodeMap.get(Integer.valueOf(row.fromId()));
                Node target = idToNodeMap.get(Integer.valueOf(row.toId()));
                dataflows.put(row.name(), new Dataflow(source, target));
            }
        }

        Collection<Node> nodes = idToNodeMap.values();

        return new DFD(
                    nodes.stream().filter(DFDParser::isExternalEntity).toList(),
                    nodes.stream().filter(DFDParser::isProcess).toList(),
                    nodes.stream().filter(DFDParser::isDb).toList(),
                    dataflows
            );
    }

    private static DFDTable.Row getRow(List<DFDTable.Row> rows, int id) {
        for (DFDTable.Row row : rows) {
            if (row.id == id) {
                return row;
            }
        }

        return null;
    }

    private static boolean isExternalEntity(Node node) {
        return node.nodeType().equals(NodeType.EXTERNAL_ENTITY);
    }

    // Ignoring all custom processes that PA_DFD have.
    private static boolean isProcess(Node node) {
        // If the node is not an external entity, db or flow, then it's a process.
        return !(node.nodeType().equals(NodeType.EXTERNAL_ENTITY)
                || node.nodeType().equals(NodeType.DATA_BASE)
                || node.nodeType().equals(NodeType.DATAFLOW)
        );
    }

    private static boolean isDb(Node node) {
        return node.nodeType().equals(NodeType.DATA_BASE);
    }

    public record DFDOptions(Map<String, Integer[]> flowOrder) { }

    private static class DFDOptionsJson {
        private Map<String, Integer[]> flows;

        @Override
        public String toString() {
            return "DFDOptionsJson{" +
                    "flows=" + flows +
                    '}';
        }
    }

    private static DFDOptions jsonToOptions(InputStream inputStream) {
        DFDOptionsJson dfdOptionsJson = new Gson().fromJson(new InputStreamReader(inputStream), DFDOptionsJson.class);

        return new DFDOptions(dfdOptionsJson.flows);
    }

    private record DFDTable(List<Row> data) {
        private record Row(
                int id,
                String name,
                String style,
                String fromId,
                String toId,
                NodeType type
        ) { }
    }

    private static DFDTable csvToTable(InputStream inputStream) {
        ArrayList<DFDTable.Row> allData = new ArrayList<>();

        // create a reader
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // read .csv file
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            for (CSVRecord record : records) {
                if (record.get(0).equals("id")) {
                    continue;
                }

                allData.add(
                        new DFDTable.Row(
                                Integer.parseInt(record.get("id")),
                                record.get("value"),
                                record.get("style"),
                                record.get("source"),
                                record.get("target"),
                                NodeType.get(record.get("type"))
                        )
                );
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new DFDTable(allData);
    }

}
