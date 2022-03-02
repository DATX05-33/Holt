package holt.test.friend;

import holt.processor.annotation.DBActivator;

@DBActivator
public class FriendsDB {

    public FriendRaw getById(FriendID id) {
        return new FriendRaw(id, "Smurf", "Smurfsson");
    }

}
