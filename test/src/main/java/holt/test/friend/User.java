package holt.test.friend;

import holt.processor.annotation.Processor;
import holt.processor.generation.interfaces.AbstractExternalEntity;

@Processor(
        methodName = "userMethod",
        outputType = FriendID.class
)
public class User extends AbstractExternalEntity {
}
