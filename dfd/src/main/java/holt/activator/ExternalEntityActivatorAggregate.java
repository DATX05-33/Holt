package holt.activator;

import holt.Metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ExternalEntityActivatorAggregate extends ActivatorAggregate implements OutputActivator {

    private final Map<TraverseName, List<Connector>> starts;
    private final Map<TraverseName, TraverseOutput> ends;
    private final Map<TraverseName, List<AddConnectors>> addConnectorsMap;

    public ExternalEntityActivatorAggregate(ActivatorId activatorId, ActivatorName activatorName, Metadata metadata) {
        super(activatorId, activatorName, new ActivatorName("Abstract" + activatorName.value()), metadata);
        starts = new HashMap<>();
        ends = new HashMap<>();
        addConnectorsMap = new HashMap<>();
    }

    public void addStart(TraverseName traverseName, List<FlowOutput> flowOutputs) {
        this.starts.put(traverseName, new ArrayList<>());
        for (FlowOutput flowOutput : flowOutputs) {
            this.starts.get(traverseName).add(new Connector(flowOutput));
        }
        for (AddConnectors addConnectors : this.addConnectorsMap.get(traverseName)) {
            addConnectors.add(this.starts.get(traverseName));
        }
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

    public Map<TraverseName, List<Connector>> starts() {
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

    public void addLateConnector(TraverseName traverseName, AddConnectors addConnectors) {
        if (!addConnectorsMap.containsKey(traverseName)) {
            addConnectorsMap.put(traverseName, new ArrayList<>());
        }
        addConnectorsMap.get(traverseName).add(addConnectors);
    }

    public interface AddConnectors {
        void add(List<Connector> connectors);
    }

}
