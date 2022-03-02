package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.generation.interfaces.AbstractExternalEntity;

@Activator(
        methodName = "userMethod",
        outputType = FriendID.class
)
public class User extends AbstractExternalEntity {
}
