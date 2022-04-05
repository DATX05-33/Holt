package holt.test.friend;

import holt.processor.annotation.QueriesFor;
import holt.test.friend.model.FriendId;
import holt.test.friend.model.FriendRaw;

@QueriesFor(FriendsDB.class)
public class FriendsDBQuerier {

    public FriendRaw getById(FriendId friendId) {
        return new FriendRaw(friendId, "Smurf", "Smurfsson");
    }

}
