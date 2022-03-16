package Holt.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//TODO: Make this into a record
public final class Node {
    private final String name;
    private final NodeType nodeType;
    private final List<Node> outputs;

    public Node(String name, NodeType nodeType) {
        this.name = name;
        this.nodeType = nodeType;
        this.outputs = new ArrayList<>();
    }

    public String name() {
        return name;
    }

    public NodeType nodeType() {
        return nodeType;
    }

    public List<Node> outputs() {
        return outputs;
    }

    void addOutput(Node node) {
        this.outputs.add(node);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Node) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.nodeType, that.nodeType) &&
                Objects.equals(this.outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nodeType, outputs);
    }

    @Override
    public String toString() {
        return "Node[" +
                "name=" + name + ", " +
                "nodeType=" + nodeType + ", " +
                "outputs=" + outputs + ']';
    }


}
