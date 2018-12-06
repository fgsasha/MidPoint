/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata.emc;

import java.io.IOException;
import org.json.JSONObject;

/**
 *
 * @author o.nekriach
 */
public class FunctionsCall {

    private final RESTapi rest;

    public FunctionsCall() {
        rest = new RESTapi();
    }

    public JSONObject getUsers(String url, String service, String function, String credentials, String params) throws IOException {
        if (Cache.getJsonCache().containsKey(Constant.GETUSERS)) {
            return Cache.getFromJsonCache(Constant.GETUSERS);
        } else {
            JSONObject obj = rest.callAPIFunction(url, service, function, credentials, params);
            if (obj != null && obj.getString(Constant.STATUS).equals(Constant.OK)) {
                return obj;
            }
        }
        return null;
    }

    public JSONObject getTeams(String url, String service, String function, String credentials, String params) throws IOException {
        if (Cache.getJsonCache().containsKey(Constant.GETTEAMS)) {
            return Cache.getFromJsonCache(Constant.GETTEAMS);
        } else {
            JSONObject obj = rest.callAPIFunction(url, service, function, credentials, params);
            if (obj != null && obj.getString(Constant.STATUS).equals(Constant.OK)) {
                return obj;
            }
        }
        return null;
    }
}
