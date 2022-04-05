package holt.processor.activator;

public record ActivatorName(String value) {

    @Override
    public String toString() {
        return value;
    }

}
