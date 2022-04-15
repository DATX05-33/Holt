package holt.processor.activator;

import com.squareup.javapoet.ClassName;

public record QualifiedName(String value) {
    public ClassName className() {
        return ClassName.bestGuess(value);
    }
}
