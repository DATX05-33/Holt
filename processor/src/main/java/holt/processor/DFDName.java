package holt.processor;

public record DFDName(String value) {
    //TODO: Add validation so that it can be a valid java package


    @Override
    public String toString() {
        return value;
    }
}
