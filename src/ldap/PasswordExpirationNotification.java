/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import mail.EmailUtil;

/**
 *
 * @author onekriach
 */
public class PasswordExpirationNotification {

    private String adminEmail = "admin@example.com";
    private final String SPECOU = "SPECOU";
    private final String SPECUSER = "SPECUSER";
    private final String INITIALPASS = "INITIALPASS";
    private final String WARNINGPASS = "WARNINGPASS";
    private static CharSequence UID = "%UID%";
    private static CharSequence DISPLAYNAME = "%DISPLAYNAME%";
    private static CharSequence PWD = "%PWD%";
    private EmailUtil mail;
    private static String mailPropFile = "mail.properties";
    private static String ldapPropFile = "ldap.properties.prod";
    
//    private Boolean forceSend = true;
//    private String debugFilename = "mail_pwd.log";
//    private static String passNotifTemplFile = "pwdExpEmail.template";
//    private static String initEmailNotifiTemplFile = "initialEmailForWP.template";

    
 // private static String ldapPropFile = "ldap.properties";
//    //////////////////////
//    private String passwordExpiration = "365";
//    private String initialsNotificationInterval = "0,1,2,4,8,16,32,64";
//    private String notificationInterval = "1,2,4,8,16,32,64,90,180"; //Countdown 
//    private String specialUsers = "";
//    private String specialOU = "ou=Services,ou=Administrators";
//    private String debugEmailsToFile = "false";
    /////////////////////
    public static void main(String[] args) throws NamingException, MessagingException, UnsupportedEncodingException, IOException, ParseException {
        PasswordExpirationNotification sup = new PasswordExpirationNotification();
        sup.run();
        sup.close();

    }

    private void run() throws NamingException, MessagingException, UnsupportedEncodingException, IOException, ParseException {
        // this.test();
        NamingEnumeration<SearchResult> sr = getAllLdapAccounts();
        analyzeSearchResult(sr);
        sr.close();
    }

    long getPasswordAge(String age) throws ParseException {

        String[] parts = age.split("[.]");
        String dateTimePart = parts[0];
        String timeZonePart = "+0" + parts[1].substring(0, parts[1].length() - 1) + "00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ");
        Date theDate = sdf.parse(dateTimePart + timeZonePart);
        Date now = new Date();
        long diff = now.getTime() - theDate.getTime();
        long output = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        return output;
    }

    private void close() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    private NamingEnumeration<SearchResult> getAllLdapAccounts() throws IOException, NamingException {
        LdapUtils util = new LdapUtils(ldapPropFile);
        DirContext ctx = util.connect();
        LdapFilter lf = new LdapFilter();
        String ldapSearchBase = util.getLdapSearchBase();
        String searchFilter = util.getLdapSearchFilter();
        NamingEnumeration<SearchResult> findAccountByAccountName = lf.findAccountsBySearchFiletr(ctx, ldapSearchBase, searchFilter);
        return findAccountByAccountName;

    }

    private void analyzeSearchResult(NamingEnumeration<SearchResult> sr) throws NamingException, ParseException, IOException, MessagingException {
        int i = 0;
        while (sr.hasMore()) {

            SearchResult account = sr.next();
            //System.out.println(account);
            String accountDN = account.getNameInNamespace();

            Attributes attributes = (Attributes) account.getAttributes();

            String pwdChangedTime = null;
            String createTimestamp = null;
            String mail = null;
            String uid = null;
            String displayName = null;

            if (attributes.get("pwdChangedTime") != null) {
                pwdChangedTime = (String) attributes.get("pwdChangedTime").get();
            }
            if (attributes.get("createTimestamp") != null) {
                createTimestamp = (String) attributes.get("createTimestamp").get();
            }
            if (attributes.get("mail") != null) {
                mail = (String) attributes.get("mail").get();
            }
            if (attributes.get("uid") != null) {
                uid = (String) attributes.get("uid").get();
            }
            if (attributes.get("displayName") != null) {
                displayName = (String) attributes.get("displayName").get();
            }

            if (mail == null) {
                initEmail();
            }

            if (checkUserSpecialOU(accountDN)) {
                //For some special containers gather details and send to technical iam mailbox
                System.out.println("account from special OU : " + accountDN);
                putAnalitic(SPECOU, accountDN);
            } else if (pwdChangedTime != null && !pwdChangedTime.isEmpty() && !pwdChangedTime.equalsIgnoreCase(createTimestamp)) {
                //send notification about password change
                System.out.println("Check pwdChangedTime");
                if (shouldSendMailWithCountdown(pwdChangedTime)) {
                    System.out.println("Send PWD expiration notification: " + uid + " : " + pwdChangedTime);
                    sendEmailNotification(mail, displayName, uid, pwdChangedTime);
                    putAnalitic(WARNINGPASS, accountDN);
                }
            } else if (checkForSpecialUser(uid)) {
                //Skip some user from notification about initial password
                //some special event or notification
                putAnalitic(SPECUSER, accountDN);
                System.out.println("special account was skiped: " + uid);
            } else {
                //send notification about initial password  
                System.out.println("Check initialPassword");
                if (shouldSendInitialMail(createTimestamp) || getForceSend()) {
                    System.out.println("Send initial mail: " + uid + " : " + createTimestamp);
                    sendInitialEmailNotification(mail, displayName, uid, createTimestamp);
                    putAnalitic(INITIALPASS, accountDN);
                    i = i + 1;
                }
            }
        }

        System.out.println("Send analitic");
        sendAnalitic(adminEmail);
        System.out.println("Number of initial mails=" + i);
        this.close();
    }

    private boolean checkUserSpecialOU(String accountDN) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        ArrayList specOU = getSpecialOUList();
        result = containsInList(specOU, accountDN); //Get List of Special OU for example Services
        return result;
    }

    private boolean checkForSpecialUser(String uid) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        ArrayList specUsers = getSpecialUserList();
        result = checkInList(specUsers, uid);
        return result;
    }

    private boolean shouldSendMail(String pwdChangedTime) throws ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        // yyyyMMddHHmmssX or yyyyMMddHHmmss'Z'
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX");
        Date pwdTime = dateFormat.parse(pwdChangedTime);
        Date currentDate = new Date();
        int diffInDays = (int) ((currentDate.getTime() - pwdTime.getTime()) / (1000 * 60 * 60 * 24));
        ArrayList notification = getNotificationInterval();
        result = checkInList(notification, String.valueOf(diffInDays));
        return result;
    }

    private boolean shouldSendMailWithCountdown(String pwdChangedTime) throws ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        // yyyyMMddHHmmssX or yyyyMMddHHmmss'Z'
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX");
        Date pwdTime = dateFormat.parse(pwdChangedTime);
        Date currentDate = new Date();
        int diffInDays = (int) ((currentDate.getTime() - pwdTime.getTime()) / (1000 * 60 * 60 * 24));
        ArrayList notification = getNotificationInterval();
        String passwordExpiration = getpasswordExpiration();
        result = checkpasswordExpirationInterval(notification, passwordExpiration, String.valueOf(diffInDays));
        return result;
    }

    private boolean shouldSendInitialMail(String createTimestamp) throws ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        // yyyyMMddHHmmssX or yyyyMMddHHmmss'Z'
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX"); //20171218095633Z
        Date pwdTime = dateFormat.parse(createTimestamp);
        Date currentDate = new Date();
        int diffInDays = (int) ((currentDate.getTime() - pwdTime.getTime()) / (1000 * 60 * 60 * 24));
        ArrayList initNotification = getInitialsNotificationInterval();
        result = checkInList(initNotification, String.valueOf(diffInDays));  // Insert force send email
        return result;
    }

    private Boolean checkInList(ArrayList list, String input) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        Boolean result = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).toString().equalsIgnoreCase(input)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void putAnalitic(String analiticCase, String account) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //System.out.println("Module putAnalitic is not ready yet");
    }

    private ArrayList getInitialsNotificationInterval() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String[] intervalArr = mail.getInitialsNotificationInterval().split(",", -2);
        ArrayList intervalArrList = new ArrayList<>(Arrays.asList(intervalArr));
        return intervalArrList;
    }

    private ArrayList getNotificationInterval() {
        String[] intervalArr = mail.getNotificationInterval().split(",", -2);
        ArrayList intervalArrList = new ArrayList<>(Arrays.asList(intervalArr));
        return intervalArrList;
    }

    private ArrayList getSpecialOUList() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String[] intervalArr = mail.getSpecialOU().split(",", -2);
        ArrayList intervalArrList = new ArrayList<>(Arrays.asList(intervalArr));
        return intervalArrList;
    }

    private ArrayList getSpecialUserList() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String[] intervalArr = mail.getSpecialUsers().split(",", -2);
        ArrayList intervalArrList = new ArrayList<>(Arrays.asList(intervalArr));
        return intervalArrList;
    }

    private String getDebugEmails() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return mail.getDebugEmailsToFile();
    }

    private void sendEmailNotification(String mailAddress, String displayName, String uid, String pwdChangedTime) throws IOException, MessagingException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String debug = getDebugEmails();
        if (debug == null || debug.equalsIgnoreCase("true")) {
            System.out.println("Debug notification");
            writeEmailTofile(mail.getDebugFilename(), mailAddress, uid, displayName, pwdChangedTime);

        } else {
            if (adminEmail == null) {
                getAdminEmail();
            }
            if (mailAddress == null) {
                initEmail();
            }
            sendNotification(mailAddress, displayName, uid, pwdChangedTime);
        }

    }

    private void sendInitialEmailNotification(String recipient, String displayName, String uid, String createTimestamp) throws IOException, MessagingException, ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //https://www.journaldev.com/2532/javamail-example-send-mail-in-java-smtp

        // System.out.println("SimpleEmail Start");
        String toEmail = null;
        // System.out.println("recipient: " + recipient);
        String debug = getDebugEmails();
        if (debug == null || debug.equalsIgnoreCase("true")) {
            System.out.println("Debug notification");
            writeEmailTofile(mail.getDebugFilename(), recipient, uid, displayName, createTimestamp);

        } else {
            if (recipient == null || recipient.isEmpty()) {
                if (adminEmail == null) {
                    getAdminEmail();
                }
                toEmail = adminEmail;
            } else {
                toEmail = recipient;
            }
            if (mail == null) {
                initEmail();
            }

            if (getPathToAttach() != null && !getPathToAttach().isEmpty()) {
                // attachments
                String[] attachFiles = new String[1];
                attachFiles[0] = getPathToAttach();
                //System.out.println(getPathToAttach());
                mail.sendEmailWithAttachment(mail.Initialization(), toEmail, getInitialEmailNotificationSubject(uid), getInitialEmailNotificationBody(displayName, uid, createTimestamp), attachFiles);
            } else {
                mail.sendEmail(mail.Initialization(), toEmail, getInitialEmailNotificationSubject(uid), getInitialEmailNotificationBody(displayName, uid, createTimestamp));
            }
        }
    }

    private void sendAnalitic(String adminEmail) throws IOException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if (adminEmail == null) {
            getAdminEmail();
        }
        if (mail == null) {
            initEmail();
        }
        System.out.println("Send analitic not supported yet.");
    }

    private Boolean containsInList(ArrayList list, String input) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        Boolean result = false;
        for (int i = 0; i < list.size(); i++) {
            if (input.toLowerCase().contains(list.get(i).toString().toLowerCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void writeEmailTofile(String debugFilename, String mail, String uid, String displayName, String pwdChangedTime) throws FileNotFoundException, UnsupportedEncodingException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        File f = new File(debugFilename);
        PrintWriter out = null;
        if (f.exists() && !f.isDirectory()) {
            out = new PrintWriter(new FileOutputStream(f, true));
        } else {
            out = new PrintWriter(debugFilename);
        }
        String now = new Date().toString();
        String newline = "\n";
        out.append(now + " : " + mail + " : " + uid + " : " + displayName + " : " + pwdChangedTime + newline);
        out.close();
    }

    private String getPasswordNotificationSubject(String uid) throws IOException {
        String subj = getPasswordNotificationSubjectTemplate();
        subj = subj.replace(UID, uid);
        return subj;
    }

    private String getPasswordNotificationBody(String displayName, String uid, String pwdChangedTime) throws IOException {
        String body = getPasswordNotificationBodyTemplate();
        body = body.replace(DISPLAYNAME, displayName).replace(UID, uid).replace(PWD, pwdChangedTime);
        return body;
    }

    private String getInitialEmailNotificationSubject(String uid) throws IOException {
        String subj = getInitialEmailNotificationSubjectTemplate();
        if (uid != null) {
            subj = subj.replace(UID, uid);
        }
        return subj;
    }

    private String getInitialEmailNotificationBody(String displayName, String uid, String createTimestamp) throws IOException, ParseException {
        String body = getInitialEmailNotificationBodyTemplate();
        if (displayName != null) {
            body = body.replace(DISPLAYNAME, displayName);
        }
        if (uid != null) {
            body = body.replace(UID, uid);
        }
        if (createTimestamp != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX");
            DateFormat dateFormatPrint = new SimpleDateFormat("dd-MM-yyyy");
            Date pwdTime = dateFormat.parse(createTimestamp);
            String createTimestampPrint = dateFormatPrint.format(pwdTime);
            body = body.replace(PWD, createTimestampPrint);
        }
        return body;
    }

    public Boolean getForceSend() {
        return Boolean.getBoolean(mail.getForceSend());
    }

    private void sendNotification(String recipient, String displayName, String uid, String pwdChangedTime) throws IOException, MessagingException {
        //https://www.journaldev.com/2532/javamail-example-send-mail-in-java-smtp

        // System.out.println("SimpleEmail Start");
        String toEmail = null;
        // System.out.println("recipient: " + recipient);
        if (recipient == null || recipient.isEmpty()) {
            if (adminEmail == null) {
                getAdminEmail();
            }
            toEmail = adminEmail;
        } else {
            toEmail = recipient;
        }

        if (mail == null) {
            initEmail();
        }

        mail.sendEmail(mail.Initialization(), toEmail, getPasswordNotificationSubject(uid), getPasswordNotificationBody(displayName, uid, pwdChangedTime));

    }

    private String getPasswordNotificationSubjectTemplate() throws FileNotFoundException, IOException {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return readFromFile(mail.getPassNotifTemplFile(), "subject");
    }

    private String getPasswordNotificationBodyTemplate() throws FileNotFoundException, IOException {
        //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return readFromFile(mail.getPassNotifTemplFile(), "body");
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    private void initEmail() throws IOException {
        //https://www.journaldev.com/2532/javamail-example-send-mail-in-java-smtp
        System.out.println("Init Email");
        mail = new EmailUtil(mailPropFile);
    }

    private String getInitialEmailNotificationSubjectTemplate() throws FileNotFoundException, IOException {
        //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return readFromFile(mail.getInitEmailNotifiTemplFile(), "subject");
    }

    private String getInitialEmailNotificationBodyTemplate() throws FileNotFoundException, IOException {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return readFromFile(mail.getInitEmailNotifiTemplFile(), "body");
    }

    private String readFromFile(String filename, String property) throws UnsupportedEncodingException, FileNotFoundException, IOException {

        Properties prop = new Properties();
        FileInputStream input;
        input = new FileInputStream(new File(filename));

        // load a properties file
        prop.load(new InputStreamReader(input, Charset.forName("UTF-8")));
        return prop.getProperty(property);
    }

    private String getpasswordExpiration() {
        return mail.getPasswordExpiration();
    }

    private Boolean checkpasswordExpirationInterval(ArrayList expirationNotificationInterval, String passwordExpirationPolicy, String passwordAge) {
        if (expirationNotificationInterval == null || expirationNotificationInterval.isEmpty() || passwordExpirationPolicy == null) {
            return false;
        }
        int policy = Integer.parseInt(passwordExpirationPolicy);
        Boolean result = false;
        for (int i = 0; i < expirationNotificationInterval.size(); i++) {
            int diff = policy - Integer.parseInt(passwordAge);
            if (Integer.parseInt(expirationNotificationInterval.get(i).toString()) == diff) {
                result = true;
                break;
            }
        }
        return result;

    }

    private String getPathToAttach() {
        return mail.getPathToAttach();
    }
}
