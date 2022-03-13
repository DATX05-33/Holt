package holt.test;

import holt.processor.generation.friend.IFriendsDB;
import holt.processor.generation.friend.IFriendsDBToFriendProcessformatFriendQuery;
import holt.processor.generation.friend2.IFriendsDB2;
import holt.processor.generation.friend2.IFriendsDB2ToFriendProcess2GFQuery;
import holt.test.friend.model.Friend;
import holt.test.friend.model.FriendId;
import holt.test.friend.model.FriendRaw;
import holt.test.friend.model.Name;
import holt.test.friend.model.NewFriend;
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
    private static final String FormatFriendQuery2Interface = "holt.processor.generation.friend2.IFriendsDB2ToFriendProcess2GFQuery";

    @Test
    public void test_IFormatFriend_Interface() {
        ClassAssert.assertThat(findClass(IFriendProcess2Interface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("AF", "query_FriendsDB2_GF", "GF");
    }

    @Test
    public void test_addFriend_Method() {
        MethodAssert.assertThat(findMethod(IFriendProcess2Interface, "AF"))
                .hasReturnType(Object.class)
                .hasParameters(Object.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_query_FriendsDB_formatFriend_Method() {
        MethodAssert.assertThat(findMethod(IFriendProcess2Interface, "query_FriendsDB2_GF"))
                .hasReturnType(IFriendsDB2ToFriendProcess2GFQuery.class)
                .hasParameters(Object.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_formatFriend_Method() {
        MethodAssert.assertThat(findMethod(IFriendProcess2Interface, "GF"))
                .hasReturnType(Object.class)
                .hasParameters(Object.class, Object.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_IFriendsDBToFriendProcessformatFriendQuery_Interface() {
        ClassAssert.assertThat(findClass(FormatFriendQuery2Interface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("createQuery");
    }

    @Test
    public void test_createQuery_Method() {
        MethodAssert.assertThat(findMethod(FormatFriendQuery2Interface, "createQuery"))
                .hasReturnType(Object.class)
                .hasParameters(IFriendsDB2.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

    }
}
