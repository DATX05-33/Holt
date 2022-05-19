package holt.test;

import holt.test.utils.ClassAssert;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static holt.test.utils.ClassUtils.findClass;

/**
 * friend2.xml, no annotations
 */
public class TestFriend3 {
    private static final String FriendProcessRequirements3Interface = "holt.processor.generation.friend3.FriendProcess3Requirements";

    @Test
    public void test_IFormatFriend3_Interface() {
        ClassAssert.assertThat(findClass(FriendProcessRequirements3Interface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE);
    }

}
