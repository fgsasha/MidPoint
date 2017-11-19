/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata;

import com.sun.security.ntlm.Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static javax.management.Query.gt;
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
                //urlConnection.disconnect();
            } else {
                HttpURLConnection urlConnection;
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                is = urlConnection.getInputStream();
                //urlConnection.disconnect();
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

    void connectToJIRA(String connectigURL) throws MalformedURLException, ProtocolException, IOException
    {
//    String data = "{ \"fields\": { \"assignee\":{\"name\":\"admin\"} }}";
//    URL jiraURL;
//    //jiraURL= new URL("http://localhost:8080/rest/api/2/issue/TEST-12") ;
//    jiraURL= new URL("https://dyninno.atlassian.net/rest/api/2/issue/IAM-1");
//            HttpURLConnection connection = (HttpURLConnection)jiraURL.openConnection();
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Accept", "*/*");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setDoOutput(true);
//            connection.setDoInput(true);
//            String userCredentials = "onekriach@dynatech.lv:dyninnoFyyeirf1980!";
//            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
//            connection.setRequestProperty ("Authorization", basicAuth);
//            connection.connect();
//            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream(),StandardCharsets.UTF_8);
//            wr.write(data.toString());
//           wr.flush();
//           wr.close();
//           Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
//            for (int c; (c = in.read()) >= 0; System.out.print((char)c));

    }
    
    public static void main(String[] args) throws IOException {

//    System.setProperty("javax.net.ssl.storetype", "pks12");
//    System.setProperty("javax.net.ssl.keyStore", "/home/onekriach/Documents/IAM/EMC/shorttermOleksandr.Nekriach.001.p12");
//    System.setProperty("javax.net.ssl.keyStorePassword", "TODO");
//    System.setProperty("javax.net.debug", "ssl");
        
        
        
        HTMLutils htmlOutput = new HTMLutils();
        htmlOutput.connectToJIRA("");

        //String readFromUrl = htmlOutput.readFromUrl("https://dyninno.atlassian.net/secure/MyJiraHome.jspa");
        //System.out.println("readFromUrl: " + readFromUrl);
    }

}
