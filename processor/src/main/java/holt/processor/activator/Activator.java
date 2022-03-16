package holt.processor.activator;

import java.util.Optional;

public sealed interface Activator permits ExternalEntityActivator, DatabaseActivator, ProcessActivator {
    ActivatorName name();

    /**
     * If there's a class that's connected to this Activator, then
     * this function return the full qualified name for that class.
     * @return The package name
     */
    Optional<QualifiedName> qualifiedName();
}
