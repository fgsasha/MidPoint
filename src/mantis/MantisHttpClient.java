/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantis;

import hrdata.HTMLutils;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author o.nekriach
 */
public class MantisHttpClient {

    public void MantisHttpClient() {
        log.setLevel(Level.INFO);
    }
    Logger log = Logger.getLogger(MantisUtil.class.getName());

    private MantisUtil util;
    private HTMLutils html;
    //POST parameters//
    final static String USERNAME = "username";
    final static String EMAIL = "email";
    final static String REALNAME = "realname";
    final static String ACCESSLEVEL = "access_level";
    final static String ENABLED = "enabled";
    final static String PROTECTED = "protected";
    final static String OP_USERID = "user_id";
    final static String OP_UPDATETOKEN = "manage_user_update_token";
    final static String OP_CREATETOKEN = "manage_user_create_token";
    final static String OP_CREATEPAGETOKEN = "manage_user_create_page_token";
    final static int users_on_page = 50;

    ///////////////////
    final static String LOGIN_URL = "/login.php";
    final static String CREATEUSER_URL = "/manage_user_create.php";
    final static String UPDATEUSER_URL = "/manage_user_update.php";
    final static String MANAGEUSER_PAGE = "/manage_user_page.php";
    final static String EDITUSER_PAGE = "/manage_user_edit_page.php?";
    final static String CREATEUSER_PAGE = "/manage_user_create_page.php";
    final static String COOKIE = "MANTIS_STRING_COOKIE";
    final static String LISTUSER_PAGE = "/manage_user_page.php?filter=ALL&hideinactive=0&showdisabled=1&sort=username&dir=ASC&page_number=";

    private Map currentUserData = null;
    private String url = null;
    private String searchAttribute = null;
    private String sendEmail = "1";

    public void setSearchAttribute(String searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void init() {
        util = new mantis.MantisUtil();
        html = new hrdata.HTMLutils();
    }

    public MantisUtil getUtil() {
        return util;
    }

    public void setSendEmail(String sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean connect(String url, String username, String pass) throws IOException {
        setUrl(url);
        html.doAuthenticationMantis(url + LOGIN_URL, username, pass);
        CookieManager manager = html.getManager();
        CookieStore cookieJar = manager.getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();
        boolean result = false;
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(COOKIE)) {
                if (cookie.getValue().isEmpty() == false) {
                    result = true;
                }
            }
        }
        if (result) {
            return result;
        } else {
            throw new VerifyError("Your account may be disabled or blocked or the username/password you entered is incorrect");
        }
    }

    /**
     *
     * @param inputUserData the value of inputUserData
     */
    public String createUserProfile(Map inputUserData) throws IOException {
        String userid = null;
        if (this.currentUserData == null) {
            this.setSearchAttribute(USERNAME);
            this.currentUserData = this.getUserData((String) inputUserData.get(USERNAME));
        }
        if (this.currentUserData == null) {
            this.createUser(inputUserData);
            this.setSearchAttribute(USERNAME);
            this.currentUserData = this.getUserData((String) inputUserData.get(USERNAME));
            if (util == null) {
                util = new MantisUtil();
            }
            if (sendEmail.equals("1")) {
                if (this.currentUserData == null) {
                    throw new VerifyError("Cant create user. Please check that input paramjeter _confirmed is set to 1");
                }
                try {
                    util.SendEmailToNewUser(this.currentUserData.get(EMAIL).toString(), this.currentUserData.get(USERNAME).toString());
                } catch (MessagingException ex) {
                    Logger.getLogger(MantisHttpClient.class.getName()).log(Level.SEVERE, null, ex);
                    Logger.getLogger(MantisHttpClient.class.getName()).log(Level.SEVERE, null, "Sending Email exception. Cant send email no new user. Check emailConfigFile and input data");
                }
            }
        } else {
            throw new VerifyError("User " + (String) inputUserData.get(USERNAME) + " already exist. Another reason is wrong user_id for user update: " + inputUserData.get(USERNAME));
        }
        if (this.currentUserData != null) {
            userid = (String) this.currentUserData.get(OP_USERID);
        } else {
            throw new VerifyError("Cant create user");
        }
        return userid;
    }

    public String updateUserProfile(Map userData) throws IOException {
        String userid = null;
        if (this.currentUserData == null) {
            if (userData.containsKey(OP_USERID) && !userData.get(OP_USERID).toString().isEmpty()) {
                this.setSearchAttribute(OP_USERID);
                this.currentUserData = getUserData((String) userData.get(OP_USERID));
            } else {
                this.setSearchAttribute(USERNAME);
                this.currentUserData = getUserData((String) userData.get(USERNAME));
            }
        }
        if (this.currentUserData == null) {
            this.createUserProfile(userData);
            this.setSearchAttribute(USERNAME);
            this.currentUserData = getUserData((String) userData.get(USERNAME));
            if (this.currentUserData != null) {
                userid = (String) this.currentUserData.get(OP_USERID);
            } else {
                throw new VerifyError("Cant create user. No such user");
            }
        } else {
            if (userShouldBeUpdated(userData)) {
                this.updateUser(userData);
                this.setSearchAttribute(USERNAME);
                this.currentUserData = getUserData((String) userData.get(USERNAME));
            }
            if (this.currentUserData != null) {
                if (userData.get(OP_USERID) != null && !userData.get(OP_USERID).toString().equalsIgnoreCase((String) currentUserData.get(OP_USERID))) {
                    throw new VerifyError("\nInput User_Id and Updated Username belongs to different users:\ninput: " + userData.get(OP_USERID) + " : " + userData.get(USERNAME) + "\noutput: " + userid + " : " + this.currentUserData.get(USERNAME));
                }
                userid = (String) this.currentUserData.get(OP_USERID);
            } else {
                throw new VerifyError("User does not exist");
            }
        }

        return userid;
    }

    private boolean userShouldBeUpdated(Map inputUserData) throws IOException {
        boolean result;
        if (this.currentUserData == null) {
            this.setSearchAttribute(USERNAME);
            this.currentUserData = this.getUserData((String) inputUserData.get(USERNAME));
        }
        if (this.currentUserData == null) {
            result = true;
        } else if (util.leftMapsIsEqlLeft(inputUserData, this.currentUserData)) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public Map getUserData(String identity) throws IOException {
        if (this.searchAttribute == null) {
            throw new VerifyError("searchAttribute cannot be null. Please use setSearchAttribute to set value: username or user_id");
        }
        Map<String, String> returnMap = new HashMap<String, String>();
        String body = html.getHttpBody(url + EDITUSER_PAGE + searchAttribute + "=" + identity);
        String updatetoken = util.getUpdateToken(body);
        if (updatetoken == null) {
            return null;
        }
        String username = util.getUsename(body);
        String realname = util.getRealname(body);
        String email = util.getEmail(body);
        String enabled = util.getEnabled(body).toString();
        String protectd = util.getProtected(body).toString();
        String accesslvl = util.getAccessLevel(body);
        String userid = util.getUserId(body);

        returnMap.put(USERNAME, username);
        returnMap.put(REALNAME, realname);
        returnMap.put(EMAIL, email);
        returnMap.put(ENABLED, enabled);
        returnMap.put(PROTECTED, protectd);
        returnMap.put(ACCESSLEVEL, accesslvl);
        returnMap.put(OP_USERID, userid);
        returnMap.put(OP_UPDATETOKEN, updatetoken);
        log.info("CurrentUser data: " + returnMap);
        return returnMap;
    }

    void createUser(Map inputUserData) throws IOException {
        if (inputUserData == null) {
            throw new VerifyError("Nothing to create input User data is null");
        }
        String manageUserPageBody = html.getHttpBody(url + MANAGEUSER_PAGE);
        String createPageToken = util.getCreatePageToken(manageUserPageBody);
        Map postParameters = new HashMap();
        postParameters.put(OP_CREATEPAGETOKEN, createPageToken);
        String createUserPageBody = html.postHttpBody(url + CREATEUSER_PAGE, postParameters);
        String createtoken = util.getCreateToken(createUserPageBody);
        if (createtoken == null) {
            throw new VerifyError("For some reason cant create user: " + inputUserData.get(USERNAME));
        }
        inputUserData.put(OP_CREATETOKEN, createtoken);
        inputUserData.remove(OP_UPDATETOKEN, createtoken);
        inputUserData = util.normalizeUserInputData(inputUserData, null);
        log.info("Create user");
        log.info("Create inputUserData: " + inputUserData);
        html.postHttpBody(url + CREATEUSER_URL, inputUserData);
    }

    private void updateUser(Map inputUserData) throws IOException {
        if (inputUserData == null) {
            throw new VerifyError("Nothing to update input User data is null");
        }
        String updatetoken = (String) this.currentUserData.get(OP_UPDATETOKEN);
        String userid = (String) this.currentUserData.get(OP_USERID);
        inputUserData.put(OP_UPDATETOKEN, updatetoken);
        inputUserData.put(OP_USERID, userid);
        inputUserData = util.normalizeUserInputData(inputUserData, this.currentUserData);
        log.info("Update user");
        log.info("Update inputUserData: " + inputUserData);
        html.postHttpBody(url + UPDATEUSER_URL, inputUserData);

    }

    public Map<String, HashMap<String, List<String>>> reconcileUserData() throws IOException {
        Map<String, HashMap<String, List<String>>> searchResult = new HashMap<String, HashMap<String, List<String>>>();
        String firstPagebodyhtml = html.getHttpBody(url + LISTUSER_PAGE + 1);
        int num = util.getNumberOfUsers(firstPagebodyhtml);
        int numpgs = ((num + users_on_page - 1) / users_on_page);
        util.getUsersAttributes(firstPagebodyhtml);
        for (int i = 2; i <= numpgs; i++) {
            String pageBody = html.getHttpBody(url + LISTUSER_PAGE + i);
            util.getUsersAttributes(pageBody);
        }
        System.out.println("Total recon users: " + util.getSearchResult().size());
        return util.getSearchResult();
    }

    public Map<String, HashMap<String, List<String>>> reconcileSingleUserData(String userid) throws IOException {
        Map<String, HashMap<String, List<String>>> searchResult = new HashMap<String, HashMap<String, List<String>>>();

        this.setSearchAttribute(OP_USERID);
        String body = html.getHttpBody(url + EDITUSER_PAGE + searchAttribute + "=" + userid);
        String updatetoken = util.getUpdateToken(body);
        if (updatetoken == null) {
            log.info("User was deleted, or wrong user_id");
            return null;
        }
        String username = util.getUsename(body);
        String realname = util.getRealname(body);
        String email = util.getEmail(body);
        String enabled = util.getEnabled(body).toString();
        String protectd = util.getProtected(body).toString();
        String accesslvl = util.getAccessLevel(body);

        HashMap<String, List<String>> values = new HashMap<String, List<String>>();
        List<String> valuesList = new ArrayList<String>();

        //userid            
        valuesList.add(userid);
        values.put(OP_USERID, valuesList);
        valuesList = new ArrayList<String>();

        //username
        valuesList.add(username);
        values.put(USERNAME, valuesList);
        valuesList = new ArrayList<String>();

        //email            
        valuesList.add(email);
        values.put(EMAIL, valuesList);
        valuesList = new ArrayList<String>();

        //realname
        valuesList.add(realname);
        values.put(REALNAME, valuesList);
        valuesList = new ArrayList<String>();

        //enabled
        valuesList.add(enabled);
        values.put(ENABLED, valuesList);
        valuesList = new ArrayList<String>();

        //protected
        valuesList.add(protectd);
        values.put(PROTECTED, valuesList);
        valuesList = new ArrayList<String>();

        //access
        valuesList.add(accesslvl);
        values.put(ACCESSLEVEL, valuesList);
        valuesList = new ArrayList<String>();

        searchResult.put(userid, values);
        return searchResult;
    }
}
