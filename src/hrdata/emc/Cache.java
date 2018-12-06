/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata.emc;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author o.nekriach
 */
public class Cache {

    static private Map<String, JSONObject> jsonCache = new HashMap<String, JSONObject>();
    static private Map<String, String> teamsNamesCache = new HashMap<String, String>();

    public static void setJsonCache(Map<String, JSONObject> jsonCache) {
        Cache.jsonCache = jsonCache;
    }

    public static void setTeamsNamesCache(Map<String, String> teamsNamesCache) {
        Cache.teamsNamesCache = teamsNamesCache;
    }

    public static Map<String, JSONObject> getJsonCache() {
        return jsonCache;
    }

    public static Map<String, String> getTeamsNamesCache() {
        return teamsNamesCache;
    }

    static public void putIntoJsonCache(String key, JSONObject value) {
        jsonCache.put(key, value);
    }

    static public JSONObject getFromJsonCache(String key) {
        return jsonCache.get(key);
    }
    
    static public JSONObject removeFromJsonCache(String key) {
        return jsonCache.remove(key);
    }
    
    
}
