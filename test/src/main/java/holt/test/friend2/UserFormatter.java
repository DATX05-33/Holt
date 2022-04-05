package holt.test.friend2;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend2.UserFormatterRequirements;

@Activator
public class UserFormatter implements UserFormatterRequirements {
    
    @Override
    public Object storeTraverse(Object input0) {
        return input0;
    }
}
