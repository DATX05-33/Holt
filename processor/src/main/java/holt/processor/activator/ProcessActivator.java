package holt.processor.activator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Right now, there's order in place that would be used by traverses.
 * This will be fixed in the next iteration.
 */
public final class ProcessActivator implements Activator {

    private final ActivatorName activatorName;
    private final Map<FlowName, Flow> flows;
    private QualifiedName qualifiedName;

    public ProcessActivator(ActivatorName activatorName) {
        this.activatorName = activatorName;
        this.flows = new HashMap<>();
    }

    @Override
    public ActivatorName name() {
        return this.activatorName;
    }

    @Override
    public Optional<QualifiedName> qualifiedName() {
        return Optional.ofNullable(this.qualifiedName);
    }

    public void setQualifiedName(QualifiedName qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public void addFlow(FlowName flowName) {
        Flow flow = new Flow();
        flow.setFunctionName(flowName.value());
        this.flows.put(flowName, flow);
    }

    public List<Flow> getFlows() {
        return this.flows.values().stream().toList();
    }

    public Flow getFlow(FlowName flowName) {
        return this.flows.get(flowName);
    }

    public void addInputToFlow(FlowName flowName, Connector connector) {
        this.flows.get(flowName).addInput(connector);
    }

    public Connector getOutput(FlowName flowName) {
        return this.flows.get(flowName).output();
    }

    @Override
    public String toString() {
        return "ProcessActivator{" +
                "activatorName=" + activatorName +
                ", flows=" + flows +
                '}';
    }
}
