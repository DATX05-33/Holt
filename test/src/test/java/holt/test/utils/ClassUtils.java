package holt.test.utils;

import java.lang.reflect.Modifier;
import java.util.Objects;

public final class ClassUtils {

    private ClassUtils() { }

    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Could not find Class with name " + className);
        }
    }

}
