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
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import mantis.MantisUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

/**
 *
 * @author o.nekriach
 */
public class HTMLutils {

    private String urlParameters;

    public void setUrlParameters(String urlParameters) {
        this.urlParameters = urlParameters;
    }

    public void HTMLutils() {
        log.setLevel(Level.INFO);
    }

    Logger log = Logger.getLogger(MantisUtil.class.getName());

    private CookieManager manager = new CookieManager();
    static final String COOKIES_HEADER = "Set-Cookie";
    static final String COOKIE_REQUEST_HEADER = "Cookie";
    private HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

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
     *
     * @param inputUrl - URL address
     * @param username - username to do authentication
     * @param passw - password to do authentication
     * @throws IOException
     * @throws JSONException
     */
    public void doAuthenticationMantis(String inputUrl, String username, String passw) throws IOException, JSONException {
        URL url = new URL(inputUrl);
        InputStream is = null;
        HttpsURLConnection urlConnectionHttps = null;
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
            if (texturl.startsWith("https")) {
                urlConnectionHttps = (HttpsURLConnection) url.openConnection();
                urlConnectionHttps.setHostnameVerifier(hostnameVerifier);
                urlConnectionHttps.setRequestMethod("GET");
                this.populateCookieHeaders(urlConnectionHttps);
                is = urlConnectionHttps.getInputStream();
                Map<String, List<String>> headerFields = urlConnectionHttps.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        manager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                body = new String(readAll(rd));
                rd.close();

                return body;
            } else {
                throw new VerifyError("http protocol is not supported yet");
            }
        } finally {
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
        }
    }

    public String postHttpBody(String texturl, Map parameters) throws IOException, JSONException {
        URL url = new URL(texturl);
        InputStream is = null;
        String body = null;
        HttpsURLConnection urlConnectionHttps = null;
        try {
            if (texturl.startsWith("https")) {
                urlConnectionHttps = (HttpsURLConnection) url.openConnection();
                urlConnectionHttps.setHostnameVerifier(hostnameVerifier);
                String urlParameters = this.postParametersFromMap(parameters);
                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                log.info("urlParameters " + urlParameters);
                urlConnectionHttps.setDoOutput(true);
                urlConnectionHttps.setInstanceFollowRedirects(false);
                urlConnectionHttps.setRequestMethod("POST");
                urlConnectionHttps.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnectionHttps.setRequestProperty("charset", "utf-8");
                urlConnectionHttps.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                urlConnectionHttps.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0");
                urlConnectionHttps.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                urlConnectionHttps.setUseCaches(false);
                urlConnectionHttps.setDoOutput(true);
                urlConnectionHttps.setDoInput(true);
                this.populateCookieHeaders(urlConnectionHttps);
                OutputStreamWriter writer = new OutputStreamWriter(urlConnectionHttps.getOutputStream());
                writer.write(urlParameters);
                writer.flush();
                writer.close();
                Map<String, List<String>> headerFields = urlConnectionHttps.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        manager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }
                is = urlConnectionHttps.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                body = new String(readAll(rd));
                log.finest(body);
                log.info("Responce msg: " + urlConnectionHttps.getResponseMessage());
                return body;

            } else {
                throw new VerifyError("http protocol is not supported yet");
            }

        } finally {
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
        }
    }

    public void populateCookieHeaders(HttpURLConnection conn) {

        if (this.manager != null) {
            //getting cookies(if any) and manually adding them to the request header
            List<HttpCookie> cookies = this.manager.getCookieStore().getCookies();
            log.finest("populate cookies: " + cookies);
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

    String postParametersFromMap(Map inputData) {
        String result = "";
        String parameterDelimiter = null;
        if (inputData == null) {
            return null;
        }
        Iterator it = inputData.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String value = (String) inputData.get(key);
            if (value.contains("&")) {
                throw new VerifyError("Invalid parameter. POST parameter cannot contain & symbol in : " + value);
            }
            if (result.isEmpty()) {
                parameterDelimiter = "";
            } else {
                parameterDelimiter = "&";
            }
            result = result + parameterDelimiter + key + "=" + value;
        }
        log.info("POST parameters: " + result);
        return result;
    }
    
        public String getLookerLoginBody(String texturl) throws IOException, JSONException {
        URL url = new URL(texturl);
        InputStream is = null;
        String body = null;
        HttpsURLConnection urlConnectionHttps = null;
        try {
            if (texturl.startsWith("https")) {
                urlConnectionHttps = (HttpsURLConnection) url.openConnection();
                urlConnectionHttps.setHostnameVerifier(hostnameVerifier);
                urlConnectionHttps.setRequestMethod("GET");
                urlConnectionHttps.setRequestProperty("Content-Type", "text/html;charset=utf-8");
                urlConnectionHttps.setRequestProperty("charset", "utf-8");
                urlConnectionHttps.setUseCaches(false);
                urlConnectionHttps.setDoOutput(true);
                this.populateCookieHeaders(urlConnectionHttps);
                is = urlConnectionHttps.getInputStream();
                Map<String, List<String>> headerFields = urlConnectionHttps.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        manager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                body = new String(readAll(rd));
                rd.close();

                return body;
            } else {
                throw new VerifyError("http protocol is not supported yet");
            }
        } finally {
            if (urlConnectionHttps != null) {
                urlConnectionHttps.disconnect();
            }
        }
    }
    
    public void doAuthenticationLooker(String inputUrl, String username, String passw) throws IOException, JSONException {
        URL url = new URL(inputUrl);
        InputStream is = null;
        HttpsURLConnection urlConnectionHttps = null;
        try {

            if (inputUrl.startsWith("https")) {

                urlConnectionHttps = (HttpsURLConnection) url.openConnection();
                urlConnectionHttps.setHostnameVerifier(hostnameVerifier);
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
    
 String retrieveCookie(URL url) throws IOException, URISyntaxException
{
     String cookieValue = null;

     CookieHandler handler = CookieHandler.getDefault();
     if (handler != null)    {
          Map<String, List<String>> headers = handler.get(url.toURI(), new HashMap<String,List<String>>());
          List<String> values = headers.get("Cookie");
          for (Iterator<String> iter=values.iterator(); iter.hasNext();) {
               String v = iter.next();

               if (cookieValue == null)
                    cookieValue = v;
               else
                    cookieValue = cookieValue + ";" + v;
          }
     }
     return cookieValue;
} 

}
