/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata.emc;

import hrdata.HTMLutils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONObject;

/**
 *
 * @author o.nekriach
 */
public class RESTapi {

    private static void initializeClientCertParam(String emcClientCertificateType, String emcClientCertificateFile, String emcClientCertificateSecret) throws UnsupportedEncodingException {

        System.setProperty("javax.net.ssl.storetype", emcClientCertificateType);
        System.setProperty("javax.net.ssl.keyStore", emcClientCertificateFile);
        System.setProperty("javax.net.ssl.keyStorePassword", emcClientCertificateSecret);

        //   System.setProperty("javax.net.debug", "ssl");
    }

    void openConnection(String url, String credentials) {
//TODO
    }

    void closeConnection() {
//TODO
    }

    /**
     * Old method to call function in EMC (should be refactored) and put to the
     * cache
     *
     * @param url - https://example.com/jsonService.php
     * @param service - API version e.g 16
     * @param function - function name e.g. getUsers
     * @param credentials - json array with credentials e.g.
     * {"login":"iam","token":"SOMETOKEN","project":"IAM"}
     * @param params - json array with funcrion parameters e.g.
     * {"id":"21504","showDeactivated":"full","useCache":"0"}
     * @return - json string
     * @throws IOException
     */
    JSONObject callAPIFunction(String url, String service, String function, String credentials, String params) throws IOException {
        JSONObject output = null;
        // At this moment is using old html query that use all parameters in one GET row
        HTMLutils htmlUtil = new HTMLutils();
        if (url == null) {
            //TODO rise exception
        } else if (service != null && function != null && credentials != null && params != null) {
            url = url + "?" + "service=" + service + "&function=" + function + "&credentials=" + credentials + "&params=" + params;
            url = URLEncoder.encode(url, "UTF-8");
            String responseOutput = htmlUtil.readFromUrl(url);
            // Check if responseOutput is json valid format
            output = new JSONObject(responseOutput);
            Cache.putIntoJsonCache(service, output);
        } else {
            String responseOutput = htmlUtil.readFromUrl(url);
            // Check if responseOutput is json valid format
            output = new JSONObject(responseOutput);
            if (url.contains(Constant.GETUSERS)) {
                Cache.putIntoJsonCache(Constant.GETUSERS, output);
            } else if (url.contains(Constant.GETTEAMS)) {
                Cache.putIntoJsonCache(Constant.GETTEAMS, output);
            }
        }
        return output;
    }

}
