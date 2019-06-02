package server;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailHandler {

    //На самом деле нужно отправлять письма через хелиос, но и так пойдет
    final private static String user = "YourGmail@gmail.com";
    final private static String password = "yourPassword";

    private static final String HOST="smtp.gmail.com";
    private static final String PORT="587"; //gmail port

    public static synchronized void sendEmail(InternetAddress recipient,String subject, String text){

        Properties prop = new Properties();
        prop.put("mail.smtp.host", HOST);
        prop.put("mail.smtp.port", PORT);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setRecipient(Message.RecipientType.TO, recipient);
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
