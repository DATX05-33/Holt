package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.emailBlast.AbstractCompany;
import holt.test.blast.model.EmailContent;
import holt.test.blast.privacy.*;

import static holt.test.blast.Main.emailBlast;

@Traverse(
        flowStartType = EmailContent.class,
        name = emailBlast,
        order = {"wantBlast", "fetchEmails", "blastEmails"}
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
    protected CompanyToFetcherRequest getEmailFetcherRequestwantBlastInstance() {
        return new CompanyToFetcherRequest();
    }

    @Override
    protected CompanyToFetcherLimit getEmailFetcherLimitwantBlastInstance() {
        return new CompanyToFetcherLimit();
    }

    @Override
    protected LogWantBlast getEmailFetcherLimitwantBlastLogwantBlastInstance() {
        return new LogWantBlast();
    }

    @Override
    protected LogBlastEmailsDB getEmailFetcherLimitwantBlastLogwantBlastDatabasewantBlastInstance() {
        return new LogBlastEmailsDB();
    }

    @Override
    protected EmailDBPolicy getEmailDBPolicyInstance() {
        return new EmailDBPolicy();
    }

    @Override
    protected DBToFetcherRequest getEmailFetcherRequestfetchEmailsInstance() {
        return new DBToFetcherRequest();
    }

    @Override
    protected EmailDB getEmailDBInstance() {
        return this.emailDB;
    }

    @Override
    protected DBtoFetcherLimit getEmailFetcherLimitfetchEmailsInstance() {
        return new DBtoFetcherLimit();
    }

    @Override
    protected LogFetchEmails getEmailFetcherLimitfetchEmailsLogfetchEmailsInstance() {
        return new LogFetchEmails();
    }

    @Override
    protected LogFetchEmailsDB getEmailFetcherLimitfetchEmailsLogfetchEmailsDatabasefetchEmailsInstance() {
        return new LogFetchEmailsDB();
    }

    @Override
    protected EmailFetcher getEmailFetcherInstance() {
        return this.emailFetcher;
    }

    @Override
    protected EmailFetcherReason getEmailFetcherReasonInstance() {
        return new EmailFetcherReason();
    }

    @Override
    protected FetcherToBlasterRequest getEmailBlasterRequestblastEmailsInstance() {
        return new FetcherToBlasterRequest();
    }

    @Override
    protected FetcherToBlasterLimit getEmailBlasterLimitblastEmailsInstance() {
        return null;
    }

    @Override
    protected LogBlastEmails getEmailBlasterLimitblastEmailsLogblastEmailsInstance() {
        return new LogBlastEmails();
    }

    @Override
    protected LogWantEmailsDB getEmailBlasterLimitblastEmailsLogblastEmailsDatabaseblastEmailsInstance() {
        return new LogWantEmailsDB();
    }

    @Override
    protected EmailBlaster getEmailBlasterInstance() {
        return this.emailBlaster;
    }
}
