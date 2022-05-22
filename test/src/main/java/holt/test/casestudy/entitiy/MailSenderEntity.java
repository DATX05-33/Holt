package holt.test.casestudy.entitiy;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.AbstractMailSender;
import holt.test.casestudy.Time;
import holt.test.casestudy.model.EmailAndContent;

import java.util.Collection;

@Activator
public class MailSenderEntity extends AbstractMailSender {

    private void sendEmail(EmailAndContent emailAndContent) {
        StringBuilder email = new StringBuilder();
        email.append("---------- NEW EMAIL SENT (Time: ").append(Time.getTime()).append("h) ----------\n");
        email.append("To: ").append(emailAndContent.email().email()).append("\n");
        email.append(emailAndContent.content().content());
        email.append("\n----------------------------------------");


        System.out.println(email);
    }


    @Override
    public void marketing(Collection<EmailAndContent> toSend) {
        toSend.forEach(this::sendEmail);
    }

    @Override
    public void resetPassword(EmailAndContent v) {
        sendEmail(v);
    }
}
