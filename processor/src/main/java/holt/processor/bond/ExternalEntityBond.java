package holt.processor.bond;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExternalEntityBond implements Bond {

    private final String name;
    private final Map<FlowName, BondFlow> startBondFlows;

    /**
     * If null, then store.
     */
    private final Map<FlowName, BondFlow> endBondFlows;

    public ExternalEntityBond(String name) {
        this.name = name;
        startBondFlows = new HashMap<>();
        endBondFlows = new HashMap<>();
    }

    public String name() {
        return this.name;
    }

    public BondFlow addFlow(FlowName flowName) {
        BondFlow bondFlow = new BondFlow();
        this.startBondFlows.put(flowName, bondFlow);
        return bondFlow;
    }

    public void setOutputType(FlowName flowName, TypeMirror typeMirror) {
        this.startBondFlows.get(flowName).setOutput(typeMirror);
    }

    public void addEnd(FlowName flowName, BondFlow bondFlow) {
        this.endBondFlows.put(flowName, bondFlow);
    }

    public Map<FlowName, BondFlow> starts() {
        return this.startBondFlows;
    }

    public Optional<BondFlow> end(FlowName flowName) {
        return Optional.ofNullable(this.endBondFlows.get(flowName));
    }

    @Override
    public String toString() {
        return "ExternalEntityBond{" +
                "startBondFlows=" + startBondFlows +
                '}';
    }
}
