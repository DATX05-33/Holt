package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.annotation.QueryDefinition;
import holt.processor.generation.friend.FriendProcessRequirements;
import holt.processor.generation.friend.FriendsDBToFriendProcessFormatFriendQuery;
import holt.processor.generation.friend.SpecialDBToFriendProcessAddFriendQuery;
import holt.test.friend.model.*;

import static holt.test.friend.Main.AF;
import static holt.test.friend.Main.GF;

@FlowThrough(
        traverse = AF,
        outputType = NewFriend.class,
        functionName = "addFriend",
        overrideQueries = {
                @QueryDefinition(
                        db = SpecialDB.class,
                        process = Wow.class,
                        type = Object.class
                )
        }
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
    public SpecialDBToFriendProcessAddFriendQuery querySpecialDBAddFriend(Name input0) {
        return db -> null;
    }

    @Override
    public Friend formatFriend(FriendId id, FriendRaw friendRaw) {
        return new Friend(friendRaw.firstName() + "; " + friendRaw.lastName()) ;
    }

    @Override
    public FriendsDBToFriendProcessFormatFriendQuery queryFriendsDBFormatFriend(FriendId input0) {
        return db -> null;
    }

}
