package no.capraconsulting.mail;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import no.capraconsulting.config.PropertiesHelper;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import java.util.concurrent.*;

//MAKE THREAD!!!!!
public class MailService implements Callable<Response>{

    private static Logger LOG = LoggerFactory.getLogger(MailService.class);
    private static final String HOST = "smtp.sendgrid.net";
    private static final int PORT = 587;
    private static final boolean SSL_FLAG = true;
    private static final String ADDRESS = PropertiesHelper.getStringProperty(PropertiesHelper.getProperties(), PropertiesHelper.MAIL_USERNAME, null);
    private static final String PASSWORD = PropertiesHelper.getStringProperty(PropertiesHelper.getProperties(), PropertiesHelper.MAIL_PASSWORD, null);

    private String userEmail;
    private String topic;
    private String answer;

    public MailService(String userEmail, String topic, String answer){
        this.userEmail = userEmail;
        this.topic = topic;
        this.answer = answer;
    }

    @Override
    public Response call() {
        String userName = ADDRESS;
        String password = PASSWORD;
        String apiKey = PropertiesHelper.getStringProperty(PropertiesHelper.getProperties(), PropertiesHelper.MAIL_APIKEY, null);

        String fromAddress= "no-reply@rodekors.no";

        //We get these values from the database
        Email toAddress = new Email(userEmail);    //questionDTO.getEmail();
        String subject = topic;               // questionDTO.getTopic();
        String message = answer;  // questionDTO.getAnswer(); + question(?)

        try {
            Email email = new Email(ADDRESS, fromAddress);
            Content content = new Content("text/html", message);
            Mail mail = new Mail(email, subject, toAddress, content);
            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);

            return Response.ok("Mail successfully sent!").build();
            /*HtmlEmail email = new HtmlEmail();
            email.setHostName(HOST);
            email.setSmtpPort(PORT);
            email.setAuthenticator(new DefaultAuthenticator(userName, password));
            email.setSSLOnConnect(SSL_FLAG);
            email.setFrom(userName, fromAddress);
            email.setSubject(subject);
            email.setHtmlMsg(message);
            email.setTextMsg("Your email client does not support HTML");
            email.addTo(toAddress);
            email.send();
            return Response.ok("Mail successfully sent!").build();*/
        }catch(Exception ex){
            LOG.error("Unable to send mail");
            LOG.error(ex.getMessage());
            return Response.ok("Unable to send email").build();
        }
    }

}
