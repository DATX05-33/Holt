package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.cli.AbstractCompany;
import holt.test.cli.model.Email;
import holt.test.cli.model.EmailAndContent;
import holt.test.cli.model.EmailContent;

@Traverse(
        name = "marketing",
        flowStartType = EmailAndContent.class,
        order = {"emailContent", "getMarketingEmails", "blastMarketing"}
)
@Traverse(
        name = "resetPassword",
        flowStartType = Email.class,
        order = {"resetPwd", "getResetEmails", "sendOTP"}
)
@Activator
public class Company extends AbstractCompany {

    public void sendMarketing(String email, String content) {
        super.marketing(new EmailAndContent(new Email(email), new EmailContent(content)));
    }

    public void resetPassword(String email) {
        super.resetPassword(new Email(email));
    }


    @Override
    protected UserDB getUserDBInstance() {
        return Cli.getUserDB();
    }

    @Override
    protected MarketingBlast getMarketingBlastInstance() {
        return new MarketingBlast();
    }

    @Override
    protected MailSender getMailSenderInstance() {
        return new MailSender();
    }

    @Override
    protected Reset getResetInstance() {
        return new Reset();
    }
}
