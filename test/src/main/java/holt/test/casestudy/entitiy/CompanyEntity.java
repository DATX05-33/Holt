package holt.test.casestudy.entitiy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Output;
import holt.processor.annotation.Traverse;
import holt.processor.generation.casestudy.AbstractCompany;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailContent;

@Traverse(
        name = "M", //Marketing
        startTypes = {@Output(type = EmailContent.class)},
        order = {"email_content", "get_marketing_emails", "blast_marketing"}
)
@Traverse(
        name = "RP", //Reset Password
        startTypes = {@Output(type = Email.class)},
        order = {"reset_pwd", "get_reset_email", "send_otp"}
)
@Activator
public class CompanyEntity extends AbstractCompany {

    public CompanyEntity(UserDB userDB, MailSenderEntity mailSenderEntity) {
        super(userDB, mailSenderEntity);
    }

    public void sendMarketing(String content) {
        super.M(new EmailContent(content));
    }

    public void resetPassword(String email) {
        super.RP(new Email(email));
    }

}
