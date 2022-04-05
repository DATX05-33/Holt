package holt.processor.activator;

public record FunctionName(String value) {

    @Override
    public String toString() {
        return value;
    }

    public String inPascalCase() {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

}
