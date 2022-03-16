package Holt.processor;

import java.util.Arrays;

public enum NodeType {
    EXTERNAL_ENTITY("external_entity"),
    CUSTOM_PROCESS("process"),
    REASON("reason"),
    REQUEST("request"),
    LIMIT("limit"),
    LOG("log"),
    DB_LOG("DB_log"),
    DATA_FLOW(null);

    NodeType(String name) {
        this.name = name;
    }

    private final String name;

    public static NodeType get(String name) {
        return Arrays.stream(NodeType.values()).filter(nodeType -> name.equals(nodeType.name)).findAny()
                .orElse(DATA_FLOW); // TODO: Possible type that is not a Data flow
        //.orElseThrow(() -> new IllegalArgumentException("Cannot find NodeType for name " + name));
    }
}