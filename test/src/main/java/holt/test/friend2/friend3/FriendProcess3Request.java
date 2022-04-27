package holt.test.friend2.friend3;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend3.FriendProcess3RequestAF1Requirements;
import holt.processor.generation.friend3.FriendProcess3RequestGF1Requirements;
import holt.processor.generation.friend3.FriendProcess3RequestGF2Requirements;
import holt.processor.generation.friend3.FriendsDB3PolicyToFriendProcess3RequestGF2GFQuery;

@Activator
public class FriendProcess3Request implements FriendProcess3RequestAF1Requirements, FriendProcess3RequestGF1Requirements, FriendProcess3RequestGF2Requirements {

    @Override
    public Object AF(Object input0) {
        return null;
    }

    @Override
    public FriendsDB3PolicyToFriendProcess3RequestGF2GFQuery queryFriendsDB3PolicyGF() {
        return null;
    }

    @Override
    public Object GF(Object input0) {
        return null;
    }
}
