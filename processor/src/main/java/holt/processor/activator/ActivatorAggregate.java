package holt.processor.activator;

import java.util.Objects;
import java.util.Optional;

public abstract class ActivatorAggregate {

    private final ActivatorName activatorName;
    private QualifiedName qualifiedName;

    protected ActivatorAggregate(ActivatorName activatorName) {
        Objects.requireNonNull(activatorName);
        this.activatorName = activatorName;
    }

    /**
     * The name of the activator.
     * If the name of the class and the name from the graph differes,
     * then this name will be the name of the class instead.
     * @return name of the activator aggregate.
     */
    public final ActivatorName name() {
        return this.activatorName;
    }

    /**
     * If there's a class that's connected to this Activator, then
     * this function return the full qualified name for that class.
     * @return The package name
     */
    public final Optional<QualifiedName> qualifiedName() {
        return Optional.ofNullable(qualifiedName);
    }

    public final void setQualifiedName(QualifiedName qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivatorAggregate that = (ActivatorAggregate) o;
        return Objects.equals(activatorName, that.activatorName) && Objects.equals(qualifiedName, that.qualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activatorName, qualifiedName);
    }

    @Override
    public String toString() {
        return "[activatorName=" + activatorName +
                ", qualifiedName=" + qualifiedName +
                ']';
    }
}
