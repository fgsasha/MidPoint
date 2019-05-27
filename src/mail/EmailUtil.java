/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author o.nekriach
 */
public class EmailUtil {

    private String smtpHostServer = "smtp.example.com";
    private String port = "25";
    private String starttlsEnable = "true";
    private String smtpAuth = "false";
    private String mailDebug = "false";
    private String fromAddress = "noreply@example.com";
    private String fromDisplayName = "no-reply-User";
    private String pathToAttach = "";
    private String passwordExpiration = "365";
    private String initialsNotificationInterval = "0,1,2,4,8,16,32,64";
    private String notificationInterval = "1,2,4,8,16,32,64,90,180"; //Countdown 
    private String specialUsers = "";
    private String specialOU = "ou=Services,ou=Administrators";
    private String debugEmailsToFile = "false";
    private String forceSend = "false";
    private String debugFilename = "mail_pwd.log";
    private static String passNotifTemplFile = "pwdExpEmail.template";
    private static String initEmailNotifiTemplFile = "initialEmailForWP.template";
    private static String sendInitialEmail = "false";
    private static String adminEmail = null;

    public String getForceSend() {
        return forceSend;
    }

    public String getDebugFilename() {
        return debugFilename;
    }

    public static String getPassNotifTemplFile() {
        return passNotifTemplFile;
    }

    public static String getInitEmailNotifiTemplFile() {
        return initEmailNotifiTemplFile;
    }

    public void setForceSend(String forceSend) {
        this.forceSend = forceSend;
    }

    public void setDebugFilename(String debugFilename) {
        this.debugFilename = debugFilename;
    }

    public static void setPassNotifTemplFile(String passNotifTemplFile) {
        EmailUtil.passNotifTemplFile = passNotifTemplFile;
    }

    //////////////////////////////////////////////////
    public static void setInitEmailNotifiTemplFile(String initEmailNotifiTemplFile) {
        EmailUtil.initEmailNotifiTemplFile = initEmailNotifiTemplFile;
    }

    public static void setSendInitialEmail(String sendInitialEmail) {
        EmailUtil.sendInitialEmail = sendInitialEmail;
    }

    public static String getSendInitialEmail() {
        return sendInitialEmail;
    }

    public String getPasswordExpiration() {
        return passwordExpiration;
    }

    public String getInitialsNotificationInterval() {
        return initialsNotificationInterval;
    }

    public String getNotificationInterval() {
        return notificationInterval;
    }

    public String getSpecialUsers() {
        return specialUsers;
    }

    public String getSpecialOU() {
        return specialOU;
    }

    public String getDebugEmailsToFile() {
        return debugEmailsToFile;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminEmail() {
        return this.adminEmail;
    }

    public void setPasswordExpiration(String passwordExpiration) {
        this.passwordExpiration = passwordExpiration;
    }

    public void setInitialsNotificationInterval(String initialsNotificationInterval) {
        this.initialsNotificationInterval = initialsNotificationInterval;
    }

    public void setNotificationInterval(String notificationInterval) {
        this.notificationInterval = notificationInterval;
    }

    public void setSpecialUsers(String specialUsers) {
        this.specialUsers = specialUsers;
    }

    public void setSpecialOU(String specialOU) {
        this.specialOU = specialOU;
    }

    public void setDebugEmailsToFile(String debugEmailsToFile) {
        this.debugEmailsToFile = debugEmailsToFile;
    }

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

    public void setPathToAttach(String pathToAttach) {
        this.pathToAttach = pathToAttach;
    }

    public String getPathToAttach() {
        return this.pathToAttach;
    }

    public EmailUtil() {
    }

    public Session Initialization() {
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
        FileInputStream input;
        if (fileProperties == null || fileProperties.isEmpty()) {
            input = new FileInputStream(new File("mail.properties"));
        } else {
            input = new FileInputStream(new File(fileProperties));
        }
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
        String pathToAttach = prop.getProperty("pathToAttach", "");
        this.setPathToAttach(pathToAttach);
        String passwordExpiration = prop.getProperty("passwordExpiration", "365");
        this.setPasswordExpiration(passwordExpiration);
        String initialsNotificationInterval = prop.getProperty("initialsNotificationInterval", "0,1,2,4,8,16,32,64");
        this.setInitialsNotificationInterval(initialsNotificationInterval);
        String notificationInterval = prop.getProperty("notificationInterval", "1,2,4,8,16,32,64,90,180");
        this.setNotificationInterval(notificationInterval);
        String specialUsers = prop.getProperty("specialUsers", "administrator");
        this.setSpecialUsers(specialUsers);
        String specialOU = prop.getProperty("specialOU", "ou=Services,ou=Administrators");
        this.setSpecialOU(specialOU);
        String debugEmailsToFile = prop.getProperty("debugEmailsToFile", "false");
        this.setDebugEmailsToFile(debugEmailsToFile);
        String forceSend = prop.getProperty("forceSend", "false");
        this.setForceSend(forceSend);
        String debugFilename = prop.getProperty("debugFilename", "mail_pwd.log");
        this.setDebugFilename(debugFilename);
        String passNotifTemplFile = prop.getProperty("passNotifTemplFile", "pwdExpEmail.template");
        this.setPassNotifTemplFile(passNotifTemplFile);
        String initEmailNotifiTemplFile = prop.getProperty("initEmailNotifiTemplFile", "initialEmailForWP.template");
        this.setInitEmailNotifiTemplFile(initEmailNotifiTemplFile);
        String sendInitialEmail = prop.getProperty("sendInitialEmail", "false");
        setSendInitialEmail(sendInitialEmail);
        String adminEmail = prop.getProperty("adminEmail", null);
        this.setAdminEmail(adminEmail);

        if (verbose != null && verbose.equalsIgnoreCase("true")) {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            System.out.println("#############################jsonparser.properties#################################");
            System.out.println("verbose: " + verbose);
            System.out.println("smtpHostServer: " + smtpHostServer);
            System.out.println("port: " + port);
            System.out.println("starttlsEnable: " + starttlsEnable);
            System.out.println("smtpAuth: " + smtpAuth);
            System.out.println("mailDebug: " + mailDebug);
            System.out.println("adminEmail: " + adminEmail);
            System.out.println("fromAddress: " + fromAddress);
            System.out.println("fromDisplayName: " + fromDisplayName);
            System.out.println("pathToAttach: " + pathToAttach);
            System.out.println("passwordExpiration: " + passwordExpiration);
            System.out.println("initialsNotificationInterval: " + initialsNotificationInterval);
            System.out.println("notificationInterval: " + notificationInterval);
            System.out.println("specialUsers: " + specialUsers);
            System.out.println("specialOU: " + specialOU);
            System.out.println("debugEmailsToFile: " + debugEmailsToFile);
            System.out.println("forceSend: " + forceSend);
            System.out.println("debugFilename: " + debugFilename);
            System.out.println("passNotifTemplFile: " + passNotifTemplFile);
            System.out.println("initEmailNotifiTemplFile: " + initEmailNotifiTemplFile);
            System.out.println("sendInitialEmail: " + initEmailNotifiTemplFile);
            System.out.println("Use in email templates: %UID% %DISPLAYNAME% %PWD%");
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
        msg.addHeader("Content-type", "text/html; charset=UTF-8");
        msg.addHeader("format", "flowed");
        msg.addHeader("Content-Transfer-Encoding", "8bit");
        msg.setFrom(new InternetAddress(fromAddress, fromDisplayName));
        msg.setReplyTo(InternetAddress.parse(fromAddress, false));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        msg.setSentDate(new Date());

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
//        System.out.println("Message is ready");
//        Transport.send(msg);
        System.out.println("Message is ready");
        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/html; charset=UTF-8");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        // sets the multi-part as e-mail's content
        msg.setContent(multipart);
        // sends the e-mail

        Transport.send(msg);
        System.out.println("EMail Sent Successfully!!");

    }

    /**
     * Utility method to send HTML email with attachment
     *
     * @param session
     * @param toEmail
     * @param subject
     * @param body
     */
    public void sendEmailWithAttachment(Session session, String toEmail, String subject, String body, String[] attachFiles) throws MessagingException, UnsupportedEncodingException {

        MimeMessage msg = new MimeMessage(session);
        //set message headers
        msg.addHeader("Content-type", "text/html; charset=UTF-8");
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
        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/html; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "8bit");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (String filePath : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();

                try {
                    attachPart.attachFile(filePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                multipart.addBodyPart(attachPart);
            }
        }

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        // sends the e-mail
        Transport.send(msg);

        System.out.println("EMail Sent Successfully!!");
    }

}
