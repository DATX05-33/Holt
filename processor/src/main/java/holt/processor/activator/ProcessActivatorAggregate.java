package holt.processor.activator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProcessActivatorAggregate extends ActivatorAggregate {

    private final Map<TraverseName, Flow> flows;

    public ProcessActivatorAggregate(ActivatorName activatorName) {
        super(activatorName);
        this.flows = new HashMap<>();
    }

    public void addFlow(TraverseName traverseName) {
        Flow flow = new Flow();
        flow.setFunctionName(new FunctionName(traverseName.value()));
        this.flows.put(traverseName, flow);
    }

    public List<Flow> getFlows() {
        return this.flows.values().stream().toList();
    }

    public Flow getFlow(TraverseName traverseName) {
        return this.flows.get(traverseName);
    }

    public void addInputToFlow(TraverseName traverseName, Connector connector) {
        Flow flow = this.flows.get(traverseName);
        if (flow == null) {
            throw new IllegalStateException("Cannot add input to a flow that have not been added yet, flowName:" + traverseName.value());
        }

        this.flows.get(traverseName).addInput(connector);
    }

    public Connector getOutput(TraverseName traverseName) {
        return this.flows.get(traverseName).output();
    }

    @Override
    public String toString() {
        return "ProcessActivatorAggregate{" +
                "info=" + super.toString() +
                ", flows=" + flows +
                '}';
    }
}
