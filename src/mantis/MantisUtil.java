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
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author onekriach
 */
public class MantisUtil {

    public static void main(String[] args) throws IOException {
        String url = "<do login URL>";
        HTMLutils htmlOutput = new HTMLutils();
        htmlOutput.doAuthenticationMantis(url, "<login>", "<password>");
        String body = htmlOutput.getHttpBody("get URL 1");

        CookieManager manager = htmlOutput.getManager();
        CookieStore cookieJar = manager.getCookieStore();
        List<HttpCookie> cookies = cookieJar.getCookies();
        for (HttpCookie cookie : cookies) {
            System.out.println(cookie);
        }
        System.out.println("body: " + body);
    }
}
