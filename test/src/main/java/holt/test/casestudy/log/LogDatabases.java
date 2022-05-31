package holt.test.casestudy.log;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.*;

import java.util.Collection;

@Activator(instantiateWithReflection = true)
public class LogDatabases implements
        DeleteUserToUserDBDULimitLogDatabaseRequirements,
        UserToDeleteUserDULimitLogDatabaseRequirements,
        UserToAddUserAULimitLogDatabaseRequirements,
        CompanyToResetRPLimitLogDatabaseRequirements,
        CompanyToMarketingBlastMLimitLogDatabaseRequirements,
        ResetToMailSenderRPLimitLogDatabaseRequirements,
        MarketingBlastToMailSenderMLimitLogDatabaseRequirements,
        AddUserToUserDBAULimitLogDatabaseRequirements,
        UserDBToResetRPLimitLogDatabaseRequirements {

    @Override
    public void AU(AddUserToUserDBAULimitLog.Row logRow) {
        System.out.println(logRow);
    }

    @Override
    public void M(CompanyToMarketingBlastMLimitLog.Row logRow) {
        System.out.println(logRow);
    }

    @Override
    public void RP(CompanyToResetRPLimitLog.Row logRow) {
        System.out.println(logRow);
    }

    @Override
    public void DU(DeleteUserToUserDBDULimitLog.Row logRow) {
        System.out.println(logRow);
    }

    @Override
    public void RP(ResetToMailSenderRPLimitLog.Row logRow) {
        System.out.println(logRow);
    }

    @Override
    public void AU(UserToAddUserAULimitLog.Row logRow) {
        System.out.println(logRow);
    }

    public void M(Collection<MarketingBlastToMailSenderMLimitLog.Row> logRow) {
        System.out.println(logRow);
    }

    @Override
    public void DU(UserToDeleteUserDULimitLog.Row logRow) {
        System.out.println(logRow);
    }


    @Override
    public void RP(UserDBToResetRPLimitLog.Row logRow) {
        System.out.println(logRow);
    }
}
