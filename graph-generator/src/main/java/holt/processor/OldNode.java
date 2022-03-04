package holt.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//TODO: Make this into a record
public final class OldNode {
    private final String name;
    private final NodeType nodeType;
    private final List<OldDataflow> dataflows;

    public OldNode(String name, NodeType nodeType) {
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

    public List<OldNode> outputs() {
        List<OldNode> outputs = new ArrayList<>();
        for (OldDataflow d : dataflows) {
            if (d.source() == this) {
                outputs.add(d.target());
            }
        }
        return outputs;
    }

    public List<OldNode> inputs() {
        List<OldNode> inputs = new ArrayList<>();
        for (OldDataflow d : dataflows) {
            if (d.target() == this) {
                inputs.add(d.source());
            }
        }
        return inputs;
    }

    public void addDataflow(OldDataflow dataflow) {
        this.dataflows.add(dataflow);
    }

    public List<OldDataflow> dataflows() {
        return this.dataflows;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (OldNode) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.nodeType, that.nodeType) &&
                Objects.equals(this.dataflows, that.dataflows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nodeType, dataflows);
    }

    @Override
    public String toString() {
        return "Node[" +
                "name=" + name + ", " +
                "nodeType=" + nodeType + ", " +
                "outputs=" + dataflows() + ']';
    }
}
