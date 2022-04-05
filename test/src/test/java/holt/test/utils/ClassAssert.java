package holt.test.utils;

import org.assertj.core.api.AbstractAssert;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClassAssert extends AbstractAssert<ClassAssert, Class<?>> {

    protected ClassAssert(Class<?> aClass) {
        super(aClass, ClassAssert.class);
    }

    public static ClassAssert assertThat(Class<?> clazz) {
        return new ClassAssert(clazz);
    }

    public ClassAssert hasModifiers(int... modifiers) {
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

    public ClassAssert hasMethods(String... methodNames) {
        isNotNull();

        if (!Objects.equals(methodNames.length, actual.getMethods().length)) {
            failWithMessage(
                    "Mismatch in the number of methods, expected <%s> but was <%s>",
                    methodNames.length,
                    actual.getMethods().length
            );
        } else {
            List<String> actualMethodNames = Arrays.stream(actual.getMethods()).map(Method::getName).toList();

            for (String methodName : methodNames) {
                if (!actualMethodNames.contains(methodName)) {
                    failWithMessage(
                            "Mismatch in methods, expected:\n <%s>\n but was:\n <%s>",
                            Arrays.toString(methodNames),
                            Arrays.stream(actual.getMethods()).map(Method::getName).toList()
                    );
                }
            }
        }

        return this;
    }

    @Override
    protected void failWithMessage(String errorMessage, Object... arguments) {
        super.failWithMessage("[" + actual.getName() + "] " + errorMessage, arguments);
    }

}
