package holt.test.friend2.friend3;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.friend3.AbstractUser3;

@Traverse(
        name = "AF",
        order = {"_AF_U-=FP", "_AF_FP-=FDB"}
)
@Traverse(
        name = "GF",
        order = {"_GF_U-=FP", "_GF_FDB-=FP", "_GF_FP-=U"}
)
@Activator
public class User3 extends AbstractUser3 {

    @Override
    protected FriendProcess3Request getFriendProcess3RequestInstance() {
        return null;
    }

    @Override
    protected FriendProcess3Limit getFriendProcess3LimitInstance() {
        return null;
    }

    @Override
    protected FriendProcess3LimitLog getFriendProcess3LimitLogInstance() {
        return null;
    }

    @Override
    protected FriendProcess3 getFriendProcess3Instance() {
        return null;
    }

    @Override
    protected FriendProcess3Reason getFriendProcess3ReasonInstance() {
        return null;
    }

    @Override
    protected FriendsDB3Request getFriendsDB3RequestInstance() {
        return null;
    }

    @Override
    protected FriendsDB3Limit getFriendsDB3LimitInstance() {
        return null;
    }

    @Override
    protected FriendsDB3LimitLog getFriendsDB3LimitLogInstance() {
        return null;
    }

    @Override
    protected FriendProcess3LimitLogDatabase getFriendProcess3LimitLogDatabaseInstance() {
        return null;
    }

    @Override
    protected FriendsDB3LimitLogDatabase getFriendsDB3LimitLogDatabaseInstance() {
        return null;
    }

    @Override
    protected FriendsDB3 getFriendsDB3Instance() {
        return null;
    }

    @Override
    protected FriendsDB3Policy getFriendsDB3PolicyInstance() {
        return null;
    }

    @Override
    protected User3Request getUser3RequestInstance() {
        return null;
    }

    @Override
    protected User3Limit getUser3LimitInstance() {
        return null;
    }

    @Override
    protected User3LimitLog getUser3LimitLogInstance() {
        return null;
    }

    @Override
    protected User3LimitLogDatabase getUser3LimitLogDatabaseInstance() {
        return null;
    }
}
