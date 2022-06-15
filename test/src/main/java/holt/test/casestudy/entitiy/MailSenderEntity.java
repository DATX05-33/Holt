package holt.test.casestudy.entitiy;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.AbstractMailSender;
import holt.processor.generation.casestudy.MailSenderMCombiner;
import holt.processor.generation.casestudy.MailSenderRPCombiner;
import holt.test.casestudy.Time;
import holt.test.casestudy.model.EmailAndContent;

import java.util.Collection;

@Activator
public class MailSenderEntity extends AbstractMailSender {

    private void sendEmail(EmailAndContent emailAndContent) {
        StringBuilder email = new StringBuilder();
        email.append("---------- NEW EMAIL SENT (Time: ").append(Time.getTime()).append("h) ----------\n")
                .append("To: ").append(emailAndContent.email().email()).append("\n")
                .append(emailAndContent.content().content())
                .append("\n----------------------------------------");


        System.out.println(email);
    }

    @Override
    public void M(MailSenderMCombiner.Combo input) {
        input.v0.forEach(this::sendEmail);
    }

    @Override
    public void RP(MailSenderRPCombiner.Combo input) {
        sendEmail(input.v0);
    }
}
