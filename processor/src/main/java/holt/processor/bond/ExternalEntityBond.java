package holt.processor.bond;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

public class ExternalEntityBond implements Bond {

    private final String name;
    private final Map<FlowName, BondFlow> startBondFlows;

    public ExternalEntityBond(String name) {
        this.name = name;
        startBondFlows = new HashMap<>();
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

    public Map<FlowName, BondFlow> starts() {
        return this.startBondFlows;
    }

    @Override
    public String toString() {
        return "ExternalEntityBond{" +
                "startBondFlows=" + startBondFlows +
                '}';
    }
}
