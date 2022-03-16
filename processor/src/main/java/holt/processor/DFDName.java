package holt.processor;

public record DFDName(String value) {
    @Override
    public String toString() {
        return value;
    }
}
