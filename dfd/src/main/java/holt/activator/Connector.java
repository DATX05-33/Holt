package holt.activator;

import java.util.Objects;

public final class Connector {

    private FlowOutput flowOutput;

    public Connector() {
        flowOutput = new FlowOutput(
                QualifiedName.OBJECT,
                false
        );
    }

    public Connector(FlowOutput flowOutput) {
        this.flowOutput = flowOutput;
    }

    public FlowOutput flowOutput() {
        return flowOutput;
    }

    public void setIsCollection(boolean collection) {
        this.flowOutput = new FlowOutput(
                flowOutput.type(),
                collection
        );
    }

    public void setType(QualifiedName type) {
        Objects.requireNonNull(type);
        this.flowOutput = new FlowOutput(
                type,
                this.flowOutput.isCollection()
        );
    }


    @Override
    public String toString() {
        return "Connector{" +
                "hashcode=" + super.hashCode() +
                ", flowOutput=" + this.flowOutput +
                '}';
    }
}
