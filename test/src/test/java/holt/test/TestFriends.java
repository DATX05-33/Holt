package holt.test;

import holt.processor.generation.friend_annotations.IFriendProcess;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFriends {

    @Test
    public void testFriendProcessVisibility() throws NoSuchMethodException {
        // Check that interface is public
        assertThat(Modifier.isPublic(IFriendProcess.class.getModifiers()))
                .isTrue();

        // IFriendProcess should have 3 methods, since there's two flows going through and one database query
//        var formatFriendAddFriendMethod = IFriendProcess.class.getMethod("formatFriendAddFriend");


    }

}
