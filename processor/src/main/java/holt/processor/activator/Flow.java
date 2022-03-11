package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class Flow {

    //methodName
    private String name;
    private final List<Flow> inputs;
    private TypeMirror output;

    public Flow() {
        this.inputs = new ArrayList<>();
    }

    public void addInput(Flow flow) {
        this.inputs.add(flow);
    }

    public List<Flow> inputs() {
        return inputs.stream().toList();
    }

    public void setOutput(TypeMirror output) {
        this.output = output;
    }

    public ClassName output() {
        if (this.output == null) {
            return ClassName.get(Object.class);
        } else {
            return ClassName.bestGuess(this.output.toString());
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "BondFlow{" +
                "value='" + name + '\'' +
                ", inputs=" + inputs +
                ", output=" + output +
                '}';
    }
}
