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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author onekriach
 */
public class MantisUtil {

    Logger log = Logger.getLogger(MantisUtil.class.getName());

    final String USERNAME = "username";
    final String EMAIL = "email";
    final String REALNAME = "realname";
    final String ACCESSLEVEL = "access_level";
    final String ENABLED = "enabled";
    final String PROTECTED = "protected";
    final String OP_USERID = "user_id";
    final String OP_UPDATETOKEN = "manage_user_update_token";
    final String OP_CREATETOKEN = "manage_user_create_token";
    final String DEFAULT_ACCESSLEVEL = "25";
    final String NATIVE_CHEKED = "on";
    final String RESOURCE_CHEKED = "true";
    final String RESOURCE_UNCHEKED = "false";
    final String NATIVE_FORMCHEKED = "checked";
    final String REC_CHEKED = "X";
    final String REC_EMPTY = "&#160;";

    static String REALNAME_PATERN = "(?s).*?<!-- Realname -->[\\r\\n].*?\\t*?([^\\t]+)\\t*?</td>[\\r\\n]+</tr>.*?";
    static String EMAIL_PATERN = "(?s).*?<!-- Email -->[\\r\\n].*?\\t*?([^\\t]+)\\t*?</td>[\\r\\n]+</tr>.*?";
    static String ENABLED_PATERN = ".*?<input type=\"checkbox\" name=\"enabled\"  checked=\"(.*?)\"  />.*?";
    static String PROTECTED_PATERN = ".*?<input type=\"checkbox\" name=\"protected\"  checked=\"(.*?)\"  />.*?";
    static String ACCESS_PATERN = "(?s).*?<select name=\"access_level\">.*?[\r\n].*?<option value=\"([\\d]+?)\" selected=\"selected\" >.*?";
    static String UPDATETOKEN_PATERN = ".*?<input type=\"hidden\" name=\"manage_user_update_token\" id=\"manage_user_update_token\" value=\"(.*?)\"/>.*?";
    static String USERID_PATERN = ".*?<input type=\"hidden\" name=\"user_id\" value=\"(.*?)\" />.*?";
    static String CREATETOKEN_PATERN = ".*?<input type=\"hidden\" name=\"manage_user_create_token\" id=\"manage_user_create_token\" value=\"(.*?)\"/>.*?";
    static String CREATE_PAGETOKEN_PATERN = ".*?<input type=\"hidden\" name=\"manage_user_create_page_token\" id=\"manage_user_create_page_token\" value=\"(.*?)\"/>.*?";
    private String REALNAME_PATERN_2 = ".*?value=\\\"([^\\\"]+?)\\\" />";
    private String EMAIL_PATERN_2 = ".*?value=\\\"([^\\\"]+?)\\\" />";
    private String NUMBEROFUSERS_PATERN = ".*?Manage Accounts \\[([\\d]+)\\].*?";
    private String USERATTRIBUTES_PATERN = "(?s)<a href=\\\"manage_user_edit_page.php\\?(user_id=.*?)</tr>";

    Map<String, HashMap<String, List<String>>> searchResult = new HashMap<String, HashMap<String, List<String>>>();
    private String REC_USERID_PATERN = "user_id=([\\d]+)";
    private String REC_REALNAME_PATERN = "<td>([^<]+)</td>";
    private String REC_USERNAME_PATERN = "user_id=[\\d]+\\\">([^<]+)</a>";
    private String REC_EMAIL_PATERN = "[\\r\\n]+?\\t<td>([^<]+@[^<]+)</td>";

    private String REC_ACCESS_PATERN = "(?s)[\\r\\n]+?\\t<td>[^<]+@[^<]+</td>[\\r\\n]+?\t<td>([^<]+)</td>";
    private String REC_ENABLED_PATERN = "<td class=\\\"center\\\">([^<]+)";
    private String REC_PROTECTED_PATERN = "alt=\"(Protected)\"";

    public Map<String, HashMap<String, List<String>>> getSearchResult() {
        return searchResult;
    }

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
        if (matchedString == null) {
            log.info("getRealname:matchedString: " + matchedString);
            return null;
        }
        if (matchedString.contains("name=\"realname\"")) {
            Pattern p2 = Pattern.compile(REALNAME_PATERN_2);
            Matcher m2 = p2.matcher(matchedString);
            if (m2.find()) {
                matchedString = m2.group(1);
            }
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

        if (matchedString.contains("name=\"email\"")) {
            Pattern p2 = Pattern.compile(EMAIL_PATERN_2);
            Matcher m2 = p2.matcher(matchedString);
            if (m2.find()) {
                matchedString = m2.group(1);
            }
        }
        if (matchedString == null) {
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
        if (matchedString == null) {
            log.info("getEnabled:matchedString: " + matchedString);
            return false;
        }
        log.info("getEnabled:matchedString: " + matchedString);
        return matchedString.equalsIgnoreCase(NATIVE_FORMCHEKED);
    }

    public Boolean getProtected(String body) {
        log.finest("getProtected body: " + body);
        Pattern p = Pattern.compile(PROTECTED_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if (matchedString == null) {
            log.info("getProtected:matchedString: " + matchedString);
            return false;
        }
        log.info("getProtected:matchedString: " + matchedString);
        return matchedString.equalsIgnoreCase(NATIVE_FORMCHEKED);
    }

    public String getAccessLevel(String body) {
        log.finest("getAccessLevel body: " + body);
        Pattern p = Pattern.compile(ACCESS_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if (matchedString == null) {
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
        if (matchedString == null) {
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
        if (matchedString == null) {
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
        if (matchedString == null) {
            log.info("getUserId:matchedString: " + matchedString);
            return null;
        }
        log.info("getUserId:matchedString: " + matchedString);
        return matchedString;
    }

    boolean leftMapsIsEqlLeft(Map inputUserData, Map currentUserData) {
        // todo
        return false;
    }

    Map normalizeUserInputData(Map inputUserData, Map currentUserData) {
        if (inputUserData == null) {
            throw new VerifyError("Nothing to normalize input User data is null");
        }
        Map result = new HashMap();
        result.putAll(inputUserData);
        if (currentUserData == null) {
            if (inputUserData.get(EMAIL) == null || inputUserData.get(EMAIL).toString().isEmpty()) {
                result.remove(EMAIL);
            }
            if (inputUserData.get(REALNAME) == null || inputUserData.get(REALNAME).toString().isEmpty()) {
                result.remove(REALNAME);
            }
            if (inputUserData.get(ACCESSLEVEL) == null || inputUserData.get(ACCESSLEVEL).toString().isEmpty()) {
                result.put(ACCESSLEVEL, DEFAULT_ACCESSLEVEL);
            }
            if (inputUserData.get(ENABLED) == null || inputUserData.get(ENABLED).toString().isEmpty()) {
                result.put(ENABLED, NATIVE_CHEKED);
            } else if (inputUserData.get(ENABLED).toString().equalsIgnoreCase(RESOURCE_CHEKED)) {
                result.replace(ENABLED, NATIVE_CHEKED);
            } else {
                result.remove(ENABLED);
            }
            if (inputUserData.get(PROTECTED) == null || inputUserData.get(PROTECTED).toString().isEmpty()) {
                result.remove(PROTECTED);
            } else if (inputUserData.get(PROTECTED).toString().equalsIgnoreCase(RESOURCE_CHEKED)) {
                result.replace(PROTECTED, NATIVE_CHEKED);
            } else {
                result.remove(PROTECTED);
            }
        } else {

            if (inputUserData.get(EMAIL) == null || inputUserData.get(EMAIL).toString().isEmpty()) {
                if (currentUserData.get(EMAIL) == null || currentUserData.get(EMAIL).toString().isEmpty()) {
                    result.remove(EMAIL);
                } else {
                    result.put(EMAIL, currentUserData.get(EMAIL).toString());
                }
            }
            if (inputUserData.get(REALNAME) == null || inputUserData.get(REALNAME).toString().isEmpty()) {
                if (currentUserData.get(REALNAME) == null || currentUserData.get(REALNAME).toString().isEmpty()) {
                    result.remove(REALNAME);
                } else {
                    result.put(REALNAME, currentUserData.get(REALNAME).toString());
                }
            }
            if (inputUserData.get(ACCESSLEVEL) == null || inputUserData.get(ACCESSLEVEL).toString().isEmpty()) {
                if (currentUserData.get(ACCESSLEVEL) == null || currentUserData.get(ACCESSLEVEL).toString().isEmpty()) {
                    result.put(ACCESSLEVEL, DEFAULT_ACCESSLEVEL);
                } else {
                    result.put(ACCESSLEVEL, currentUserData.get(ACCESSLEVEL).toString());
                }
            }

            if (inputUserData.get(ENABLED) == null || inputUserData.get(ENABLED).toString().isEmpty()) {
                if (currentUserData.get(ENABLED) == null || currentUserData.get(ENABLED).toString().isEmpty()) {
                    result.remove(ENABLED);
                } else if (currentUserData.get(ENABLED).toString().equalsIgnoreCase(NATIVE_FORMCHEKED)) {
                    result.put(ENABLED, NATIVE_CHEKED);
                } else {
                    result.remove(ENABLED);
                }
            } else if (inputUserData.get(ENABLED).toString().equalsIgnoreCase(RESOURCE_CHEKED)) {
                result.replace(ENABLED, NATIVE_CHEKED);
            } else {
                result.remove(ENABLED);
            }

            if (inputUserData.get(PROTECTED) == null || inputUserData.get(PROTECTED).toString().isEmpty()) {
                if (currentUserData.get(PROTECTED) == null || currentUserData.get(PROTECTED).toString().isEmpty()) {
                    result.remove(PROTECTED);
                } else if (currentUserData.get(PROTECTED).toString().equalsIgnoreCase(NATIVE_FORMCHEKED)) {
                    result.put(PROTECTED, NATIVE_CHEKED);
                } else {
                    result.remove(PROTECTED);
                }
            } else if (inputUserData.get(PROTECTED).toString().equalsIgnoreCase(RESOURCE_CHEKED)) {
                result.replace(PROTECTED, NATIVE_CHEKED);
            } else {
                result.remove(PROTECTED);
            }
        }
        return result;
    }

    int getNumberOfUsers(String firstPagebodyhtml) {
        log.finest("firstPagebodyhtml body: " + firstPagebodyhtml);
        Pattern p = Pattern.compile(NUMBEROFUSERS_PATERN);
        Matcher m = p.matcher(firstPagebodyhtml);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        log.info("Number of users :matchedString: " + matchedString);
        return Integer.parseInt(matchedString);
    }

    void getUsersAttributes(String body) {
        log.finest("getUsersAttributes body: " + body);
        Pattern p = Pattern.compile(USERATTRIBUTES_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        while (m.find()) {
            matchedString = m.group(1);
            log.finest("Number of users :matchedString: " + matchedString);

            HashMap<String, List<String>> values = new HashMap<String, List<String>>();
            List<String> valuesList = new ArrayList<String>();

            //userid
            String userid = getValue(matchedString, REC_USERID_PATERN);
            valuesList.add(userid);
            values.put(OP_USERID, valuesList);
            valuesList = new ArrayList<String>();

            //username
            valuesList.add(getValue(matchedString, REC_USERNAME_PATERN));
            values.put(USERNAME, valuesList);
            valuesList = new ArrayList<String>();

            //email            
            valuesList.add(getValue(matchedString, REC_EMAIL_PATERN));
            values.put(EMAIL, valuesList);
            valuesList = new ArrayList<String>();

            //realname
            valuesList.add(getValue(matchedString, REC_REALNAME_PATERN));
            values.put(REALNAME, valuesList);
            valuesList = new ArrayList<String>();

            //enabled
            valuesList.add(getValue(matchedString, REC_ENABLED_PATERN));
            values.put(ENABLED, valuesList);
            valuesList = new ArrayList<String>();

            //protected
            valuesList.add(getValue(matchedString, REC_PROTECTED_PATERN));
            values.put(PROTECTED, valuesList);
            valuesList = new ArrayList<String>();

            //access
            valuesList.add(getValue(matchedString, REC_ACCESS_PATERN));
            values.put(ACCESSLEVEL, valuesList);
            valuesList = new ArrayList<String>();

            searchResult.put(userid, values);
        }

    }

    String getValue(String body, String pattern) {
        log.finest("getValue body: " + body);
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if (pattern.equals(REC_PROTECTED_PATERN)) {
            if (matchedString == null) {
                log.info("getValue :matchedString: " + RESOURCE_UNCHEKED);
                return RESOURCE_UNCHEKED;
            } else {
                matchedString = matchedString.replace("Protected", RESOURCE_CHEKED);
                log.info("getValue :matchedString: " + matchedString);
                return matchedString;
            }
        }

        if (matchedString == null) {
            log.info("getValue :matchedString: " + matchedString);
            return null;
        }
        if (matchedString != null && pattern.equals(REC_ENABLED_PATERN)) {
            matchedString = matchedString.replace(REC_EMPTY, RESOURCE_UNCHEKED).replace(REC_CHEKED, RESOURCE_CHEKED);
            log.info("getValue :matchedString: " + matchedString);
            return matchedString;
        }

        log.info("getValue :matchedString: " + matchedString);
        return matchedString;
    }

    public String getCreatePageToken(String body) {

        log.finest("getCreatePageToken body: " + body);
        Pattern p = Pattern.compile(CREATE_PAGETOKEN_PATERN);
        Matcher m = p.matcher(body);
        String matchedString = null;
        if (m.find()) {
            matchedString = m.group(1);
        }
        if (matchedString == null) {
            log.info("getCreatePageToken:matchedString: " + matchedString);
            return null;
        }
        log.info("getCreatePageToken:matchedString: " + matchedString);
        return matchedString;
    }

    public static String ldapGoupToAccLvl(String prefix, String joinedGroup, String delimiter) {
        String acclvl = null;
        int intAcc = 0;
        String viewer = "_viewer";
        String reporter = "_reporter";
        String manager = "_manager";
        String developer = "_developer";
        String updater = "_updater";
        String administrator = "_administrator";

        if (joinedGroup != null && prefix != null && delimiter != null) {
            int cLvl = 0;
            String[] gr = joinedGroup.toLowerCase().split(delimiter);            
            for (int i = 0; i < gr.length; i++) {
                if (gr[i].startsWith(prefix)) {                    
                    if (gr[i].contains(viewer)) {
                        cLvl = 10;
                    } else if (gr[i].contains(reporter)) {
                        cLvl = 25;
                    } else if (gr[i].contains(manager)) {
                        cLvl = 70;
                    } else if (gr[i].contains(developer)) {
                        cLvl = 55;
                    } else if (gr[i].contains(updater)) {
                        cLvl = 40;
                    } else if (gr[i].contains(administrator)) {
                        cLvl = 90;
                    } else {
                        cLvl = 5;
                    }
                    if (cLvl > intAcc) {
                        intAcc = cLvl;
                    }
                }
            }
            acclvl = String.valueOf(intAcc);
        }
        return acclvl;
    }

    String getUsename(String body) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
