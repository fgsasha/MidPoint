/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantis;

import hrdata.HTMLutils;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author onekriach
 */
public class MantisHttpClient {

    private MantisUtil util;
    private HTMLutils html;
    final String USERNAME = "username";
    final String LOGIN_URL = "/login.php";
    final String USERVIEW_URL=""; // todo
    private Map currentUserData = null;
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }
    
    void init() {
        util = new mantis.MantisUtil();
        html = new HTMLutils();
    }

    void connect(String url, String username, String pass) throws IOException {
        html.doAuthenticationMantis(url + LOGIN_URL, username, pass);
        setUrl(url);
    }

    /**
     *
     * @param inputUserData the value of inputUserData
     */
    void createUserProfile(Map inputUserData) throws IOException {
        if (currentUserData == null) {
            currentUserData = getUserData((String) inputUserData.get(USERNAME));
        }
        if (currentUserData == null) {
            createUser(inputUserData);
        } else {
            throw new UnsupportedOperationException("User " + (String) inputUserData.get(USERNAME) + " already exist");
        }

    }

    void updateUserProfile(Map userData) throws IOException {
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
        } else {
            if (compareMapsIsEqlLeft(inputUserData, currentUserData)) {
                result = false;
            } else {
                result = true;
            }

        }

        return result;
    }

    private Map getUserData(String username) throws IOException {
       Map<String, String> returnMap = null ;
        String body = html.getHttpBody(url+USERVIEW_URL);
        returnMap.put("username", username);
        
        //todo
        
        return null;
    }

    private boolean compareMapsIsEqlLeft(Map inputUserData, Map currentUserData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void createUser(Map inputUserData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void updateUser(Map userData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
