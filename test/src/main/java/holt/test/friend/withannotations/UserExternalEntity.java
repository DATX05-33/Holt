package holt.test.friend.withannotations;

import holt.processor.annotation.FlowStart;
import holt.processor.generation.friend_annotations.AbstractUser;
import holt.test.friend.withannotations.model.FriendID;
import holt.test.friend.withannotations.model.Name;

import static holt.test.friend.withannotations.Main.AF;
import static holt.test.friend.withannotations.Main.GF;

@FlowStart(
        flow = GF,
        flowStartType = FriendID.class
)
@FlowStart(
        flow = AF,
        flowStartType = Name.class
)
public class UserExternalEntity extends AbstractUser {

}
