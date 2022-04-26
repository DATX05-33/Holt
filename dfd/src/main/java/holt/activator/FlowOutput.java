package holt.activator;

public record FlowOutput(QualifiedName type, boolean isCollection) {
    @Override
    public String toString() {
        return "FlowOutput{" +
                "type=" + type +
                ", isCollection=" + isCollection +
                '}';
    }
}
