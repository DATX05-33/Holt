package holt.processor.activator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TraverseOutput {
    private final FunctionName functionName;
    private final List<Connector> inputs;

    public TraverseOutput(FunctionName functionName) {
        this.functionName = functionName;
        this.inputs = new ArrayList<>();
    }

    public FunctionName functionName() {
        return functionName;
    }

    public List<Connector> inputs() {
        return inputs;
    }

    public void addInput(Connector input) {
        if (this.inputs.contains(input)) {
            throw new IllegalStateException("Input already exists " + input);
        }
        this.inputs.add(input);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TraverseOutput) obj;
        return Objects.equals(this.functionName, that.functionName) &&
                Objects.equals(this.inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionName, inputs);
    }

    @Override
    public String toString() {
        return "TraverseEnd[" +
                "functionName=" + functionName + ", " +
                "inputs=" + inputs + ']';
    }

}
