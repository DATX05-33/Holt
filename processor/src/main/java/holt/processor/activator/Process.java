package holt.processor.activator;

import holt.processor.DFDName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Right now, there's order in place that would be used by traverses.
 * This will be fixed in the next iteration.
 */
public final class Process implements Activator {

    private final ActivatorName activatorName;
    private final Map<FlowName, Flow> flows;

    public Process(ActivatorName activatorName) {
        this.activatorName = activatorName;
        this.flows = new HashMap<>();
    }

    @Override
    public ActivatorName name() {
        return this.activatorName;
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
        return "Process{" +
                "activatorName=" + activatorName +
                ", methods=" + flows +
                '}';
    }
}
