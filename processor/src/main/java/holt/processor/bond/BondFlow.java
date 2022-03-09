package holt.processor.bond;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class BondFlow {

    //methodName
    private String name;
    private final List<BondFlow> inputs;

    /**
     * If null, then Object will be used
     */
    private TypeMirror output;

    public BondFlow() {
        this.inputs = new ArrayList<>();
    }

    public void addInput(BondFlow bondFlow) {
        this.inputs.add(bondFlow);
    }

    public List<BondFlow> inputs() {
        return inputs.stream().toList();
    }

    public void setOutput(TypeMirror output) {
        this.output = output;
    }

    public TypeMirror output() {
        return this.output;
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
