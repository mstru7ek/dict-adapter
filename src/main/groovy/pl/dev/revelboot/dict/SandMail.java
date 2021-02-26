package pl.dev.revelboot.dict;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SandMail {


  public static void message(String mailTo, String subject, String text) throws MessagingException {

    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");

    Session session = Session.getInstance(props, authenticator("michal.struzek", "qthpfyckpkgmsmoq"));

    MimeMessage message = new MimeMessage(session);
    message.setFrom("michal.struzek@gmail.com");
    message.addRecipients(Message.RecipientType.TO, mailTo);
    message.setSubject(subject);
    message.setText(text);
    Transport.send(message);

  }

  private static Authenticator authenticator(final String username, final String password) {
    return new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    };
  }
}
