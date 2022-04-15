package holt.processor.activator;

import java.util.Locale;

public record FunctionName(String value) {

    public static FunctionName of(TraverseName traverseName) {
        return new FunctionName(traverseName.value());
    }
    @Override
    public String toString() {
        return value;
    }

    public String inPascalCase() {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

}
