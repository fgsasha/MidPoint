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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

/**
 *
 * @author onekriach
 */
public class HTMLutils {

    private CookieManager manager = new CookieManager();
    static final String COOKIES_HEADER = "Set-Cookie";
    static final String COOKIE_REQUEST_HEADER = "Cookie";

    public CookieManager getManager() {
        return manager;
    }

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

    /**
     * Do authentication and fill up manager with cookies
     * @param inputUrl - URL address
     * @param username - username to do authentication
     * @param passw - password to do authentication
     * @throws IOException
     * @throws JSONException
     */
    public void doAuthenticationMantis(String inputUrl, String username, String passw) throws IOException, JSONException {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        URL url = new URL(inputUrl);
        InputStream is = null;
        HttpsURLConnection urlConnectionHttps = null;
        //String cookiesHeader;
        //final String COOKIES_HEADER = "Set-Cookie";
        try {

            if (inputUrl.startsWith("https")) {

                urlConnectionHttps = (HttpsURLConnection) url.openConnection();
                urlConnectionHttps.setHostnameVerifier(hostnameVerifier);

                String urlParameters = "return=index.php&username=" + username + "&password=" + passw;
                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                urlConnectionHttps.setDoOutput(true);
                urlConnectionHttps.setInstanceFollowRedirects(false);
                urlConnectionHttps.setRequestMethod("POST");
                urlConnectionHttps.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnectionHttps.setRequestProperty("charset", "utf-8");
                urlConnectionHttps.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                urlConnectionHttps.setUseCaches(false);
                urlConnectionHttps.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnectionHttps.getOutputStream());

                writer.write(urlParameters);
                writer.flush();
//                String line;
//                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnectionHttps.getInputStream()));
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }

                //CookieHandler.setDefault(manager);
                Map<String, List<String>> headerFields = urlConnectionHttps.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        manager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }

                    writer.close();
                    //reader.close();

                } else {
                    throw new RuntimeException("Using unsecure  protocol for authentication: " + inputUrl);
                }
                //  return cookiesHeader;
            }
        } finally {
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
        }
    }

    public String getHttpBody(String texturl) throws IOException, JSONException {
        URL url = new URL(texturl);
        InputStream is = null;
        String body = null;
        HttpsURLConnection urlConnectionHttps = null;
        try {
            urlConnectionHttps = (HttpsURLConnection) url.openConnection();
            urlConnectionHttps.setRequestMethod("GET");
            this.populateCookieHeaders(urlConnectionHttps);
            is = urlConnectionHttps.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            body = new String(readAll(rd));
//            String line;
//            while ((line = rd.readLine()) != null) {
//                System.out.println(line);
//            }
            rd.close();

            return body;
        } finally {
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
        }
    }

    public void populateCookieHeaders(HttpURLConnection conn) {

        if (this.manager != null) {
            //getting cookies(if any) and manually adding them to the request header
            List<HttpCookie> cookies = this.manager.getCookieStore().getCookies();
            if (cookies != null) {
                if (cookies.size() > 0) {
                    //adding the cookie header
                    conn.setRequestProperty(COOKIE_REQUEST_HEADER, StringUtils.join(cookies.toArray(), ";"));
                }
            }
        }
    }

    public void disconnect(HttpsURLConnection conn) {
        conn.disconnect();
    }
  
}
