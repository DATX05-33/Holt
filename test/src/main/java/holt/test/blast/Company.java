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
    protected CompanyToFetcherRequest getEmailFetcherRequestBlastContentInstance() {
        return null;
    }

    @Override
    protected CompanyToFetcherLimit getEmailFetcherLimitBlastContentInstance() {
        return null;
    }

    @Override
    protected LogWantBlast getEmailFetcherLimitBlastContentLogBlastContentInstance() {
        return null;
    }

    @Override
    protected LogBlastEmailsDB getEmailFetcherLimitBlastContentLogBlastContentDatabaseBlastContentInstance() {
        return null;
    }

    @Override
    protected EmailDB getEmailDBInstance() {
        return null;
    }

    @Override
    protected EmailFetcherTempQuerier getEmailFetcherTempQuerierInstance() {
        return null;
    }

    @Override
    protected EmailDBPolicy getEmailDBPolicyInstance() {
        return null;
    }

    @Override
    protected DBToFetcherRequest getEmailFetcherRequestEmailsInstance() {
        return null;
    }

    @Override
    protected DBtoFetcherLimit getEmailFetcherLimitEmailsInstance() {
        return null;
    }

    @Override
    protected LogFetchEmails getEmailFetcherLimitEmailsLogEmailsInstance() {
        return null;
    }

    @Override
    protected LogFetchEmailsDB getEmailFetcherLimitEmailsLogEmailsDatabaseEmailsInstance() {
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
    protected FetcherToBlasterRequest getEmailBlasterRequestToSendInstance() {
        return null;
    }

    @Override
    protected FetcherToBlasterLimit getEmailBlasterLimitToSendInstance() {
        return null;
    }

    @Override
    protected LogBlastEmails getEmailBlasterLimitToSendLogToSendInstance() {
        return null;
    }

    @Override
    protected LogWantEmailsDB getEmailBlasterLimitToSendLogToSendDatabaseToSendInstance() {
        return null;
    }

    @Override
    protected EmailBlaster getEmailBlasterInstance() {
        return null;
    }
}
