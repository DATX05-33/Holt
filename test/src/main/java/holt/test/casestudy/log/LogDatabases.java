package holt.test.casestudy.log;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.AddUserToUserDBAULimitLog;
import holt.processor.generation.casestudy.AddUserToUserDBAULimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.CompanyToMarketingBlastMLimitLog;
import holt.processor.generation.casestudy.CompanyToMarketingBlastMLimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.CompanyToResetRPLimitLog;
import holt.processor.generation.casestudy.CompanyToResetRPLimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.DeleteUserToUserDBDULimitLog;
import holt.processor.generation.casestudy.DeleteUserToUserDBDULimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.MarketingBlastToMailSenderMLimitLog;
import holt.processor.generation.casestudy.MarketingBlastToMailSenderMLimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.ResetToMailSenderRPLimitLog;
import holt.processor.generation.casestudy.ResetToMailSenderRPLimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.UserDBToMarketingBlastMLimitLog;
import holt.processor.generation.casestudy.UserDBToMarketingBlastMLimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.UserDBToResetRPLimitLog;
import holt.processor.generation.casestudy.UserDBToResetRPLimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.UserToAddUserAULimitLog;
import holt.processor.generation.casestudy.UserToAddUserAULimitLogDatabaseRequirements;
import holt.processor.generation.casestudy.UserToDeleteUserDULimitLog;
import holt.processor.generation.casestudy.UserToDeleteUserDULimitLogDatabaseRequirements;

import java.util.Collection;

public class LogDatabases implements
        DeleteUserToUserDBDULimitLogDatabaseRequirements,
        UserToDeleteUserDULimitLogDatabaseRequirements,
        UserToAddUserAULimitLogDatabaseRequirements,
        CompanyToResetRPLimitLogDatabaseRequirements,
        CompanyToMarketingBlastMLimitLogDatabaseRequirements,
        ResetToMailSenderRPLimitLogDatabaseRequirements,
        MarketingBlastToMailSenderMLimitLogDatabaseRequirements,
        AddUserToUserDBAULimitLogDatabaseRequirements,
        UserDBToMarketingBlastMLimitLogDatabaseRequirements,
        UserDBToResetRPLimitLogDatabaseRequirements {

    @Activator(instantiateWithReflection = true)

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

    @Override
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

    @Override
    public void M(Collection<UserDBToMarketingBlastMLimitLog.Row> input0) {

    }
}
