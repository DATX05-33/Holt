package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend.FriendsDBRequirements;
import holt.test.friend.model.NewFriend;

@Activator
public class FriendsDB implements FriendsDBRequirements {

    private final FriendsDBQuerier friendsDBQuerier;

    public FriendsDB() {
        this.friendsDBQuerier = new FriendsDBQuerier();
    }

    @Override
    public void AF(NewFriend input) {
        // This output is used for testing...
        System.out.println("Saving..." + input);
    }

    @Override
    public FriendsDBQuerier getQuerierInstance() {
        return this.friendsDBQuerier;
    }

}
