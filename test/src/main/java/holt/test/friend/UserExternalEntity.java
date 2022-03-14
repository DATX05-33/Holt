package holt.test.friend;

import holt.processor.annotation.FlowStart;
import holt.processor.generation.friend.AbstractUser;
import holt.test.friend.model.FriendId;
import holt.test.friend.model.Name;

import static holt.test.friend.Main.AF;
import static holt.test.friend.Main.GF;

@FlowStart(
        flow = GF,
        flowStartType = FriendId.class
)
@FlowStart(
        flow = AF,
        flowStartType = Name.class
)
public class UserExternalEntity extends AbstractUser {

}
