package holt.test.friend2.friend3;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend3.FriendProcess3LimitRequirements;
import holt.processor.generation.friend3.FriendsDB3ToFriendProcess3LimitGFQuery;

@Activator
public class FriendProcess3Limit implements FriendProcess3LimitRequirements {

    @Override
    public Object AF(Object input0, Object input1) {
        return null;
    }

    @Override
    public FriendsDB3ToFriendProcess3LimitGFQuery queryFriendsDB3GF(Object input0, Object input1) {
        return null;
    }

    @Override
    public Object GF(Object input0, Object input1, Object dbInput2) {
        return null;
    }

}
