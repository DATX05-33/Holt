package holt.test.friend2.friend3;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend3.FriendProcess3LimitAF1Requirements;
import holt.processor.generation.friend3.FriendProcess3LimitGF1Requirements;
import holt.processor.generation.friend3.FriendProcess3LimitGF2Requirements;
import holt.processor.generation.friend3.FriendsDB3ToFriendProcess3LimitGF2GFQuery;

@Activator
public class FriendProcess3Limit implements FriendProcess3LimitAF1Requirements, FriendProcess3LimitGF1Requirements, FriendProcess3LimitGF2Requirements {

    @Override
    public Object AF(Object input0, Object input1) {
        return null;
    }

    @Override
    public Object GF(Object input0, Object input1) {
        return null;
    }

    @Override
    public FriendsDB3ToFriendProcess3LimitGF2GFQuery queryFriendsDB3GF(Object input0) {
        return null;
    }

}
