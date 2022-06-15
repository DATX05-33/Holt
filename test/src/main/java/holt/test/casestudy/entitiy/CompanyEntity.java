package holt.test.casestudy.entitiy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Output;
import holt.processor.annotation.Traverse;
import holt.processor.generation.casestudy.AbstractCompany;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.db.UserPolicyDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.MarketingType;


@Traverse(
        name = "M", //Marketing
        startTypes = {@Output(type = EmailContent.class), @Output(type = MarketingType.class)},
        order = {"email_content", "get_marketing_emails", "blast_marketing"}
)
@Traverse(
        name = "RP", //Reset Password
        startTypes = {@Output(type = Email.class), @Output(type = AccessUserReason.class)},
        order = {"reset_pwd", "get_reset_email", "password", "send_otp"}
)
@Activator
public class CompanyEntity extends AbstractCompany {

    public CompanyEntity(UserPolicyDB userPolicyDB, UserDB userDB, MailSenderEntity mailSenderEntity) {
        super(userPolicyDB, userDB, mailSenderEntity);
    }

    public void sendMarketing(String content) {
        super.M(new EmailContent(content), MarketingType.PRODUCT_MARKETING);
    }

    public void resetPassword(String email) {
        super.RP(new Email(email), AccessUserReason.RESET_PASSWORD);
    }

}
