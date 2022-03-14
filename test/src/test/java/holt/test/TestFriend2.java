package holt.test;

import holt.processor.generation.friend.IFriendsDBToFriendProcessformatFriendQuery;
import holt.processor.generation.friend2.IFriendsDB2;
import holt.test.utils.ClassAssert;
import holt.test.utils.MethodAssert;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static holt.test.utils.ClassUtils.findClass;
import static holt.test.utils.MethodUtils.findMethod;

/**
 * friend2.csv, no annotations
 */
public class TestFriend2 {
    private static final String IFriendProcess2Interface = "holt.processor.generation.friend2.IFriendProcess2";

    @Test
    public void test_IFormatFriend2_Interface() {
        ClassAssert.assertThat(findClass(IFriendProcess2Interface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE);
    }

}
