package holt.test.casestudy.entitiy;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.AbstractMailSender;
import holt.test.casestudy.model.EmailAndContent;

import java.util.Collection;

@Activator
public class MailSenderEntity extends AbstractMailSender {

    private void sendEmail(EmailAndContent emailAndContent) {
        StringBuilder email = new StringBuilder();
        email.append("---------- NEW EMAIL SENT ----------\n")
                .append("To: ").append(emailAndContent.email().email()).append("\n")
                .append(emailAndContent.content().content())
                .append("\n----------------------------------------");


        System.out.println(email);
    }

    @Override
    public void M(Collection<EmailAndContent> input) {
        input.forEach(this::sendEmail);
    }

    @Override
    public void RP(EmailAndContent input) {
        sendEmail(input);
    }
}
