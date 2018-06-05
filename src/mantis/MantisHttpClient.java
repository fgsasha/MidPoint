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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author onekriach
 */
public class MantisHttpClient {

    private MantisUtil util;
    private HTMLutils html;
    //POST parameters//
    final String USERNAME = "username";
    final String EMAIL = "email";
    final String REALNAME = "realname";
    final String ACCESSLEVEL = "access_level";
    final String ENABLED = "enabled";
    final String PROTECTED = "protected";
    final String OP_USERID = "user_id";
    final String OP_UPDATETOKEN = "manage_user_update_token";
    final String OP_CREATETOKEN = "manage_user_create_token";
    final int users_on_page = 50;

    ///////////////////
    final String LOGIN_URL = "/login.php";
    final String CREATEUSER_URL = "/manage_user_create.php";
    final String UPDATEUSER_URL = "/manage_user_update.php";
    final String MANAGEUSER_PAGE = "/manage_user_edit_page.php?username=";
    final String CREATEUSER_PAGE = "/manage_user_create_page.php";
    final String COOKIE = "MANTIS_STRING_COOKIE";
    final String LISTUSER_PAGE = "/manage_user_page.php?filter=ALL&hideinactive=0&showdisabled=1&sort=username&dir=ASC&page_number=";

    private Map currentUserData = null;
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public void init() {
        util = new mantis.MantisUtil();
        html = new HTMLutils();
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
    public void createUserProfile(Map inputUserData) throws IOException {
        if (currentUserData == null) {
            currentUserData = getUserData((String) inputUserData.get(USERNAME));
        }
        if (currentUserData == null) {
            createUser(inputUserData);
            currentUserData = getUserData((String) inputUserData.get(USERNAME));
        } else {
            throw new VerifyError("User " + (String) inputUserData.get(USERNAME) + " already exist");
        }

    }

    public void updateUserProfile(Map userData) throws IOException {
        if (userShouldBeUpdated(userData)) {
            updateUser(userData);
        }
    }

    private boolean userShouldBeUpdated(Map inputUserData) throws IOException {
        boolean result;
        if (currentUserData == null) {
            currentUserData = getUserData((String) inputUserData.get(USERNAME));
        }
        if (currentUserData == null) {
            createUserProfile(inputUserData);
            result = false;
        } else if (util.compareMapsIsEqlLeft(inputUserData, currentUserData)) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public Map getUserData(String username) throws IOException {
        Map<String, String> returnMap = new HashMap<String, String>();
        String body = html.getHttpBody(url + MANAGEUSER_PAGE + username);
        String updatetoken = util.getUpdateToken(body);
        if (updatetoken == null) {
            return null;
        }
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

        return returnMap;
    }

    void createUser(Map inputUserData) throws IOException {
        if (inputUserData == null) {
            throw new VerifyError("Nothing to create input User data is null");
        }
        String createPageBody = html.getHttpBody(url + CREATEUSER_PAGE);
        String createtoken = util.getCreateToken(createPageBody);
        if (createtoken == null) {
            throw new VerifyError("For some reason cant create user: " + inputUserData.get(USERNAME));
        }
        inputUserData.put(OP_CREATETOKEN, createtoken);
        inputUserData = util.normalizeUserInputData(inputUserData, null);
        html.postHttpBody(url + CREATEUSER_URL, inputUserData);
    }

    private void updateUser(Map inputUserData) throws IOException {
        if (inputUserData == null) {
            throw new VerifyError("Nothing to create input User data is null");
        }
        if (currentUserData == null) {
            currentUserData = getUserData((String) inputUserData.get(USERNAME));
        }
        if (currentUserData == null) {
            createUser(inputUserData);
            currentUserData = getUserData((String) inputUserData.get(USERNAME));
        } else {
            String updatetoken = (String) currentUserData.get(OP_UPDATETOKEN);
            String userid = (String) currentUserData.get(OP_USERID);
            inputUserData.put(OP_UPDATETOKEN, updatetoken);
            inputUserData.put(OP_USERID, userid);
            inputUserData = util.normalizeUserInputData(inputUserData, currentUserData);
            html.postHttpBody(url + UPDATEUSER_URL, inputUserData);
            getUserData((String) inputUserData.get(USERNAME));
        }

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
}
