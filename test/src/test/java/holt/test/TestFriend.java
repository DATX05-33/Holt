package holt.test;

import holt.processor.generation.friend.IFriendsDB;
import holt.processor.generation.friend.IFriendsDBToFriendProcessformatFriendQuery;
import holt.test.friend.FriendsDB;
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
import static org.assertj.core.api.Assertions.assertThat;

/**
 * friend.csv, that uses annotations
 */
public class TestFriend {

    private static final String IFriendProcessInterface = "holt.processor.generation.friend.IFriendProcess";
    private static final String FormatFriendQueryInterface = "holt.processor.generation.friend.IFriendsDBToFriendProcessformatFriendQuery";

    @Test
    public void test_IFormatFriend_Interface() {
        ClassAssert.assertThat(findClass(IFriendProcessInterface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("addFriend", "query_FriendsDB_formatFriend", "formatFriend");
    }

    @Test
    public void test_addFriend_Method() {
        MethodAssert.assertThat(findMethod(IFriendProcessInterface, "addFriend"))
                .hasReturnType(NewFriend.class)
                .hasParameters(Name.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_query_FriendsDB_formatFriend_Method() {
        MethodAssert.assertThat(findMethod(IFriendProcessInterface, "query_FriendsDB_formatFriend"))
                .hasReturnType(IFriendsDBToFriendProcessformatFriendQuery.class)
                .hasParameters(FriendId.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_formatFriend_Method() {
        MethodAssert.assertThat(findMethod(IFriendProcessInterface, "formatFriend"))
                .hasReturnType(Friend.class)
                .hasParameters(FriendId.class, FriendRaw.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_IFriendsDBToFriendProcessformatFriendQuery_Interface() {
        ClassAssert.assertThat(findClass(FormatFriendQueryInterface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("createQuery");
    }

    @Test
    public void test_createQuery_Method() {
        MethodAssert.assertThat(findMethod(FormatFriendQueryInterface, "createQuery"))
                .hasReturnType(FriendRaw.class)
                .hasParameters(FriendsDB.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

    }

}
