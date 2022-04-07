package holt.test;

import holt.processor.generation.friend.FriendsDBToFriendProcessFormatFriendQuery;
import holt.test.friend.FriendsDBQuerier;
import holt.test.friend.User;
import holt.test.friend.model.*;
import holt.test.utils.ClassAssert;
import holt.test.utils.MethodAssert;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;

import static holt.test.utils.ClassUtils.findClass;
import static holt.test.utils.MethodUtils.findMethod;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * friend.xml, that uses annotations
 */
public class TestFriend {

    private static final String FriendProcessRequirementsInterface = "holt.processor.generation.friend.FriendProcessRequirements";
    private static final String FormatFriendQueryInterface = "holt.processor.generation.friend.FriendsDBToFriendProcessFormatFriendQuery";
    private static final String FriendDBRequirementsInterface = "holt.processor.generation.friend.FriendsDBRequirements";

    @Test
    public void test_Running_Flows() {
        User user = new User();

        //Testing add friend flow
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        user.addFriend(new Name("Theodor"));
        assertThat(outputStreamCaptor.toString().trim())
                .isEqualTo("wow special NewFriend[name=Theodor]\nSaving...NewFriend[name=Theodor]");

        System.setOut(System.out);

        // Testing GF flow
        assertThat(user.getFriend(new FriendId("asdf")))
                .isEqualTo(new Friend("Smurf; Smurfsson"));
    }

    @Test
    public void test_FormatFriendProcessor() {
        ClassAssert.assertThat(findClass(FriendProcessRequirementsInterface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("addFriend", "queryFriendsDBFormatFriend", "formatFriend");

        MethodAssert.assertThat(findMethod(FriendProcessRequirementsInterface, "addFriend"))
                .hasReturnType(NewFriend.class)
                .hasParameters(Name.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        MethodAssert.assertThat(findMethod(FriendProcessRequirementsInterface, "queryFriendsDBFormatFriend"))
                .hasReturnType(FriendsDBToFriendProcessFormatFriendQuery.class)
                .hasParameters(FriendId.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        MethodAssert.assertThat(findMethod(FriendProcessRequirementsInterface, "formatFriend"))
                .hasReturnType(Friend.class)
                .hasParameters(FriendId.class, FriendRaw.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_FriendsDBToFriendProcessFormatFriendQuery() {
        ClassAssert.assertThat(findClass(FormatFriendQueryInterface))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE)
                .hasMethods("createQuery");

        MethodAssert.assertThat(findMethod(FormatFriendQueryInterface, "createQuery"))
                .hasReturnType(FriendRaw.class)
                .hasParameters(FriendsDBQuerier.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
    }

    @Test
    public void test_FriendsDBQuerier() {
        ClassAssert.assertThat(findClass(FriendDBRequirementsInterface))
                .hasMethods("AF", "getQuerierInstance")
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.INTERFACE);

        MethodAssert.assertThat(findMethod(FriendDBRequirementsInterface, "AF"))
                .hasNoReturn()
                .hasParameters(NewFriend.class)
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        MethodAssert.assertThat(findMethod(FriendDBRequirementsInterface, "getQuerierInstance"))
                .hasModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .hasNoParameters()
                .hasReturnType(FriendsDBQuerier.class);
    }

}
