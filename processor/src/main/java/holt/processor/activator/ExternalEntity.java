package holt.processor.activator;

import com.squareup.javapoet.ClassName;
import holt.processor.DFDName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ExternalEntity implements Activator {

    private final ActivatorName activatorName;
    private final DFDName dfdName;
    private final Map<FlowName, Flow> startFlows;

    /**
     * If null, then store.
     */
    private final Map<FlowName, Flow> endFlows;

    public ExternalEntity(ActivatorName activatorName, DFDName dfdName) {
        this.activatorName = activatorName;
        this.dfdName = dfdName;
        startFlows = new HashMap<>();
        endFlows = new HashMap<>();
    }

    @Override
    public ActivatorName name() {
        return this.activatorName;
    }

    @Override
    public DFDName dfd() {
        return this.dfdName;
    }

    public Flow addFlow(FlowName flowName) {
        Flow flow = new Flow();
        this.startFlows.put(flowName, flow);
        return flow;
    }

    public void setOutputType(FlowName flowName, ClassName startOutput) {
        this.startFlows.get(flowName).setOutput(startOutput);
    }

    public void addEnd(FlowName flowName, Flow flow) {
        this.endFlows.put(flowName, flow);
    }

    public Map<FlowName, Flow> starts() {
        return this.startFlows;
    }

    public Optional<Flow> end(FlowName flowName) {
        return Optional.ofNullable(this.endFlows.get(flowName));
    }

    @Override
    public String toString() {
        return "ExternalEntity{" +
                "activatorName=" + activatorName +
                ", dfdName=" + dfdName +
                ", startFlows=" + startFlows +
                ", endFlows=" + endFlows +
                '}';
    }
}
