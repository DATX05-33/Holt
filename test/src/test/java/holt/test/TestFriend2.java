package holt.test;

import holt.processor.generation.friend2.UserDatabaseToUserFetcherFetchTraverseQuery;
import holt.test.utils.ClassAssert;
import holt.test.utils.MethodAssert;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static holt.test.utils.ClassUtils.findClass;
import static holt.test.utils.MethodUtils.findMethod;

/**
 * friend2.xml
 */
public class TestFriend2 {
    private static final String UserFormatterRequirementsInterface = "holt.processor.generation.friend2.UserFormatterRequirements";
    private static final String UserFetcherRequirementsInterface = "holt.processor.generation.friend2.UserFetcherRequirements";
    private static final String UserDatabaseRequirementsInterface = "holt.processor.generation.friend2.UserDatabaseRequirements";

    @Test
    public void test_UserFormatterRequirements() {
        String storeFunctionName = "storeTraverse";

        ClassAssert.assertThat(findClass(UserFormatterRequirementsInterface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods(storeFunctionName);

        MethodAssert.assertThat(findMethod(UserFormatterRequirementsInterface, storeFunctionName))
                .hasParameters(Object.class)
                .hasReturnType(Object.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_UserQuerier() {
        ClassAssert.assertThat(findClass(UserFetcherRequirementsInterface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("fetchTraverse", "queryUserDatabaseFetchTraverse");

        MethodAssert.assertThat(findMethod(UserFetcherRequirementsInterface, "fetchTraverse"))
                .hasParameters(Object.class, Object.class)
                .hasReturnType(Object.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        MethodAssert.assertThat(findMethod(UserFetcherRequirementsInterface, "queryUserDatabaseFetchTraverse"))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .hasParameters(Object.class)
                .hasReturnType(UserDatabaseToUserFetcherFetchTraverseQuery.class);
    }

    @Test
    public void test_UserDatabaseRequirementsInterface() {
        ClassAssert.assertThat(findClass(UserDatabaseRequirementsInterface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("storeTraverse");

        MethodAssert.assertThat(findMethod(UserDatabaseRequirementsInterface, "storeTraverse"))
                .hasNoReturn()
                .hasParameters(Object.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

}
