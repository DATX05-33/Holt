package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.friend.FriendProcessRequirements;
import holt.processor.generation.friend.FriendsDBToFriendProcessFormatFriendQuery;
import holt.test.friend.model.*;

import static holt.test.friend.Main.AF;
import static holt.test.friend.Main.GF;

@FlowThrough(
        traverse = AF,
        outputType = NewFriend.class,
        functionName = "addFriend"
)
@FlowThrough(
        traverse = GF,
        outputType = Friend.class,
        functionName = "formatFriend",
        queries = {
                @Query(
                        db = FriendsDB.class,
                        type = FriendRaw.class
                )
        }
)
@Activator
public class FriendProcess implements FriendProcessRequirements {

    @Override
    public NewFriend addFriend(Name name) {
        return new NewFriend(name.name());
    }

    @Override
    public FriendsDBToFriendProcessFormatFriendQuery queryFriendsDBFormatFriend(FriendId id) {
        return db -> db.getById(id);
    }

    @Override
    public Friend formatFriend(FriendId id, FriendRaw friendRaw) {
        return new Friend(friendRaw.firstName() + "; " + friendRaw.lastName()) ;
    }
}
