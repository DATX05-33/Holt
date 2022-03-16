package holt.test.utils;

import java.lang.reflect.Method;

import static holt.test.utils.ClassUtils.findClass;

public final class MethodUtils {

    private MethodUtils() {}

    public static Method findMethod(String className, String methodName) {
        Class<?> clazz = findClass(className);
        for (Method method : clazz.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }

        throw new AssertionError("Could not find method with name " + methodName + " in class " + clazz.getName());
    }

}
