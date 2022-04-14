package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.emailBlast.AbstractCompany;
import holt.test.blast.model.EmailContent;
import holt.test.blast.privacy.CompanyToFetcherLimit;
import holt.test.blast.privacy.CompanyToFetcherRequest;
import holt.test.blast.privacy.DBToFetcherRequest;
import holt.test.blast.privacy.DBtoFetcherLimit;
import holt.test.blast.privacy.EmailDBPolicy;
import holt.test.blast.privacy.EmailFetcherReason;
import holt.test.blast.privacy.FetcherToBlasterLimit;
import holt.test.blast.privacy.FetcherToBlasterRequest;
import holt.test.blast.privacy.LogBlastEmails;
import holt.test.blast.privacy.LogBlastEmailsDB;
import holt.test.blast.privacy.LogFetchEmails;
import holt.test.blast.privacy.LogFetchEmailsDB;
import holt.test.blast.privacy.LogWantBlast;
import holt.test.blast.privacy.LogWantEmailsDB;

import static holt.test.blast.Main.EB;

@Traverse(
        flowStartType = EmailContent.class,
        name = EB,
        order = {"BlastContent", "Emails", "ToSend"}
)
@Activator
public class Company extends AbstractCompany {

    private EmailFetcher emailFetcher;
    private EmailDB emailDB;
    private EmailBlaster emailBlaster;

    public Company(EmailFetcher emailFetcher, EmailDB emailDB, EmailBlaster emailBlaster) {
        super();
        this.emailFetcher = emailFetcher;
        this.emailDB = emailDB;
        this.emailBlaster = emailBlaster;
    }


    @Override
    protected CompanyToFetcherRequest getCompanyToFetcherRequestInstance() {
        return null;
    }

    @Override
    protected CompanyToFetcherLimit getCompanyToFetcherLimitInstance() {
        return null;
    }

    @Override
    protected LogWantBlast getLogWantBlastInstance() {
        return null;
    }

    @Override
    protected LogBlastEmailsDB getLogBlastEmailsDBInstance() {
        return null;
    }

    @Override
    protected EmailDBPolicy getEmailDBPolicyInstance() {
        return null;
    }

    @Override
    protected DBToFetcherRequest getDBToFetcherRequestInstance() {
        return null;
    }

    @Override
    protected EmailDB getEmailDBInstance() {
        return null;
    }

    @Override
    protected DBtoFetcherLimit getDBtoFetcherLimitInstance() {
        return null;
    }

    @Override
    protected LogFetchEmails getLogFetchEmailsInstance() {
        return null;
    }

    @Override
    protected LogFetchEmailsDB getLogFetchEmailsDBInstance() {
        return null;
    }

    @Override
    protected EmailFetcher getEmailFetcherInstance() {
        return null;
    }

    @Override
    protected EmailFetcherReason getEmailFetcherReasonInstance() {
        return null;
    }

    @Override
    protected FetcherToBlasterRequest getFetcherToBlasterRequestInstance() {
        return null;
    }

    @Override
    protected FetcherToBlasterLimit getFetcherToBlasterLimitInstance() {
        return null;
    }

    @Override
    protected LogBlastEmails getLogBlastEmailsInstance() {
        return null;
    }

    @Override
    protected LogWantEmailsDB getLogWantEmailsDBInstance() {
        return null;
    }

    @Override
    protected EmailBlaster getEmailBlasterInstance() {
        return null;
    }
}
