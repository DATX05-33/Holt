package holt.processor;

public record Node(int id, String name, NodeType nodeType) {
    public Node(int id, String name, NodeType nodeType) {
        this.id = id;
        this.name = name
                .substring(0, 1)
                .toUpperCase() + name
                .substring(1)
                .replace(" ", "")
                .replace("?", "");
        this.nodeType = nodeType;
    }
}
