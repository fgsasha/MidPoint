
import hrdata.HTMLutils;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.Base64;
import java.util.List;
import mantis.MantisUtil;
import org.apache.commons.codec.binary.StringUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author onekriach
 */
public class TestMantisUtil {
    public String getUserInfo() throws IOException{
        String url = "<URL1>";
        HTMLutils htmlOutput = new HTMLutils();
        htmlOutput.doAuthenticationMantis(url, "<Username1>", "<Pass1>");
        String username="a.ivasko";
        String body = new String(htmlOutput.getHttpBody("<URL2>"+username));

//        CookieManager manager = htmlOutput.getManager();
//        CookieStore cookieJar = manager.getCookieStore();
//        List<HttpCookie> cookies = cookieJar.getCookies();
//        for (HttpCookie cookie : cookies) {
//            System.out.println(cookie);
//        }
//        System.out.println("body: " + body);
    return body;
    }
    
    public static void main(String[] args) throws IOException {
        MantisUtil mantis = new mantis.MantisUtil();
        TestMantisUtil test = new TestMantisUtil();
        String body=test.getUserInfo();
//        Test body
//        String body64="";
//        byte[] decoded = Base64.getDecoder().decode(body64);
//        String body=new String(decoded);
        mantis.getEnabled(body);
        mantis.getProtected(body);
        mantis.getAccessLevel(body);
        mantis.getUpdateToken(body);
        mantis.getUserId(body);
    }
}
