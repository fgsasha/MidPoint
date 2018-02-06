/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private String adminEmail=null;
    private final String SPECOU = "SPECOU";
    private final String SPECUSER = "SPECUSER";
    private final String INITIALPASS = "INITIALPASS";
    private final String WARNINGPASS = "WARNINGPASS";
    private String debugFilename = "mail_pwd.log";
    private static CharSequence UID="%UID%";
    private static CharSequence DISPLAYNAME="%DISPLAYNAME%";
    private static CharSequence PWD="%PWD%";

    public static void main(String[] args) throws NamingException, MessagingException, UnsupportedEncodingException, IOException, ParseException {
        PasswordExpirationNotification sup = new PasswordExpirationNotification();
        sup.run();
        sup.close();

    }


    private void run() throws NamingException, MessagingException, UnsupportedEncodingException, IOException, ParseException {
        // this.test();
        NamingEnumeration<SearchResult> sr = getAllLdapAccounts();
        analyzeSearchResult(sr);

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

    void test() throws NamingException, MessagingException, UnsupportedEncodingException, IOException {
        LdapUtils util = new LdapUtils("ldap.properties");
        DirContext ctx = util.connect();
        LdapFilter lf = new LdapFilter();
        String ldapSearchBase = util.getLdapSearchBase();
        String accountName = "idm-wr";
        SearchResult findAccountByAccountName = lf.findAccountByAccountName(ctx, ldapSearchBase, accountName);
        System.out.println("Result: " + findAccountByAccountName.toString());
        //Attributes attributes = ctx.getAttributes(entryDN, new String[] {"*", "+"});
        System.out.println("Result2: " + findAccountByAccountName.getAttributes());

        System.out.println(findAccountByAccountName.getNameInNamespace().toString());
        Attributes attributes = ctx.getAttributes(findAccountByAccountName.getNameInNamespace().toString(), new String[]{"*", "+"});
        System.out.println("uid: " + attributes.get("mail").get());
        if (attributes.get("pwdChangedTime") != null) {
            System.out.println("pwdChangedTime: " + attributes.get("pwdChangedTime").get());
        } else {
            System.out.println("createTimestamp: " + attributes.get("createTimestamp").get());
        }
        System.out.println("userPassword: " + attributes.get("userPassword").get());
        ctx.close();
        this.sendNotification(attributes.get("mail").get().toString());

    }

    void sendNotification(String recipient) throws MessagingException, UnsupportedEncodingException, IOException {
        //https://www.journaldev.com/2532/javamail-example-send-mail-in-java-smtp
        EmailUtil mail = new EmailUtil("mail.properties");

        System.out.println("SimpleEmail Start");

        String toEmail = null;
        System.out.println("recipient: " + recipient);
        if (recipient == null || recipient.isEmpty()) {
            toEmail = "default@example.com";
        } else {
            toEmail = recipient;
        }

        mail.sendEmail(mail.Initialization(), toEmail, "SimpleEmail Testing Subject", "SimpleEmail Testing Body");

    }

    void exportResultInFile() {

    }

    private void close() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    private NamingEnumeration<SearchResult> getAllLdapAccounts() throws IOException, NamingException {
        LdapUtils util = new LdapUtils("ldap.properties");
        DirContext ctx = util.connect();
        LdapFilter lf = new LdapFilter();
        String ldapSearchBase = util.getLdapSearchBase();
        //    String accountName = "idm-wr";
        NamingEnumeration<SearchResult> findAccountByAccountName = lf.findAccountsBySearchFiletr(ctx, ldapSearchBase, null);
        return findAccountByAccountName;

    }

    private void analyzeSearchResult(NamingEnumeration<SearchResult> sr) throws NamingException, ParseException, IOException, MessagingException {

        while (sr.hasMore()) {
            SearchResult account = sr.next();
            String accountDN = account.getNameInNamespace();
            String pwdChangedTime = (String) account.getAttributes().get("pwdChangedTime").get();
            String createTimestamp = (String) account.getAttributes().get("createTimestamp").get();
            String mail = (String) account.getAttributes().get("mail").get();
            String uid = (String) account.getAttributes().get("uid").get();
            String displayName = (String) account.getAttributes().get("displayName").get();

            if (checkUserSpecialOU(accountDN)) {
                //For some special containers gather details and send to technical iam mailbox
                putAnalitic(SPECOU, accountDN);
            } else if (pwdChangedTime != null && !pwdChangedTime.isEmpty() && pwdChangedTime != createTimestamp) {
                //send notification about password change
                if (shouldSendMail(pwdChangedTime)) {
                    sendEmailNotification(mail, displayName, uid, pwdChangedTime);
                    putAnalitic(WARNINGPASS, accountDN);
                }
            } else if (checkForSpecialUser(accountDN)) {
                //Skip some user from notification about initial password
                //some special event or notification
                putAnalitic(SPECUSER, accountDN);
                System.out.println("special account was skiped: " + accountDN);
            } else {
                //send notification about initial password       
                if (shouldSendInitialMail(createTimestamp)) {
                    sendInitialEmailNotification(mail, displayName, uid, createTimestamp);
                    putAnalitic(INITIALPASS, accountDN);
                }
            }
        }
        if(adminEmail==null){getAdminEmail();}
        sendAnalitic(adminEmail);
        this.close();
    }

    private boolean checkUserSpecialOU(String accountDN) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        ArrayList specOU = getSpecialOUList();
        result = containsInList(specOU, accountDN); //Get List of Special OU for example Services
        return result;
    }

    private boolean checkForSpecialUser(String accountDN) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        ArrayList specUsers = getSpecialUserList();
        result = checkInList(specUsers, accountDN);
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
        ArrayList notification = getNotificationIntervalsList();
        result = checkInList(notification, String.valueOf(diffInDays));
        return result;
    }

    private boolean shouldSendInitialMail(String createTimestamp) throws ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        // yyyyMMddHHmmssX or yyyyMMddHHmmss'Z'
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX");
        Date pwdTime = dateFormat.parse(createTimestamp);
        Date currentDate = new Date();
        int diffInDays = (int) ((currentDate.getTime() - pwdTime.getTime()) / (1000 * 60 * 60 * 24));
        ArrayList initNotification = getInitialsNotificationIntervalsList();
        result = checkInList(initNotification, String.valueOf(diffInDays));
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ArrayList getInitialsNotificationIntervalsList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ArrayList getNotificationIntervalsList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ArrayList getSpecialOUList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ArrayList getSpecialUserList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getDebugNotification() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void sendEmailNotification(String mail, String displayName, String uid, String pwdChangedTime) throws IOException, MessagingException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String debug = getDebugNotification();
        if (debug == null || debug == "true") {
            System.out.println("Debug notification");
            writeEmailTofile(debugFilename, mail, uid, displayName, pwdChangedTime);

        } else {
            sendNotification(mail, displayName, uid, pwdChangedTime);
        }

    }

    private void sendInitialEmailNotification(String mail, String displayName, String uid, String createTimestamp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void sendAnalitic(String adminEmail) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        out.append(now + " : " + mail + " : " + uid + " : " + displayName + " : " + pwdChangedTime);
        out.close();
    }

    private String getPasswordNotificationSubject(String uid) {
        String subj=getPasswordNotificationSubjectTemplate();
        subj=subj.replace(UID, uid);
        return subj;
    }

    private String getPasswordNotificationBody(String displayName, String uid, String pwdChangedTime) {
    String body = getPasswordNotificationBodyTemplate();
    body=body.replace(DISPLAYNAME, displayName).replace(UID, uid).replace(PWD,pwdChangedTime);
    return body;
    }

    private void sendNotification(String recipient, String displayName, String uid, String pwdChangedTime) throws IOException, MessagingException {
        //https://www.journaldev.com/2532/javamail-example-send-mail-in-java-smtp
        EmailUtil mail = new EmailUtil("mail.properties");

        // System.out.println("SimpleEmail Start");
        String toEmail = null;
        // System.out.println("recipient: " + recipient);
        if (recipient == null || recipient.isEmpty()) {
            if(adminEmail==null){getAdminEmail();}
            toEmail = adminEmail;
        } else {
            toEmail = recipient;
        }

        mail.sendEmail(mail.Initialization(), toEmail, getPasswordNotificationSubject(uid), getPasswordNotificationBody(displayName, uid, pwdChangedTime));

    }

    private String getPasswordNotificationSubjectTemplate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getPasswordNotificationBodyTemplate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public String getAdminEmail() {
        return adminEmail;
    }
}
