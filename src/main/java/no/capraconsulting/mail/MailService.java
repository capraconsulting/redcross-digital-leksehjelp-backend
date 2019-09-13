package no.capraconsulting.mail;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import java.util.concurrent.*;

//MAKE THREAD!!!!!
public class MailService implements Callable<Response>{

    private static Logger LOG = LoggerFactory.getLogger(MailService.class);
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;
    private static final boolean SSL_FLAG = true;
    private static final String ADDRESS = "";
    private static final String PASSWORD = "";

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

        String fromAddress= "no-reply@rodekors.no";

        //We get these values from the database
        String toAddress =  userEmail;    //questionDTO.getEmail();
        String subject = topic;               // questionDTO.getTopic();
        String message = answer;  // questionDTO.getAnswer(); + question(?)

        try {
            HtmlEmail email = new HtmlEmail();
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
            return Response.ok("Mail successfully sent!").build();
        }catch(Exception ex){
            LOG.error("Unable to send mail");
            LOG.error(ex.getMessage());
            return Response.ok("Unable to send email").build();
        }
    }

}
