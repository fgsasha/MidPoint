/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.json.JSONException;

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
        //System.out.println("URL: " + texturl);

        URL url = new URL(texturl);
        InputStream is = null;
        String jsonText = null;
        HttpsURLConnection urlConnectionHttp = null;
        HttpURLConnection urlConnectionHttps = null;
        try {

            if (texturl.startsWith("https")) {

                urlConnectionHttp = (HttpsURLConnection) url.openConnection();
                urlConnectionHttp.setHostnameVerifier(hostnameVerifier);
                urlConnectionHttp.setRequestProperty("Content-Type", "application/json");
                is = urlConnectionHttp.getInputStream();
                //urlConnection.disconnect();
            } else {
                urlConnectionHttps = (HttpURLConnection) url.openConnection();
                urlConnectionHttps.setRequestProperty("Content-Type", "application/json");
                is = urlConnectionHttps.getInputStream();
                //urlConnection.disconnect();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonText = new String(readAll(rd));
            rd.close();

            return jsonText;
        } finally {
            if (urlConnectionHttp != null) {
                urlConnectionHttp.disconnect();
            }
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
            if (is != null) {
                is.close();

            }
        }
    }

    void connectToJIRA(String connectigURL) throws MalformedURLException, ProtocolException, IOException {
    }

    public static void main(String[] args) throws IOException {

//    System.setProperty("javax.net.ssl.storetype", "pks12");
//    System.setProperty("javax.net.ssl.keyStore", "/home/onekriach/Documents/IAM/EMC/shorttermOleksandr.Nekriach.001.p12");
//    System.setProperty("javax.net.ssl.keyStorePassword", "TODO");
//    System.setProperty("javax.net.debug", "ssl");
        HTMLutils htmlOutput = new HTMLutils();
        htmlOutput.connectToJIRA("");

    }

}
