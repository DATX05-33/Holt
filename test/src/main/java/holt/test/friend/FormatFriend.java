package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.annotation.PADFD;
import holt.processor.generation.interfaces.IFormatFriend;
import holt.processor.generation.interfaces.IFriendsDBToFormatFriendQuery;

@Activator(
        methodName = "formatFriend",
        outputType = Friend.class
)
@PADFD(name = "friend", file = "friend-padfd.csv")
public class FormatFriend {

    // first
    // generate IFormatFriend and IFriendDBToFormatFriendQuery
    //      IFormatFriend har formatFriend som är givet av Activator
    //          och FriendDBQueryToFormatFriendQuery som är genererad pga pilen från FriendDB
    /*public FriendDBQueryToFormatFriendQuery getFriendDBQuery(FriendID id){
        return db -> {
            return db.get(id);
        };
    }

    // then
    @Query(FriendRaw.class)
    public FriendDBQueryToFormatFriendQuery getFriendDBQuery(FriendID id){
        return db -> {
            return db.get(id);
        };
    }

    public Friend formatFriend(FriendID id, FriendRaw raw){
        return new Friend(raw.toString());
    }*/
}
