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
        order = {"AF_U->FP", "AF_FP->FDB"})
@Traverse(
        name = GF,
        flowStartType = FriendId.class,
        order = {"GF_U->FP", "GF_FDB->FP", "GF_FP->U"})
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
    protected FriendsDB getFriendsDBInstance() {
        return new FriendsDB();
    }
}
