package holt.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//TODO: Make this into a record
public final class Node {
    private final String name;
    private final NodeType nodeType;
    private final List<Dataflow> dataflows;

    public Node(String name, NodeType nodeType) {
        this.name = name
                .substring(0,1)
                .toUpperCase() + name
                .substring(1)
                .replace(" ", "")
                .replace("?", "");
        this.nodeType = nodeType;
        this.dataflows = new ArrayList<>();
    }

    public String name() {
        return name;
    }

    public NodeType nodeType() {
        return nodeType;
    }

    public List<Node> outputs() {
        List<Node> outputs = new ArrayList<>();
        for (Dataflow d : dataflows) {
            if (d.source() == this) {
                outputs.add(d.target());
            }
        }
        return outputs;
    }

    public List<Node> inputs() {
        List<Node> inputs = new ArrayList<>();
        for (Dataflow d : dataflows) {
            if (d.target() == this) {
                inputs.add(d.source());
            }
        }
        return inputs;
    }

    public void addDataflow(Dataflow dataflow) {
        this.dataflows.add(dataflow);
    }

    public List<Dataflow> dataflows() {
        return this.dataflows;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Node) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.nodeType, that.nodeType) &&
                Objects.equals(this.outputs(), that.outputs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nodeType, outputs());
    }

    @Override
    public String toString() {
        return "Node[" +
                "name=" + name + ", " +
                "nodeType=" + nodeType + ", " +
                "outputs=" + outputs() + ']';
    }
}
