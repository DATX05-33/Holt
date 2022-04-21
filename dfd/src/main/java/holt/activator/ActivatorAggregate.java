package holt.activator;

import holt.Metadata;

import java.util.Objects;
import java.util.Optional;

public abstract class ActivatorAggregate {

    private final ActivatorId activatorId;
    private final ActivatorName requirementsName;
    private ActivatorName activatorName;
    private ConnectedClass connectedClass;
    private final Metadata metadata;
    protected ActivatorAggregate(ActivatorId activatorId, ActivatorName activatorName, ActivatorName requirementsName) {
        this(activatorId, activatorName, requirementsName, null);
    }

    protected ActivatorAggregate(ActivatorId activatorId, ActivatorName activatorName, ActivatorName requirementsName, Metadata metadata) {
        Objects.requireNonNull(activatorId);
        Objects.requireNonNull(activatorName);
        this.activatorId = activatorId;
        this.activatorName = activatorName;
        this.requirementsName = requirementsName;
        this.metadata = metadata;
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

    public Metadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivatorAggregate that = (ActivatorAggregate) o;
        return activatorId.equals(that.activatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activatorId);
    }

    @Override
    public String toString() {
        return "[activatorName=" + activatorName +
                ", qualifiedName=" + connectedClass +
                ']';
    }
}
