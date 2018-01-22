/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static void main(String[] args) throws NamingException, MessagingException, UnsupportedEncodingException, IOException {
        PasswordExpirationNotification sup = new PasswordExpirationNotification();
        sup.run();
        sup.close();

    }

    private void run() throws NamingException, MessagingException, UnsupportedEncodingException, IOException {
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

    private void analyzeSearchResult(NamingEnumeration<SearchResult> sr) throws NamingException {

        while (sr.hasMore()) {
            SearchResult account = sr.next();
            String accountDN = account.getNameInNamespace();
            String pwdChangedTime = "";
            String userCreated = "";

            if (checkUserSpecialOU(accountDN)) {
                //For some special containers gather details and send to technical iam mailbox
            } else if (account.getAttributes().get("pwdChangedTime").get() != null && !account.getAttributes().get("pwdChangedTime").get().toString().isEmpty()) {
                //send notification about password change

            } else if (checkForSpecialUser(accountDN)) {
                //Skip some user from notification about initial password

            } else {
                //send notification about initial password       

            }

        }

    }

    private boolean checkUserSpecialOU(String accountDN) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean checkForSpecialUser(String accountDN) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
