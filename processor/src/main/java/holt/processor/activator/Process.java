package holt.processor.activator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Right now, there's order in place that would be used by traverses.
 * This will be fixed in the next iteration.
 */
public final class Process implements Activator {

    private final String name;
    private final Map<FlowName, Flow> methods;

    public Process(String name) {
        this.name = name;
        this.methods = new HashMap<>();
    }

    public void addMethod(FlowName flowName, Flow bondFlow) {
        this.methods.put(flowName, bondFlow);
        //TODO: Which it's always right?
        if (bondFlow.name() == null) {
            bondFlow.setName(flowName.value());
        }
    }

    @Override
    public String name() {
        return this.name;
    }

    public Flow getFlow(FlowName flowName) {
        return this.methods.get(flowName);
    }

    public List<Flow> methods() {
        return this.methods.values().stream().toList();
    }

    @Override
    public String toString() {
        return "ProcessBond{" +
                "value='" + name + '\'' +
                ", methods=" + methods +
                '}';
    }
}
