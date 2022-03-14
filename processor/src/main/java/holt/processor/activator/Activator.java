package holt.processor.activator;

public sealed interface Activator permits ExternalEntityActivator, DatabaseActivator, ProcessActivator {
    ActivatorName name();
}
