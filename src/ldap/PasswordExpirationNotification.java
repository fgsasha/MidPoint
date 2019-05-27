/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import mail.EmailUtil;

/**
 *
 * @author o.nekriach
 */
public class PasswordExpirationNotification {

    private DirContext ctx;
    private LdapUtils util;
    private String admin = null;
    private final String SPECOU = "SPECOU";
    private final String SPECUSER = "SPECUSER";
    private final String INITIALPASS = "INITIALPASS";
    private final String WARNINGPASS = "WARNINGPASS";
    private final String NEVEREXPIRED = "2812345";
    private static CharSequence UID = "%UID%";
    private static CharSequence DISPLAYNAME = "%DISPLAYNAME%";
    private static CharSequence PWD = "%PWD%";
    private static CharSequence MESSAGEDAYS = "%MESSAGEDAYS%";
    private EmailUtil mail;
    private static String mailPropFile = "mail.properties";
    private static String ldapPropFile = "ldap.properties";
    private static Map<String, Integer> expUserDaysMap = new HashMap<>();
    private static Map<String, String[]> analiticWarn = new HashMap<>();
    private Map<String, String[]> analiticInit = new HashMap<>();
    private Map<String, String[]> analiticSpecOu = new HashMap<>();
    private Map<String, String[]> analiticSpecUsr = new HashMap<>();

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
        NamingEnumeration<SearchResult> pol = getPasswordPolicies();
        // Set PasswordPolicy pwdMaxAge mapping <DNname, Pwd Age Days>
        LdapUtils.setPwdMaxAgePolicies(pol);
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

    private void close() throws NamingException {
        if (ctx != null) {
            ctx.close();
        }

    }

    private NamingEnumeration<SearchResult> getAllLdapAccounts() throws IOException, NamingException {
        if (ctx == null) {
            util = new LdapUtils(ldapPropFile);
            ctx = util.connect();
        }
        LdapFilter lf = new LdapFilter();
        String ldapSearchBase = util.getLdapSearchBase();
        String searchFilter = util.getAccountsLdapSearchFilter();
        NamingEnumeration<SearchResult> findAccountByAccountName = lf.findEntriesBySearchFilter(ctx, ldapSearchBase, searchFilter);
        return findAccountByAccountName;

    }

    /**
     * Read all policies from OpenLdap
     *
     * @return result set
     * @throws IOException
     * @throws NamingException
     */
    private NamingEnumeration<SearchResult> getPasswordPolicies() throws IOException, NamingException {
        if (ctx == null) {
            util = new LdapUtils(ldapPropFile);
            ctx = util.connect();
        }
        LdapFilter lf = new LdapFilter();
        String ldapSearchBase = util.getLdapSearchBase();
        String searchFilter = util.getPoliciesLdapSearchFilter();
        NamingEnumeration<SearchResult> findAllPolicies = lf.findEntriesBySearchFilter(ctx, ldapSearchBase, searchFilter);
        return findAllPolicies;
    }

    private void analyzeSearchResult(NamingEnumeration<SearchResult> sr) throws NamingException, ParseException, IOException, MessagingException {
        int i = 0;
        int w = 0;
        int s = 0;
        while (sr.hasMore()) {

            SearchResult account = sr.next();
            //System.out.println(account);
            String accountDN = account.getNameInNamespace();

            Attributes attributes = (Attributes) account.getAttributes();

            String pwdChangedTime = null;
            String createTimestamp = null;
            String ldapMail = null;
            String uid = null;
            String displayName = null;
            String pwdPolicySubentry = null;

            if (attributes.get("pwdPolicySubentry") != null) {
                pwdPolicySubentry = (String) attributes.get("pwdPolicySubentry").get();
            }

            if (attributes.get("pwdChangedTime") != null) {
                pwdChangedTime = (String) attributes.get("pwdChangedTime").get();
            }
            if (attributes.get("createTimestamp") != null) {
                createTimestamp = (String) attributes.get("createTimestamp").get();
            }
            if (attributes.get("mail") != null) {
                ldapMail = (String) attributes.get("mail").get();
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
                putAnalitic(SPECOU, accountDN, displayName, uid, ldapMail, createTimestamp, pwdChangedTime, "skip", "account from special OU", pwdPolicySubentry);
            } else if (pwdChangedTime != null && !pwdChangedTime.isEmpty() && !pwdChangedTime.equalsIgnoreCase(createTimestamp)) {
                //send notification about password change                
                if (shouldSendMailWithCountdown(uid, pwdChangedTime, pwdPolicySubentry) || getForceSend()) {
                    System.out.println("Send PWD expiration notification: " + uid + " : " + pwdChangedTime);
                    sendEmailNotification(ldapMail, displayName, uid, expUserDaysMap.get(uid).toString(), getExpirationDate(expUserDaysMap.get(uid)));
                    putAnalitic(WARNINGPASS, accountDN, displayName, uid, ldapMail, createTimestamp, pwdChangedTime, expUserDaysMap.get(uid).toString(), getExpirationDate(expUserDaysMap.get(uid)), pwdPolicySubentry);
                    w = w + 1;
                }
            } else if (checkForSpecialUser(uid)) {
                //Skip some user from notification about initial password
                //some special event or notification
                putAnalitic(SPECUSER, accountDN, displayName, uid, ldapMail, createTimestamp, pwdChangedTime, "skip", "special account", pwdPolicySubentry);
                System.out.println("special account was skiped: " + uid);
            } else {
                //send notification about initial password                  
                if ((shouldSendInitialMail(createTimestamp) || getForceSend()) && getSendInitialEmail()) {
                    System.out.println("Send initial mail: " + uid + " : " + createTimestamp);
                    sendInitialEmailNotification(ldapMail, displayName, uid, createTimestamp);
                    putAnalitic(INITIALPASS, accountDN, displayName, uid, ldapMail, createTimestamp, "", "new", "account with initial password", pwdPolicySubentry);
                    i = i + 1;
                }
            }
            s = s + 1;
        }

        System.out.println("Send analitic");
        sendAnalitic(admin);
        System.out.println("Totally analyzed users " + s);
        System.out.println("Number of warning password expiration mails=" + w);
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

    /** Deprecated method +1 day is not added
     * 
     * @param pwdChangedTime
     * @return
     * @throws ParseException 
     */
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

//    private boolean shouldSendMailWithCountdown(String pwdChangedTime) throws ParseException {
//        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        Boolean result = false;
//        // yyyyMMddHHmmssX or yyyyMMddHHmmss'Z'
//        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX");
//        Date pwdTime = dateFormat.parse(pwdChangedTime);
//        Date currentDate = new Date();
//        int diffInDays = (int) ((currentDate.getTime() - pwdTime.getTime()) / (1000 * 60 * 60 * 24));
//        ArrayList notification = getNotificationInterval();
//        String passwordExpiration = getpasswordExpiration();
//        result = checkpasswordExpirationInterval(notification, passwordExpiration, String.valueOf(diffInDays));
//        return result;
//    }
    private boolean shouldSendMailWithCountdown(String uid, String pwdChangedTime, String pwdPolicySubentry) throws ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        // yyyyMMddHHmmssX or yyyyMMddHHmmss'Z'
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX");
        Date pwdTime = dateFormat.parse(pwdChangedTime);
        Date currentDate = new Date();
        // added +1 day because cast to integer of division rounds numbers to lower value       
       int diffInDays = (int) ((currentDate.getTime() - pwdTime.getTime()) / (1000 * 60 * 60 * 24))+1;
       
       
        ArrayList notification = getNotificationInterval();
        String passwordExpiration = getpasswordExpiration(pwdPolicySubentry);
        System.out.println(uid+" pwd will be expired in (policy pwdMaxAge=" + passwordExpiration + "): " + (Integer.parseInt(passwordExpiration) - diffInDays));
        result = checkpasswordExpirationInterval(notification, passwordExpiration, String.valueOf(diffInDays));
        expUserDaysMap.put(uid, (Integer.parseInt(passwordExpiration) - diffInDays));
        return result;
    }

    private boolean shouldSendInitialMail(String createTimestamp) throws ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Boolean result = false;
        // yyyyMMddHHmmssX or yyyyMMddHHmmss'Z'
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssX"); //20171218095633Z
        Date pwdTime = dateFormat.parse(createTimestamp);
        Date currentDate = new Date();
        // should be invistigated Is it necesary to add +1 day to diffInDays
        int diffInDays = (int) ((currentDate.getTime() - pwdTime.getTime()) / (1000 * 60 * 60 * 24));
        ArrayList initNotification = getInitialsNotificationInterval();
        result = checkInList(initNotification, String.valueOf(diffInDays));  // Insert force send email
        return result;
    }

    private Boolean checkInList(ArrayList list, String input) {
        Boolean result = false;
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).toString().equalsIgnoreCase(input)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void putAnalitic(String analiticCase, String accountDN, String displayName, String uid, String mail, String createTimestamp, String pwdChangedTime, String info1, String info2, String pwdPolicySubentry) {
        String[] inputArray = {analiticCase, accountDN, displayName, uid, mail, createTimestamp, pwdChangedTime, info1, info2, pwdPolicySubentry};
        if (analiticCase != null) {
            if (analiticCase.equalsIgnoreCase(WARNINGPASS)) {
                analiticWarn.put(accountDN, inputArray);
            } else if (analiticCase.equalsIgnoreCase(INITIALPASS)) {
                analiticInit.put(accountDN, inputArray);
            } else if (analiticCase.equalsIgnoreCase(SPECOU)) {
                analiticSpecOu.put(accountDN, inputArray);
            } else if (analiticCase.equalsIgnoreCase(SPECUSER)) {
                analiticSpecUsr.put(accountDN, inputArray);
            }
        }
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

    private void sendEmailNotification(String mailAddress, String displayName, String uid, String diffInDays, String pwdChangedTime) throws IOException, MessagingException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String debug = getDebugEmails();
        if (debug == null || debug.equalsIgnoreCase("true")) {
            System.out.println("Debug notification");
            writeEmailTofile(mail.getDebugFilename(), mailAddress, uid, displayName, pwdChangedTime);

        } else {
            if (admin == null) {
                admin=getAdminEmail();
            }
            if (mailAddress == null) {
                initEmail();
            }
            sendNotification(mailAddress, displayName, uid, diffInDays, pwdChangedTime);
        }

    }

    private void sendInitialEmailNotification(String recipient, String displayName, String uid, String createTimestamp) throws IOException, MessagingException, ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //https://www.journaldev.com/2532/javamail-example-send-ldapMail-in-java-smtp

        // System.out.println("SimpleEmail Start");
        String toEmail = null;
        // System.out.println("recipient: " + recipient);
        String debug = getDebugEmails();
        if (debug == null || debug.equalsIgnoreCase("true")) {
            System.out.println("Debug notification");
            writeEmailTofile(mail.getDebugFilename(), recipient, uid, displayName, createTimestamp);

        } else {
            if (recipient == null || recipient.isEmpty()) {
                if (admin == null) {
                    admin=getAdminEmail();
                }
                toEmail = admin;
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

    private void sendAnalitic(String adminEmail) throws IOException, MessagingException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         if (mail == null) {
            initEmail();
        }
        if (adminEmail == null) {
            admin = getAdminEmail();
        }
        mail.sendEmail(mail.Initialization(), admin, "IDM Util: LDAP password analitic information (" + LocalDate.now().toString() + ")", getPasswordAnaliticBody());

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

    private String getPasswordNotificationSubject(String displayName, String uid, String diffInDays, String pwdChangedTime) throws IOException {
        String subj = getPasswordNotificationSubjectTemplate();
        subj = subj.replace(DISPLAYNAME, displayName).replace(UID, uid).replace(PWD, pwdChangedTime).replace(MESSAGEDAYS, diffInDays);
        return subj;
    }

    private String getPasswordNotificationBody(String displayName, String uid, String diffInDays, String pwdChangedTime) throws IOException {
        String body = getPasswordNotificationBodyTemplate();
        body = body.replace(DISPLAYNAME, displayName).replace(UID, uid).replace(PWD, pwdChangedTime).replace(MESSAGEDAYS, diffInDays);
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
        return Boolean.parseBoolean(mail.getForceSend());
    }

    public Boolean getSendInitialEmail() {
        return Boolean.parseBoolean(mail.getSendInitialEmail());
    }

    private void sendNotification(String recipient, String displayName, String uid, String diffInDays, String pwdChangedTime) throws IOException, MessagingException {
        //https://www.journaldev.com/2532/javamail-example-send-ldapMail-in-java-smtp

        // System.out.println("SimpleEmail Start");
        String toEmail = null;
        // System.out.println("recipient: " + recipient);
        if (mail == null) {
            initEmail();
        }
        if (recipient == null || recipient.isEmpty()) {
            if (admin == null) {
                admin=getAdminEmail();
            }
            toEmail = admin;
        } else {
            toEmail = recipient;
        }


        mail.sendEmail(mail.Initialization(), toEmail, getPasswordNotificationSubject(displayName, uid, diffInDays, pwdChangedTime), getPasswordNotificationBody(displayName, uid, diffInDays, pwdChangedTime));

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
        return mail.getAdminEmail();
    }

    private void initEmail() throws IOException {
        //https://www.journaldev.com/2532/javamail-example-send-ldapMail-in-java-smtp
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

    /**
     * Get password expiration in days from configured policies by user
     * pwdPolicySubentry attribute value. If value is null return default
     * password policy value. If value of pwdMaxAge=0 (Never expired) return
     * 2147483647 days
     *
     * @param pwdPolicySubentry - user attribute which stores DN of password
     * policy
     * @return - password max age in days
     */
    private String getpasswordExpiration(String pwdPolicySubentry) {
        String output = null;
        if (LdapUtils.getPwdMaxAgeDaysPolicies() != null) {
            if (pwdPolicySubentry == null) {
                output = getpasswordExpirationFromPolicy(util.getDefaultPolicyDN());
            } else {
                output = getpasswordExpirationFromPolicy(pwdPolicySubentry);
            }
        }
        if (output == null) {
            return mail.getPasswordExpiration();
        } else if (output.equalsIgnoreCase("0")) {
            return NEVEREXPIRED;
        } else {
            return output;
        }
    }

    private Boolean checkpasswordExpirationInterval(ArrayList expirationNotificationInterval, String passwordExpirationPolicy, String passwordAge) {
        Boolean result = false;
        if (expirationNotificationInterval == null || expirationNotificationInterval.isEmpty() || passwordExpirationPolicy == null) {
            return result;
        }
        int policy = Integer.parseInt(passwordExpirationPolicy);
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

    private String getpasswordExpirationFromPolicy(String policy) {
        return LdapUtils.getPwdMaxAgeDaysPolicies().get(policy);
    }

    private String getExpirationDate(Integer inDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DATE, inDays); // Adding 5 days
        String output = sdf.format(c.getTime());
        System.out.println(output);
        return output;
    }

    private String getPasswordAnaliticBody() {
        StringBuilder stringBuild = new StringBuilder();
        stringBuild.append("IDM Util Password expiration script result:");
        stringBuild.append("<table><tr><td>" + "Case" + "</td><td>" + "DN" + "</td><td>" + "displayName" + "</td><td>" + "uid" + "</td><td>" + "mail" + "</td><td>" + "createTimestamp" + "</td><td>" + "pwdChangedTime" + "</td><td>" + "daysToExpiration" + "</td><td>" + "expirationDate" + "</td><td>" + "pwdPolicySubentry" + "</td></tr>");

        if (!analiticWarn.isEmpty()) {
            Set<String> ks = analiticWarn.keySet();
            Iterator<String> it = ks.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String[] arr = analiticWarn.get(key);
                stringBuild.append("<tr><td>" + arr[0] + "</td><td>" + arr[1] + "</td><td>" + arr[2] + "</td><td>" + arr[3] + "</td><td>" + arr[4] + "</td><td>" + arr[5] + "</td><td>" + arr[6] + "</td><td>" + arr[7] + "</td><td>" + arr[8] + "</td><td>" + arr[9] + "</td></tr>");
            }
            stringBuild.append("<br>");
        }
        if (!analiticInit.isEmpty()) {
            Set<String> ks = analiticInit.keySet();
            Iterator<String> it = ks.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String[] arr = analiticInit.get(key);
                stringBuild.append("<tr><td>" + arr[0] + "</td><td>" + arr[1] + "</td><td>" + arr[2] + "</td><td>" + arr[3] + "</td><td>" + arr[4] + "</td><td>" + arr[5] + "</td><td>" + arr[6] + "</td><td>" + arr[7] + "</td><td>" + arr[8] + "</td><td>" + arr[9] + "</td></tr>");
            }
            stringBuild.append("<br>");
        }
        if (!analiticSpecUsr.isEmpty()) {
            Set<String> ks = analiticSpecUsr.keySet();
            Iterator<String> it = ks.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String[] arr = analiticSpecUsr.get(key);
                stringBuild.append("<tr><td>" + arr[0] + "</td><td>" + arr[1] + "</td><td>" + arr[2] + "</td><td>" + arr[3] + "</td><td>" + arr[4] + "</td><td>" + arr[5] + "</td><td>" + arr[6] + "</td><td>" + arr[7] + "</td><td>" + arr[8] + "</td><td>" + arr[9] + "</td></tr>");
            }
            stringBuild.append("<br>");
        }
        if (!analiticSpecOu.isEmpty()) {
            Set<String> ks = analiticSpecOu.keySet();
            Iterator<String> it = ks.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String[] arr = analiticSpecOu.get(key);
                stringBuild.append("<tr><td>" + arr[0] + "</td><td>" + arr[1] + "</td><td>" + arr[2] + "</td><td>" + arr[3] + "</td><td>" + arr[4] + "</td><td>" + arr[5] + "</td><td>" + arr[6] + "</td><td>" + arr[7] + "</td><td>" + arr[8] + "</td><td>" + arr[9] + "</td></tr>");
            }
        }
        stringBuild.append("</table>");
        return stringBuild.toString();
    }
}
