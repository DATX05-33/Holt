package holt.test.friend.withannotations;

import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.friend_annotations.IFriendProcess;
import holt.processor.generation.friend_annotations.IFriendsDBToFriendProcessformatFriendQuery;
import holt.test.friend.withannotations.model.Friend;
import holt.test.friend.withannotations.model.FriendID;
import holt.test.friend.withannotations.model.FriendRaw;
import holt.test.friend.withannotations.model.Name;
import holt.test.friend.withannotations.model.NewFriend;

import static holt.test.friend.withannotations.Main.AF;
import static holt.test.friend.withannotations.Main.GF;

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
    public IFriendsDBToFriendProcessformatFriendQuery query_FriendsDB_formatFriend(FriendID input0) {
        return null;
    }

    @Override
    public Friend formatFriend(FriendID input0, FriendRaw dbInput1) {
        return null;
    }
}
