package holt.test.friend;

import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.IFriendProcess;
import holt.processor.generation.IFriendsDBToFriendProcessformatFriendGetFriendQuery;

@FlowThrough(
        flow = "GF",
        outputType = Friend.class,
        functionName = "formatFriendGetFriend",
        queries = {
                @Query(
                        db = FriendProcess.lmao,
                        type = FriendRaw.class
                )
        }
)
public class FriendProcess implements IFriendProcess {

    public static final String lmao = "FriendDB";

    @Override
    public IFriendsDBToFriendProcessformatFriendGetFriendQuery query_FriendsDB_formatFriendGetFriend(FriendID input) {
        return null;
    }

    @Override
    public Object formatFriendGetFriend(FriendID input0, Object dbInput1) {
        return null;
    }
}
