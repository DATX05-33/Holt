package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.generation.cli.AbstractMailSender;
import holt.test.cli.model.EmailAndContent;

@Activator
public class MailSender extends AbstractMailSender {

    public void sendEmail(EmailAndContent emailAndContent) {
        StringBuilder email = new StringBuilder();
        email.append("---------- NEW EMAIL SENT (").append(Time.getTime()).append("h) ----------\n");
        email.append("To: ").append(emailAndContent.email().email()).append("\n");
        email.append(emailAndContent.content().content());
        email.append("\n----------------------------------------");


        System.out.println(email.toString());
    }

    @Override
    public void marketing(EmailAndContent v) {
        sendEmail(v);
    }

    @Override
    public void resetPassword(EmailAndContent v) {
        sendEmail(v);
    }
}
