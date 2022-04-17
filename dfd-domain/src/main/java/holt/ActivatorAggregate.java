package holt;

import java.util.Objects;
import java.util.Optional;

public abstract class ActivatorAggregate {

    private final ActivatorName requirementsName;
    private ActivatorName activatorName;
    private ConnectedClass connectedClass;

    protected ActivatorAggregate(ActivatorName activatorName, ActivatorName requirementsName) {
        Objects.requireNonNull(activatorName);
        this.activatorName = activatorName;
        this.requirementsName = requirementsName;
    }

    // Only called when @Activator(graphName) is active
    public final void setActivatorName(ActivatorName activatorName) {
        this.activatorName = activatorName;
    }

    /**
     * The name of the activator.
     * If the name of the class and the name from the graph differs,
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
    public final Optional<ConnectedClass> connectedClass() {
        return Optional.ofNullable(connectedClass);
    }

    public void setConnectedClass(ConnectedClass connectedClass) {
        this.connectedClass = connectedClass;
    }

    public ActivatorName requirementsName() {
        return requirementsName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivatorAggregate that = (ActivatorAggregate) o;
        return Objects.equals(requirementsName, that.requirementsName) && Objects.equals(activatorName, that.activatorName) && Objects.equals(connectedClass, that.connectedClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirementsName, activatorName, connectedClass);
    }

    @Override
    public String toString() {
        return "[activatorName=" + activatorName +
                ", qualifiedName=" + connectedClass +
                ']';
    }
}
