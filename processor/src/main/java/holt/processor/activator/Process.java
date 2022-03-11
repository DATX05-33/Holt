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
    private final DFDName dfdName;
    private final Map<FlowName, Flow> methods;

    public Process(ActivatorName activatorName, DFDName dfdName) {
        this.activatorName = activatorName;
        this.dfdName = dfdName;
        this.methods = new HashMap<>();
    }

    public void addMethod(FlowName flowName, Flow flow) {
        this.methods.put(flowName, flow);
        //TODO: Which it's always right?
        if (flow.functionName() == null) {
            flow.setFunctionName(flowName.value());
        }
    }

    @Override
    public ActivatorName name() {
        return this.activatorName;
    }

    @Override
    public DFDName dfd() {
        return this.dfdName;
    }

    public Flow getFlow(FlowName flowName) {
        return this.methods.get(flowName);
    }

    public List<Flow> methods() {
        return this.methods.values().stream().toList();
    }

    @Override
    public String toString() {
        return "Process{" +
                "activatorName=" + activatorName +
                ", dfdName=" + dfdName +
                ", methods=" + methods +
                '}';
    }
}
