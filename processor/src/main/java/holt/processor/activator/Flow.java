package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

public final class Flow {

    private final List<Connector> inputs;
    private final Connector output;
    private FunctionName functionName;

    public Flow() {
        this.inputs = new ArrayList<>();
        this.output = new Connector();
    }

    public void addInput(Connector input) {
        this.inputs.add(input);
    }

    public List<Connector> inputs() {
        return inputs.stream().toList();
    }

    public void setOutputType(ClassName output) {
        this.output.setType(output);
    }

    public Connector output() {
        return this.output;
    }

    public void setFunctionName(FunctionName functionName) {
        this.functionName = functionName;
    }

    public FunctionName functionName() {
        return functionName;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "inputs=" + inputs +
                ", output=" + output +
                ", functionName='" + functionName + '\'' +
                '}';
    }
}
