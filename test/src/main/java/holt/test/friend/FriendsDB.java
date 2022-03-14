package holt.test.friend;

import holt.processor.annotation.Database;
import holt.processor.generation.friend.IFriendsDB;
import holt.test.friend.model.FriendId;
import holt.test.friend.model.FriendRaw;
import holt.test.friend.model.NewFriend;

@Database
public class FriendsDB implements IFriendsDB {

    public FriendRaw getById(FriendId friendId) {
        return new FriendRaw(friendId, "Smurf", "Smurfsson");
    }

    @Override
    public void AF(NewFriend input) {
        System.out.println("Saving..." + input);
    }
}
