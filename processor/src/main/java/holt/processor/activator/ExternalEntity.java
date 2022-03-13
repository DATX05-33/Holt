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
    private final Map<FlowName, Connector> endConnections;

    public ExternalEntity(ActivatorName activatorName, DFDName dfdName) {
        this.activatorName = activatorName;
        this.dfdName = dfdName;
        startFlows = new HashMap<>();
        endConnections = new HashMap<>();
    }

    @Override
    public ActivatorName name() {
        return this.activatorName;
    }

    @Override
    public DFDName dfd() {
        return this.dfdName;
    }

    public void addStartFlow(FlowName flowName) {
        Flow flow = new Flow();
        this.startFlows.put(flowName, flow);
    }

    public Connector getOutput(FlowName flowName) {
        return this.startFlows.get(flowName).getOutput();
    }

    public void setOutputType(FlowName flowName, ClassName startOutput) {
        this.startFlows.get(flowName).setOutputType(startOutput);
    }

    public void addEnd(FlowName flowName, Connector connector) {
        this.endConnections.put(flowName, connector);
    }

    public Map<FlowName, Flow> starts() {
        return this.startFlows;
    }

    public Optional<Connector> end(FlowName flowName) {
        return Optional.ofNullable(this.endConnections.get(flowName));
    }

    @Override
    public String toString() {
        return "ExternalEntity{" +
                "activatorName=" + activatorName +
                ", dfdName=" + dfdName +
                ", startFlows=" + startFlows +
                ", endFlows=" + endConnections +
                '}';
    }
}
