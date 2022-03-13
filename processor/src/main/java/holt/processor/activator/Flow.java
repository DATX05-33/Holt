package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

public final class Flow {

    private final List<Connector> inputs;
    private final Connector output;
    private String functionName;

    public Flow() {
        this.inputs = new ArrayList<>();
        this.output = new Connector();
    }

    public void addInput(Connector input) {
        this.inputs.add(input);
    }

    public List<Connector> getInputs() {
        return inputs.stream().toList();
    }

    public void setOutputType(ClassName output) {
        this.output.setType(output);
    }

    public Connector getOutput() {
        return this.output;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public String toString() {
        return "FlowBuilder{" +
                ", inputs=" + inputs +
                ", output=" + output +
                '}';
    }
}
