package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ExternalEntityActivator implements Activator {

    private final ActivatorName activatorName;
    private final Map<FlowName, Flow> startFlows;
    private QualifiedName qualifiedName;

    /**
     * If null, then store.
     */
    private final Map<FlowName, Connector> endConnections;

    public ExternalEntityActivator(ActivatorName activatorName) {
        this.activatorName = activatorName;
        startFlows = new HashMap<>();
        endConnections = new HashMap<>();
    }

    @Override
    public Optional<QualifiedName> qualifiedName() {
        return Optional.ofNullable(qualifiedName);
    }

    public void setQualifiedName(QualifiedName qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public ActivatorName name() {
        return this.activatorName;
    }

    public void addStartFlow(FlowName flowName) {
        Flow flow = new Flow();
        this.startFlows.put(flowName, flow);
    }

    public Connector getOutput(FlowName flowName) {
        return this.startFlows.get(flowName).output();
    }

    public void setOutputType(FlowName flowName, ClassName startOutput) {
        this.startFlows.get(flowName).setOutputType(startOutput);
    }

    public void addEnd(FlowName flowName, Connector connector) {
        this.endConnections.put(flowName, connector);
    }

    public Map<FlowName, Flow> starts() {
        return this.startFlows;
    }

    public Optional<Connector> end(FlowName flowName) {
        return Optional.ofNullable(this.endConnections.get(flowName));
    }

    @Override
    public String toString() {
        return "ExternalEntityActivator{" +
                "activatorName=" + activatorName +
                ", startFlows=" + startFlows +
                ", endConnections=" + endConnections +
                '}';
    }
}
