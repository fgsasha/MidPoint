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
import java.net.URL;
import java.nio.charset.Charset;
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

  public String readFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      return jsonText;
    } finally {
      is.close();
    }
  }
      
    
    
    
    
        public static void main(String[] args) throws IOException {
        HTMLutils htmlOutput = new HTMLutils();
 
        String readFromUrl = htmlOutput.readFromUrl("https://github.com/mdn/learning-area/blob/master/accessibility/aria/quotes.json");
            System.out.println("readFromUrl: "+readFromUrl);
    }
    
    
    
    
}
