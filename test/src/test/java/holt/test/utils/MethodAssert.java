package holt.test.utils;

import org.assertj.core.api.AbstractAssert;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

public class MethodAssert extends AbstractAssert<MethodAssert, Method> {
    private MethodAssert(Method actual) {
        super(actual, MethodAssert.class);
    }

    public static MethodAssert assertThat(Method actual) {
        return new MethodAssert(actual);
    }

    public MethodAssert hasReturnType(Class<?> returnType) {
        isNotNull();

        if (!Objects.equals(actual.getReturnType(), returnType)) {
            failWithMessage(
                    "Expected return type to be <%s>, but it was <%s>",
                    actual.getName(),
                    returnType,
                    actual.getReturnType()
            );
        }

        return this;
    }

    public MethodAssert hasNoReturn() {
        isNotNull();

        if (!actual.getReturnType().getName().equals("void")) {
            failWithMessage(
                    "Expected method to return nothing (void)"
            );
        }

        return this;
    }

    public MethodAssert hasParameters(Class<?>... parameters) {
        isNotNull();

        if (parameters.length != actual.getParameterCount()) {
            failWithMessage(
                    "Expected parameters count to be <%s> but was <%s>",
                    parameters.length,
                    actual.getParameterCount()
            );
        } else {
            for (int i = 0; i < parameters.length; i++) {
                if (!Objects.equals(parameters[i], actual.getParameters()[i].getType())) {
                    failWithMessage(
                            "Mismatch in parameters, expected:\n <%s>\n but was:\n <%s>",
                            Arrays.toString(parameters),
                            Arrays.toString(actual.getParameters())
                    );
                }
            }
        }


        return this;
    }

    public MethodAssert hasNoParameters() {
        isNotNull();

        if (actual.getParameterCount() > 0) {
            failWithMessage(
                    "Expected no parameters"
            );
        }

        return this;
    }

    public MethodAssert hasModifiers(int... modifiers) {
        isNotNull();

        int allModifiers = 0;
        for (int modifier : modifiers) {
            allModifiers |= modifier;
        }

        if (!Objects.equals(allModifiers, actual.getModifiers())) {
            failWithMessage(
                    "Expected modifier to be <%s> but was <%s>",
                    Modifier.toString(allModifiers),
                    Modifier.toString(actual.getModifiers())
            );
        }

        return this;
    }

    @Override
    protected void failWithMessage(String errorMessage, Object... arguments) {
        super.failWithMessage("[" + actual.getName() + "] " + errorMessage, arguments);
    }
}
