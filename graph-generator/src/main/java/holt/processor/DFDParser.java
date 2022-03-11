package holt.processor;



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

    /**
     * Note that the flows in flowsMap are not ordered in execution order.
     */
    public record DFD(List<Node> externalEntities,
                      List<Node> processes,
                      List<Node> databases,
                      Map<String, List<Dataflow>> flowsMap) { }

    public static DFD loadDfd(InputStream inputStream) {
        DFDTable table = csvToTable(inputStream);

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

        // Go through the table again, this time connecting the idToNodeMap by adding outputs
        for (DFDTable.Row row : table.data()) {
            // data flows, if from is not null then to is not either.
            if (!row.fromId().equals("null")) {
                Node source = idToNodeMap.get(Integer.valueOf(row.fromId()));
                Node target = idToNodeMap.get(Integer.valueOf(row.toId()));

                /* A flow must have the text: Something;SAF
                 * Something would be a descriptive text that explaines the flow
                 * when looking at the graph in Draw.io.
                 * It's not used here, but the text after ; is important.
                 * It's the name of the full flow through out the DFD.
                 */

                String flowName = row.name.split(";")[1];

                List<Dataflow> flows = flowsMap.computeIfAbsent(flowName, key -> new ArrayList<>());
                flows.add(new Dataflow(source, target));
            }
        }

        Collection<Node> nodes = idToNodeMap.values();

        return new DFD(
                nodes.stream().filter(DFDParser::isExternalEntity).toList(),
                nodes.stream().filter(DFDParser::isProcess).toList(),
                nodes.stream().filter(DFDParser::isDb).toList(),
                flowsMap
        );
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

    public record DFDTable(List<Row> data) {
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
