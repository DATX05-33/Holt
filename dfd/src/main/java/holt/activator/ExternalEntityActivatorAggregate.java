package holt.activator;

import holt.Metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ExternalEntityActivatorAggregate extends ActivatorAggregate implements OutputActivator {

    private final Map<TraverseName, Connector> starts;
    private final Map<TraverseName, TraverseOutput> ends;

    public ExternalEntityActivatorAggregate(ActivatorName activatorName, Metadata metadata) {
        super(activatorName, new ActivatorName("Abstract" + activatorName.value()), metadata);
        starts = new HashMap<>();
        ends = new HashMap<>();
    }

    public void addStart(TraverseName traverseName) {
        Connector connector = new Connector();
        this.starts.put(traverseName, connector);
    }

    public Connector getStartConnector(TraverseName traverseName) {
        return this.starts.get(traverseName);
    }

    public void setOutputType(TraverseName traverseName, QualifiedName startOutput) {
        this.starts.get(traverseName).setType(startOutput);
    }

    public void addOutput(TraverseName traverseName) {
        TraverseOutput traverseOutput = new TraverseOutput(FunctionName.of(traverseName));
        this.ends.put(traverseName, traverseOutput);
    }

    @Override
    public void addInputToTraverseOutput(TraverseName traverseName, Connector connector) {
        this.ends.get(traverseName).addInput(connector);
    }

    /**
     *
     * @return TraverseEnds that doesn't originate in this external entity
     */
    @Override
    public Map<TraverseName, TraverseOutput> outputs() {
        return this.ends.entrySet()
                .stream()
                // Remove ends that also is a start
                .filter(entry -> !starts.containsKey(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public Map<TraverseName, Connector> starts() {
        return this.starts;
    }

    public Optional<TraverseOutput> end(TraverseName traverseName) {
        return Optional.ofNullable(this.ends.get(traverseName));
    }

    @Override
    public String toString() {
        return "ExternalEntityActivatorAggregate{" +
                "starts=" + starts +
                ", ends=" + ends +
                '}';
    }
}
