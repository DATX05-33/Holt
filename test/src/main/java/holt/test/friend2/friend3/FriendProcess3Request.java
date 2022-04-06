package holt.test.friend2.friend3;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend3.FriendProcess3RequestRequirements;
import holt.processor.generation.friend3.FriendsDB3PolicyToFriendProcess3RequestGFQuery;

@Activator
public class FriendProcess3Request implements FriendProcess3RequestRequirements {

    @Override
    public Object AF(Object input0) {
        return null;
    }

    @Override
    public FriendsDB3PolicyToFriendProcess3RequestGFQuery queryFriendsDB3PolicyGF(Object input0) {
        return null;
    }

    @Override
    public Object GF(Object input0, Object dbInput1) {
        return null;
    }
}
