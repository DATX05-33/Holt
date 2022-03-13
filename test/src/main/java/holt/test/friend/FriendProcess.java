package holt.test.friend;

import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.friend.IFriendProcess;
import holt.processor.generation.friend.IFriendsDBToFriendProcessformatFriendQuery;
import holt.test.friend.model.Friend;
import holt.test.friend.model.FriendId;
import holt.test.friend.model.FriendRaw;
import holt.test.friend.model.Name;
import holt.test.friend.model.NewFriend;

import static holt.test.friend.Main.AF;
import static holt.test.friend.Main.GF;

@FlowThrough(
        flow = AF,
        outputType = NewFriend.class,
        functionName = "addFriend"
)
@FlowThrough(
        flow = GF,
        outputType = Friend.class,
        functionName = "formatFriend",
        queries = {
                @Query(
                        db = FriendsDB.class,
                        type = FriendRaw.class
                )
        }
)
public class FriendProcess implements IFriendProcess {

    @Override
    public NewFriend addFriend(Name input0) {
        return null;
    }

    @Override
    public IFriendsDBToFriendProcessformatFriendQuery query_FriendsDB_formatFriend(FriendId input0) {
        return null;
    }

    @Override
    public Friend formatFriend(FriendId input0, FriendRaw dbInput1) {
        return null;
    }
}
