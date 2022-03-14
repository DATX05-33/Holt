package holt.test.friend;

import holt.processor.annotation.Database;
import holt.processor.generation.friend.IFriendsDB;
import holt.test.friend.model.NewFriend;

@Database
public class FriendsDB implements IFriendsDB {
    @Override
    public void AF(NewFriend input) {

    }
}
