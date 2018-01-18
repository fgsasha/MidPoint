/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;
//https://javaee.github.io/javamail/

/**
 *
 * @author onekriach
 */
import hrdata.JSONparser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtil {

    private String smtpHostServer = "smtp.example.com";
    private String port = "25";
    private String starttlsEnable = "true";
    private String smtpAuth = "false";
    private String mailDebug = "false";
    private String fromAddress = "noreply@example.com";
    private String fromDisplayName = "no-reply-User";

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setFromDisplayName(String fromDisplayName) {
        this.fromDisplayName = fromDisplayName;
    }

    public void setSmtpHostServer(String smtpHostServer) {
        this.smtpHostServer = smtpHostServer;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setStarttlsEnable(String starttlsEnable) {
        this.starttlsEnable = starttlsEnable;
    }

    public void setSmtpAuth(String smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public void setMailDebug(String mailDebug) {
        this.mailDebug = mailDebug;
    }

    public EmailUtil() {
    }

    public Session initialization() {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpHostServer);
        props.put("mail.smtp.port", port); //TLS Port
        props.put("mail.smtp.starttls.enable", starttlsEnable); //enable STARTTLS
        props.put("mail.smtp.auth", smtpAuth); //enable authentication
        props.put("mail.debug", mailDebug);
        return Session.getInstance(props, null);

    }

    public EmailUtil(String fileProperties) throws FileNotFoundException, IOException {
        System.out.println("----------------------------------------------------------------");
        String inputParameter = null;
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream(new File("mail.properties"));

        // load a properties file
        prop.load(new InputStreamReader(input, Charset.forName("UTF-8")));

        // get the property value and print it out
        String verbose = prop.getProperty("verbose", "true");
        String smtpHostServer = prop.getProperty("smtpHostServer", "smtp.example.com");
        this.setSmtpHostServer(smtpHostServer);
        String port = prop.getProperty("port", "25");
        this.setPort(port);
        String starttlsEnable = prop.getProperty("starttlsEnable", "true");
        this.setStarttlsEnable(starttlsEnable);
        String smtpAuth = prop.getProperty("smtpAuth", "false");
        this.setSmtpAuth(smtpAuth);
        String mailDebug = prop.getProperty("mailDebug", "false");
        this.setMailDebug(mailDebug);
        String fromAddress = prop.getProperty("fromAddress", "noreply@example.com");
        this.setFromAddress(fromAddress);
        String fromDisplayName = prop.getProperty("fromDisplayName", "no-reply-User");
        this.setFromDisplayName(fromDisplayName);
        
        if (verbose != null && verbose.equalsIgnoreCase("true")) {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            System.out.println("#############################jsonparser.properties#################################");
            System.out.println("verbose: " + verbose);
            System.out.println("port: " + port);
            System.out.println("starttlsEnable: " + starttlsEnable);
            System.out.println("smtpAuth: " + smtpAuth);
            System.out.println("mailDebug: " + mailDebug);
            System.out.println("fromAddress: " + fromAddress);
            System.out.println("fromDisplayName: " + fromDisplayName);            
            System.out.println("#############################EOF#################################");
        }

    }

    /**
     * Utility method to send simple HTML email
     *
     * @param session
     * @param toEmail
     * @param subject
     * @param body
     */
    public void sendEmail(Session session, String toEmail, String subject, String body) throws MessagingException, UnsupportedEncodingException {

        MimeMessage msg = new MimeMessage(session);
        //set message headers
        msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        msg.addHeader("format", "flowed");
        msg.addHeader("Content-Transfer-Encoding", "8bit");
//
        msg.setFrom(new InternetAddress(fromAddress, fromDisplayName));

        msg.setReplyTo(InternetAddress.parse(fromAddress, false));

        msg.setSubject(subject, "UTF-8");

        msg.setText(body, "UTF-8");

        msg.setSentDate(new Date());

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        System.out.println("Message is ready");
        Transport.send(msg);

        System.out.println("EMail Sent Successfully!!");
    }

}
