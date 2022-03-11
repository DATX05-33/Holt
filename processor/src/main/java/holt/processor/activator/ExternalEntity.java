package holt.processor.activator;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ExternalEntity implements Activator {

    private final String name;
    private final Map<FlowName, Flow> startBondFlows;

    /**
     * If null, then store.
     */
    private final Map<FlowName, Flow> endBondFlows;

    public ExternalEntity(String name) {
        this.name = name;
        startBondFlows = new HashMap<>();
        endBondFlows = new HashMap<>();
    }

    @Override
    public String name() {
        return this.name;
    }

    public Flow addFlow(FlowName flowName) {
        Flow bondFlow = new Flow();
        this.startBondFlows.put(flowName, bondFlow);
        return bondFlow;
    }

    public void setOutputType(FlowName flowName, TypeMirror typeMirror) {
        this.startBondFlows.get(flowName).setOutput(typeMirror);
    }

    public void addEnd(FlowName flowName, Flow bondFlow) {
        this.endBondFlows.put(flowName, bondFlow);
    }

    public Map<FlowName, Flow> starts() {
        return this.startBondFlows;
    }

    public Optional<Flow> end(FlowName flowName) {
        return Optional.ofNullable(this.endBondFlows.get(flowName));
    }

    @Override
    public String toString() {
        return "ExternalEntityBond{" +
                "startBondFlows=" + startBondFlows +
                '}';
    }
}
