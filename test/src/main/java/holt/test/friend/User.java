package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.friend.AbstractUser;
import holt.test.friend.model.Friend;
import holt.test.friend.model.FriendId;
import holt.test.friend.model.Name;

import static holt.test.friend.Main.AF;
import static holt.test.friend.Main.GF;

@Traverse(
        name = AF,
        flowStartType = Name.class,
        order = {"AFwow1", "AFwow3", "wow1", "wow5", "wow2", "AFwow2"})
@Traverse(
        name = GF,
        flowStartType = FriendId.class,
        order = {"GFwow1", "GFwow2", "GFwow3"})
@Activator
public class User extends AbstractUser {

    public void addFriend(Name name) {
        super.AF(name);
    }

    public Friend getFriend(FriendId id) {
        return super.GF(id);
    }

    @Override
    protected FriendProcess getFriendProcessInstance() {
        return new FriendProcess();
    }

    @Override
    protected Wow getWowInstance() {
        return new Wow();
    }

    @Override
    protected SpecialDB getSpecialDBInstance() {
        return new SpecialDB();
    }

    @Override
    protected WowEntity getWowEntityInstance() {
        return new WowEntity();
    }

    @Override
    protected FriendsDB getFriendsDBInstance() {
        return new FriendsDB();
    }
}
