/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myhomeproject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author onekriach
 */
public class HTMLutils {

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public String readFromUrl(String texturl) throws IOException, JSONException {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        System.out.println("URL: " + texturl);

        URL url = new URL(texturl);
        InputStream is = null;
        String jsonText = null;
        try {

            if (texturl.startsWith("https")) {
                HttpsURLConnection urlConnection;
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setHostnameVerifier(hostnameVerifier);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                is = urlConnection.getInputStream();
            } else {
                HttpURLConnection urlConnection;
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                is = urlConnection.getInputStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonText = new String(readAll(rd));
            rd.close();

            return jsonText;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        HTMLutils htmlOutput = new HTMLutils();

        String readFromUrl = htmlOutput.readFromUrl("https://");
        System.out.println("readFromUrl: " + readFromUrl);
    }

}
