package holt.processor;



import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class DFDParser {

    private DFDParser() {}

    public record DFD(List<Node> externalEntities,
                      List<Node> processes,
                      List<Node> databases,
                      Map<String, List<Dataflow>> flowsMap) { }

    public static DFD loadDfd(InputStream csvInputStream, InputStream optionsInputStream) {
        DFDTable table = csvToTable(csvInputStream);
        DFDOptions options = jsonToOptions(optionsInputStream);

        Map<Integer, Node> idToNodeMap = new HashMap<>();

        //First, all node rows. Go through the table once and add them to idToNodeMap and external entity idToNodeMap
        for (DFDTable.Row row : table.data()) {
            //If there's no from, then it's a node
            if (row.fromId().equals("null")) {
                Node node = new Node(row.id, row.name, row.type);
                idToNodeMap.put(row.id, node);
            }
        }

        Map<String, List<Dataflow>> flowsMap = new HashMap<>();
        // Go through the table again, this time connecting the idToNodeMap by adding outputs via order
        if (options.flowOrder != null) {


            options.flowOrder.forEach((flowName, order) -> {
            for (int current : order) {
                DFDTable.Row row = getRow(table.data, current);

                Node source = idToNodeMap.get(Integer.valueOf(row.fromId()));
                Node target = idToNodeMap.get(Integer.valueOf(row.toId()));

                List<Dataflow> flows = flowsMap.computeIfAbsent(flowName, key -> new ArrayList<>());
                flows.add(new Dataflow(source, target));
            }
            });
        }

        Collection<Node> nodes = idToNodeMap.values();

        return new DFD(
                    nodes.stream().filter(DFDParser::isExternalEntity).toList(),
                    nodes.stream().filter(DFDParser::isProcess).toList(),
                    nodes.stream().filter(DFDParser::isDb).toList(),
                    flowsMap
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
