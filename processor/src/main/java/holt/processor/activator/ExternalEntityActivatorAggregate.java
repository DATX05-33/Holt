package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ExternalEntityActivatorAggregate extends ActivatorAggregate {

    private final Map<TraverseName, Flow> startFlows;

    /**
     * If null, then store.
     */
    private final Map<TraverseName, Connector> endConnections;

    public ExternalEntityActivatorAggregate(ActivatorName activatorName) {
        super(activatorName);
        startFlows = new HashMap<>();
        endConnections = new HashMap<>();
    }

    public void addStartFlow(TraverseName traverseName) {
        Flow flow = new Flow();
        this.startFlows.put(traverseName, flow);
    }

    public Connector getOutput(TraverseName traverseName) {
        return this.startFlows.get(traverseName).output();
    }

    public void setOutputType(TraverseName traverseName, ClassName startOutput) {
        this.startFlows.get(traverseName).setOutputType(startOutput);
    }

    public void addEnd(TraverseName traverseName, Connector connector) {
        this.endConnections.put(traverseName, connector);
    }

    public Map<TraverseName, Flow> starts() {
        return this.startFlows;
    }

    public Optional<Connector> end(TraverseName traverseName) {
        return Optional.ofNullable(this.endConnections.get(traverseName));
    }

    @Override
    public String toString() {
        return "ExternalEntityActivatorAggregate{" +
                "info=" + super.toString() +
                ", startFlows=" + startFlows +
                ", endConnections=" + endConnections +
                '}';
    }
}
