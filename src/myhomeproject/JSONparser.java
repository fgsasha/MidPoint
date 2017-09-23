/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myhomeproject;

import java.awt.Event;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.*;

/**
 *
 * @author onekriach
 */
public class JSONparser {

    private String inputJSONFilePath;
    private String outputCSVFilePath;
    private Boolean wholeFile=Boolean.TRUE;
    private String delimiter=",";
    private Integer numberOfrows=5;
    private String[] returnArray = new String[numberOfrows+1];
    private String filterFieldName="PID";
    private String filterValues="00000,00001";

    public void setNumberOdRows(Boolean numberOdRows) {
        this.wholeFile = numberOdRows;
    }

    public void setOutputCSVFilePath(String outputCSVFilePath) {
        this.outputCSVFilePath = outputCSVFilePath;
    }


    public void setInputJSONFilePath(String filePath) {
        this.inputJSONFilePath = filePath;
    }



    String StringFromStream() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(inputJSONFilePath)));
        return content;

    }

    public String[] jsonToArray() throws IOException {

        JSONObject obj = new JSONObject(this.StringFromStream());

        JSONArray array = obj.getJSONArray("records");
        
        

        JSONObject firstRow = array.getJSONObject(0);
        Iterator<String> keySetIter = firstRow.keys();

        String csvKeysString = "";
        String keySetIternextValue = "";
        String[] keys = new String[firstRow.keySet().size()];
        int k = 0;
        while (keySetIter.hasNext()) {
            keySetIternextValue = keySetIter.next();

            keys[k] = keySetIternextValue.toString();

            csvKeysString = csvKeysString + keySetIternextValue;
            if (keySetIter.hasNext()) {
                csvKeysString = csvKeysString + delimiter;
            }
            k = k + 1;
        }
        //System.out.println(csvKeysString);
        
        
        
        
        if (this.wholeFile){
        returnArray = new String[array.length() + 1];
        this.numberOfrows=array.length();
        } 
        
        returnArray[0] = csvKeysString;
        
        for (int i = 0; i < numberOfrows; i++) {

            JSONObject arrayJson = array.getJSONObject(i);
            String csvStringValues = "";

            for (int p = 0; p < keys.length; p++) {

                csvStringValues = csvStringValues + arrayJson.get(keys[p].toString().trim()).toString();
                if (p < keys.length - 1) {
                    csvStringValues = csvStringValues + delimiter;
                }

            }
            //System.out.println(csvStringValues);
            returnArray[i + 1] = csvStringValues;

        }
        return returnArray;
    }

    public String[] advanceJsonToArray() throws IOException {

        JSONObject obj = new JSONObject(this.StringFromStream());
        JSONObject objGetByKey = obj.getJSONObject("result");
        String[] ks = objGetByKey.keySet().toArray(new String[0]);
 
        
        
        JSONObject firstRow=(JSONObject) objGetByKey.get("1");
      
        Iterator<String> keySetIter = firstRow.keys();

        String csvKeysString = "";
        String keySetIternextValue = "";
        String[] keys = new String[firstRow.keySet().size()];
        int k = 0;
        while (keySetIter.hasNext()) {
            keySetIternextValue = keySetIter.next();

            keys[k] = keySetIternextValue.toString();

            csvKeysString = csvKeysString + keySetIternextValue;
            if (keySetIter.hasNext()) {
                csvKeysString = csvKeysString + delimiter;
            }
            k = k + 1;
        }
        System.out.println(csvKeysString);

       
if (this.wholeFile){
returnArray = new String[ks.length + 1];
this.numberOfrows=ks.length;
        }
        
returnArray[0] = csvKeysString;
        
        
      for(int i=0; i < ks.length;i++){
          //System.out.println(ks[i]);
           
           JSONObject obj2=(JSONObject) objGetByKey.get(ks[i]);
           if(obj2.isNull("password")==false){ 
           String csvStringValues = "";
           for (int p = 0; p < keys.length; p++) {
               
               String toAdd=obj2.get(keys[p].toString().trim()).toString();
               String mainEmail;
                  if(keys[p].equalsIgnoreCase("emails")){
               mainEmail = getMainEmail(toAdd);
               
               }
               
               if(toAdd.contains(",")){
               toAdd=toAdd.replaceAll(",","+");
                   //System.out.println("FIND!!!");
               }
               System.out.println(keys[p]);
               if(keys[p].equalsIgnoreCase("extensions") || keys[p].equalsIgnoreCase("settings")){
               toAdd="";
               }
            
               
               csvStringValues = csvStringValues + toAdd;
               
                if (p < keys.length - 1) {
                csvStringValues = csvStringValues + delimiter;
           }
            
           }
           //System.out.println(csvStringValues);
           returnArray[i + 1] = csvStringValues;
           }            
        }

 return returnArray;
    }
    
    
    
    private void toCSVFile() throws IOException {

        PrintWriter writer = new PrintWriter(this.outputCSVFilePath, "UTF-8");
        //String[] k = this.jsonToArray();
        String[] k = this.advanceJsonToArray();
        System.out.println(k.length);
        for (int i = 0; i < k.length; i++) {
            if(k[i]!=null){
            writer.println(k[i]);
        }
        }
        writer.close();

    }


    private String getMainEmail(String toAdd) {
        JSONObject obj = new JSONObject(toAdd);
        System.out.println(obj.toString());
        String keySetIternextValue = "";
        Iterator<String> keySetIter = obj.keys();     
        String[] keys = new String[obj.keySet().size()];
        int k = 0;
        while (keySetIter.hasNext()) {
            keySetIternextValue = keySetIter.next();
            keySetIternextValue.toString();
           
            System.out.println(
                    obj.get(keySetIternextValue.toString())
            );
            
       
        
        
        
        }
        return obj.toString();
    }

    
    



    public static void main(String[] args) throws IOException {
        JSONparser json = new JSONparser();
        //json.setInputJSONFilePath("/home/onekriach/Downloads/work/HRM_json_All_users2.json");
        //json.setOutputCSVFilePath("/home/onekriach/Downloads/work/HRM_csv_All_users.csv");
        json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export_cut.json");
        //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export.json");
        json.setOutputCSVFilePath("/home/onekriach/Downloads/work/EMC_export.csv");
        json.wholeFile=Boolean.TRUE;
        json.toCSVFile();
        System.out.println("Done!!");
    }
}
        
