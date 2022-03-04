package holt.processor;

public record Node(String name, NodeType nodeType) {
    public Node(String name, NodeType nodeType) {
        this.name = name
                .substring(0, 1)
                .toUpperCase() + name
                .substring(1)
                .replace(" ", "")
                .replace("?", "");
        this.nodeType = nodeType;
    }
}
