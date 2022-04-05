package holt.test.utils;

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
