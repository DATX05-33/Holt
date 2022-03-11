package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

public class Flow {

    //methodName
    private String functionName;
    private final List<Flow> inputs;
    private ClassName output;

    public Flow() {
        this.inputs = new ArrayList<>();
    }

    public void addInput(Flow flow) {
        this.inputs.add(flow);
    }

    public List<Flow> inputs() {
        return inputs.stream().toList();
    }

    public void setOutput(ClassName output) {
        this.output = output;
    }

    public ClassName output() {
        if (this.output == null) {
            return ClassName.get(Object.class);
        } else {
            return output;
        }
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String functionName() {
        return this.functionName;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "value='" + functionName + '\'' +
                ", inputs=" + inputs +
                ", output=" + output +
                '}';
    }
}
