package holt.processor.bond;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Right now, there's order in place that would be used by traverses.
 * This will be fixed in the next iteration.
 */
public class ProcessBond implements Bond {

    private final String name;
    private final Map<FlowName, BondFlow> methods;

    public ProcessBond(String name) {
        this.name = name;
        this.methods = new HashMap<>();
    }

    public void addMethod(FlowName flowName, BondFlow bondFlow) {
        this.methods.put(flowName, bondFlow);
        //TODO: Which it's always right?
        if (bondFlow.name() == null) {
            bondFlow.setName(flowName.value());
        }
    }

    public BondFlow getFlow(FlowName flowName) {
        return this.methods.get(flowName);
    }

    public List<BondFlow> methods() {
        return this.methods.values().stream().toList();
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "ProcessBond{" +
                "value='" + name + '\'' +
                ", methods=" + methods +
                '}';
    }
}
