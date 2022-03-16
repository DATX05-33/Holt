package holt.processor;

import java.util.Arrays;

public enum NodeType {
    EXTERNAL_ENTITY("external_entity"),
    PROCESS("process"),
    DATA_BASE("data_base"),
    DATAFLOW(null);

    NodeType(String name) {
        this.name = name;
    }

    private final String name;

    public static NodeType get(String name) {
        return Arrays.stream(NodeType.values()).filter(nodeType -> name.equals(nodeType.name)).findAny()
                .orElse(DATAFLOW);
    }
}