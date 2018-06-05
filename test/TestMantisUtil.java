
import hrdata.HTMLutils;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mantis.MantisHttpClient;
import mantis.MantisUtil;
import org.apache.commons.codec.binary.StringUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author onekriach
 */
public class TestMantisUtil {

    private String url = "";
    private String userAdmin = "";
    private String password = "";
    ///////// input User data ////////
    private String username = "";
    private String realname = "";
    private String email = "";
    private String enabled = "";
    private String protectd = "";
    private String accesslvl = "";
    /////////////////////////////////
    final String USERNAME = "username";
    final String EMAIL = "email";
    final String REALNAME = "realname";
    final String ACCESSLEVEL = "access_level";
    final String ENABLED = "enabled";
    final String PROTECTED = "protected";
    ///////////////////////////////////

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserAdmin(String userAdmin) {
        this.userAdmin = userAdmin;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    void testConnection() throws IOException {
        MantisHttpClient client = new mantis.MantisHttpClient();
        client.init();
        boolean isSuccessful = client.connect(url, userAdmin, password);
        System.out.println("Connection successful: " + isSuccessful);
    }

    void testGetUserData() throws IOException {
        MantisHttpClient client = new mantis.MantisHttpClient();
        client.init();
        client.connect(url, userAdmin, password);
        Map findedUser = client.getUserData(username);
        if (findedUser == null) {
            System.out.println("Can't find user: " + username);
        } else {
            System.out.println("User has been found successful: " + username);
        }
    }

    void testCreateUser() throws IOException {
        MantisHttpClient client = new mantis.MantisHttpClient();
        client.init();
        client.connect(url, userAdmin, password);
        client.createUserProfile(initUserData());
    }

    void testUpdateUser() throws IOException {
        MantisHttpClient client = new mantis.MantisHttpClient();
        client.init();
        client.connect(url, userAdmin, password);
        client.updateUserProfile(initUserData());
    }

    private void reconcileUsers() throws IOException {
        MantisHttpClient client = new mantis.MantisHttpClient();
        client.init();
        client.connect(url, userAdmin, password);
        client.reconcileUserData();
    }

    Map initUserData() {
        Map<String, String> returnMap = new HashMap<String, String>();
        returnMap.put(USERNAME, username);
        returnMap.put(REALNAME, realname);
        returnMap.put(EMAIL, email);
        returnMap.put(ENABLED, enabled);
        returnMap.put(PROTECTED, protectd);
        returnMap.put(ACCESSLEVEL, accesslvl);
        return returnMap;
    }

    public static void main(String[] args) throws IOException {
        TestMantisUtil test = new TestMantisUtil();
        // Test connection parameters

        //////////// true false null(empty)

        //test.testConnection();
        //test.testGetUserData();
        //test.testCreateUser();
        //test.testUpdateUser();
        test.reconcileUsers();
    }

}
