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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author onekriach
 */
public class MantisUtil {

    Logger log = Logger.getLogger(MantisUtil.class.getName());
    static String REALNAME_PATERN ="(?s).*?<!-- Realname -->[\\r\\n].*?\\t*?([^\\t]+)\\t*?</td>[\\r\\n]+</tr>.*?";
    static String EMAIL_PATERN = "(?s).*?<!-- Email -->[\\r\\n].*?\\t*?([^\\t]+)\\t*?</td>[\\r\\n]+</tr>.*?";
    static String ENABLED_PATERN = ".*?<input type=\"checkbox\" name=\"enabled\"  checked=\"(.*?)\"  />.*?";
    static String PROTECTED_PATERN = ".*?<input type=\"checkbox\" name=\"protected\"  checked=\"(.*?)\"  />.*?";
    static String ACCESS_PATERN = "(?s).*?<select name=\"access_level\">.*?[\r\n].*?<option value=\"([\\d]+?)\" selected=\"selected\" >.*?";
    static String UPDATETOKEN_PATERN = ".*?<input type=\"hidden\" name=\"manage_user_update_token\" id=\"manage_user_update_token\" value=\"(.*?)\"/>.*?";
    static String USERID_PATERN = ".*?<input type=\"hidden\" name=\"user_id\" value=\"(.*?)\" />.*?";
    static String CREATETOKEN_PATERN = ".*?<input type=\"hidden\" name=\"manage_user_create_token\" id=\"manage_user_create_token\" value=\"(.*?)\"/>.*?";

    void MantisUtil() {
    log.setLevel(Level.INFO);    
    }
        public String getRealname(String body) {
        log.finest("getRealname body: " + body);
        Pattern p = Pattern.compile(REALNAME_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if(matchedString==null){
        log.info("getRealname:matchedString: " + matchedString);
        return null;
        }
        log.info("getRealname:matchedString: " + matchedString);
        return matchedString;
    }
        
        public String getEmail(String body) {
        log.finest("getEmail body: " + body);
        Pattern p = Pattern.compile(EMAIL_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if(matchedString==null){
        log.info("getEmail:matchedString: " + matchedString);
        return null;
        }
        log.info("getEmail:matchedString: " + matchedString);
        return matchedString;
    }
    
    public Boolean getEnabled(String body) {
        log.finest("getEnabled body: " + body);
        Pattern p = Pattern.compile(ENABLED_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if(matchedString==null){
        log.info("getEnabled:matchedString: " + matchedString);
        return false;
        }
        log.info("getEnabled:matchedString: " + matchedString);
        return matchedString.equalsIgnoreCase("checked");
    }

    public Boolean getProtected(String body) {
        log.finest("getProtected body: " + body);
        Pattern p = Pattern.compile(PROTECTED_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if(matchedString==null){
        log.info("getProtected:matchedString: " + matchedString);
        return false;
        }
        log.info("getProtected:matchedString: " + matchedString);
        return matchedString.equalsIgnoreCase("checked");
    }

    public String getAccessLevel(String body) {
        log.finest("getAccessLevel body: " + body);
        Pattern p = Pattern.compile(ACCESS_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if(matchedString==null){
        log.info("getAccessLevel:matchedString: " + matchedString);
        return null;
        }
        log.info("getAccessLevel:matchedString: " + matchedString);
        return matchedString;
    }

    public String getUpdateToken(String body) {
        log.finest("getUpdateToken body: " + body);
        Pattern p = Pattern.compile(UPDATETOKEN_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }        
        if(matchedString==null){
        log.info("getUpdateToken:matchedString: " + matchedString);
        return null;
        }
        log.info("getUpdateToken:matchedString: " + matchedString);
        return matchedString;
    }
    public String getCreateToken(String body) {
        log.finest("getCreateToken body: " + body);
        Pattern p = Pattern.compile(CREATETOKEN_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }        
        if(matchedString==null){
        log.info("getCreateToken:matchedString: " + matchedString);
        return null;
        }
        log.info("getCreateToken:matchedString: " + matchedString);
        return matchedString;
    }

    public String getUserId(String body) {
        log.finest("getUserId body: " + body);
        Pattern p = Pattern.compile(USERID_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if(matchedString==null){
        log.info("getUserId:matchedString: " + matchedString);
        return null;
        }
        log.info("getUserId:matchedString: " + matchedString);
        return matchedString;
    }

    public static void main(String[] args) throws IOException {
        String url = "<do login URL>";
        HTMLutils htmlOutput = new HTMLutils();
        htmlOutput.doAuthenticationMantis(url, "<login>", "<password>");
        String body = htmlOutput.getHttpBody("get URL 1");

        CookieManager manager = htmlOutput.getManager();
        CookieStore cookieJar = manager.getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();
//        for (HttpCookie cookie : cookies) {
//            System.out.println(cookie);
//        }
//        System.out.println("body: " + body);
    }
}
