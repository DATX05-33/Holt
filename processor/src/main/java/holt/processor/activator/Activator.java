package holt.processor.activator;

public sealed interface Activator permits ExternalEntity, Database, Process {
    String name();
}
