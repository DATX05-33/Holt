package holt.test.casestudy.log;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.UserDBToMarketingBlastMLimitLog;
import holt.processor.generation.casestudy.UserDBToMarketingBlastMLimitLogDatabaseRequirements;

import java.util.Collection;

@Activator(instantiateWithReflection = true)
public class LogDatabase2 implements UserDBToMarketingBlastMLimitLogDatabaseRequirements {
    @Override
    public void M(Collection<UserDBToMarketingBlastMLimitLog.Row> input0) {
        System.out.println(input0);
    }
}
