package holt.test.cli;

import holt.processor.annotation.Activator;

@Activator
public class MailSender {

    public void sendEmail(String to, String content) {
        StringBuilder email = new StringBuilder();
        email.append("---------- NEW EMAIL SENT (").append(Time.getTime()).append("h) ----------\n");
        email.append("To: ").append(to).append("\n");
        email.append(content);
        email.append("----------------------------------------");

    }

}
