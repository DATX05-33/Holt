package holt.test.friend;

import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.IFriendProcess;
import holt.processor.generation.IFriendsDB;
import holt.processor.generation.IFriendsDBToFriendProcessformatFriendGetFriendQuery;

@FlowThrough(
        flow = "GF",
        outputType = Friend.class,
        functionName = "formatFriendGetFriend",
        queries = {
                @Query(
                        db = IFriendsDB.class,
                        type = FriendRaw.class
                )
        }
)
public class FriendProcess implements IFriendProcess {

    @Override
    public IFriendsDBToFriendProcessformatFriendGetFriendQuery query_FriendsDB_formatFriendGetFriend(FriendID input) {
        return null;
    }

    @Override
    public Friend formatFriendGetFriend(FriendID input0, FriendRaw dbInput1) {
        return null;
    }

}
