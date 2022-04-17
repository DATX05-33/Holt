package holt;

public record ActivatorName(String value) {

    @Override
    public String toString() {
        return value;
    }

    public String asVariableName() {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }
}
