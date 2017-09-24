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
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private Boolean wholeFile = Boolean.TRUE;
    private String delimiter = ",";
    private Integer numberOfrows = 5;
    private String[] returnArray = new String[numberOfrows + 1];
    private String filterFieldName = "PID";
    private String filterValues = "00000,00001";

    public void setFilterFieldName(String filterFieldName) {
        this.filterFieldName = filterFieldName;
    }

    public void setFilterValues(String filterValues) {
        this.filterValues = filterValues;
    }

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
        
        String csvStringValues = "";
        String[] filterFieldnames = filterFieldName.split(delimiter);
        Boolean checkResult = Boolean.FALSE;
        Array returnArrayList;
        
        String pid="";
        Map hmap = new HashMap<String,String>();     

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

        if (this.wholeFile) {
            returnArray = new String[array.length() + 1];
            this.numberOfrows = array.length();
        }

        returnArray[0] = csvKeysString;
        hmap.put("csvFieldNames", csvKeysString);


        for (int i = 0; i < numberOfrows; i++) {

            JSONObject arrayJson = array.getJSONObject(i);

            if (filterFieldName.equalsIgnoreCase("*") || filterValues.equalsIgnoreCase("*")) {
                checkResult = true;
            } else {

                for (int s = 0; s < filterFieldnames.length; s++) {
                    //System.out.println(filterFieldnames[s].toString());
                    String checkedValue = arrayJson.get(filterFieldnames[s].toString()).toString();
                    //System.out.println(checkedValue);
                    Boolean innerCheckResult = this.checkInList(checkedValue, filterValues, delimiter);
                    if (innerCheckResult) {
                        checkResult = innerCheckResult;
                    }
                }
            }
            if (checkResult) {
                // System.out.println(checkedValue);

                for (int p = 0; p < keys.length; p++) {
                    pid=arrayJson.get("PID").toString();
                    csvStringValues = csvStringValues + arrayJson.get(keys[p].toString().trim()).toString();
                    if (p < keys.length - 1) {
                        csvStringValues = csvStringValues + delimiter;
                    }

                }
            }

            //добавляем в хеш мап не пустые csv строки и ключ hrmid(pid)
            if (csvStringValues.isEmpty() == false) {
                System.out.println(pid+": "+csvStringValues);
                hmap.put(pid, csvStringValues);
                //System.out.println(csvStringValues);
            }
            returnArray[i + 1] = csvStringValues;

            //Сбрасываем все в начальные значения
            csvStringValues = "";
            checkResult = false;
            pid="";
        }

        List<String> list = new ArrayList<String>();

        for (String s : returnArray) {
            if (s != null && s.length() > 0) {
                list.add(s);
            }
        }

        returnArray = list.toArray(new String[list.size()]);

        return (String[]) returnArray;
    }

    public String[] advanceJsonToArray() throws IOException {

        JSONObject obj = new JSONObject(this.StringFromStream());
        JSONObject objGetByKey = obj.getJSONObject("result");
        String[] ks = objGetByKey.keySet().toArray(new String[0]);

        JSONObject firstRow = (JSONObject) objGetByKey.get("1");

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

        if (this.wholeFile) {
            returnArray = new String[ks.length + 1];
            this.numberOfrows = ks.length;
        }

        returnArray[0] = csvKeysString;

        for (int i = 0; i < ks.length; i++) {
            //System.out.println(ks[i]);

            JSONObject obj2 = (JSONObject) objGetByKey.get(ks[i]);
            if (obj2.isNull("password") == false) {
                String csvStringValues = "";
                for (int p = 0; p < keys.length; p++) {

                    String toAdd = obj2.get(keys[p].toString().trim()).toString();
                    String mainEmail;
                    if (keys[p].equalsIgnoreCase("emails")) {
                        mainEmail = getMainEmail(toAdd);

                    }

                    if (toAdd.contains(",")) {
                        toAdd = toAdd.replaceAll(",", "+");
                        //System.out.println("FIND!!!");
                    }
                    System.out.println(keys[p]);
                    if (keys[p].equalsIgnoreCase("extensions") || keys[p].equalsIgnoreCase("settings")) {
                        toAdd = "";
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
        String[] k = this.jsonToArray();
        //String[] k = this.advanceJsonToArray();
        System.out.println(k.length);
        for (int i = 0; i < k.length; i++) {
            if (k[i] != null) {
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

    Boolean checkInList(String checkedValue, String inputString, String delimiter) {
        Boolean result = Boolean.FALSE;
        if (checkedValue == null || checkedValue.isEmpty()) {
            result = false;
        } else {
            String[] arrayInputData = inputString.split(delimiter);
            for (int i = 0; i < arrayInputData.length; i++) {
                if (arrayInputData[i].equalsIgnoreCase(checkedValue)) {
                    return true;
                } else {
                    result = false;

                }

            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        JSONparser json = new JSONparser();
        json.setInputJSONFilePath("/home/onekriach/Downloads/work/HRM_json_All_users2.json");
        json.setOutputCSVFilePath("/home/onekriach/Downloads/work/HRM_csv_All_users.csv");
        json.setFilterFieldName("emailPrimary,emailWork");
        json.setFilterValues("anderson.a@itncorp.com,ashton.s@airconcierge.com,caleb.s@itncorp.com,calvin.i@airconcierge.com,cj.g@airconcierge.com,darel.a@itncorp.com,dex.s@airconcierge.com,emma.s@itncorp.com,Felix.n@itncorp.com,gordon.r@itncorp.com,jd.a@asaptickets.com,fred@skyluxtravel.com,cody@skyluxtravel.com,matthew@skyluxtravel.com,jefferson.s@airconcierge.com,kevin.s@itncorp.com,leon.s@itncorp.com,medo.c@asaptickets.com,melvin.a@itncorp.com,mike.h@itncorp.com,mo.b@asaptickets.com,otis.f@airconcierge.com,payton.m@itncorp.com,roger.m@airconcierge.com,roy.v@itncorp.com,sergio.g@itncorp.com,tyrion.i@airconcierge.com,walden.m@airconcierge.com,anete.s@itncorp.com,Mara.p@itncorp.com,derek.j@airconcierge.com,mercedes.z@airconcierge.com,bree.r@airconcierge.com,miguel.p@airconcierge.com,parvesh.k@airconcierge.com,irwin.a@airconcierge.com,molly.m@airconcierge.com,arnold.l@itncorp.com,Larry.r@airconcierge.com,fernando.m@airconcierge.com,sri.k@airconcierge.com,gideon.f@airconcierge.com,patrick.a@airconcierge.com,lauris.e@itncorp.com,robert.b@airconcierge.com,Ralph@skyluxtravel.com,silvester@skyluxtravel.com,tom@skyluxtravel.com,chandler.m@itncorp.com,nicolas.p@itncorp.com,ted.s@itncorp.com,Marcel.M@itncorp.com ,quentin.d@airconcierge.com ,jabez.a@airconcierge.com,steve.g@itncorp.com,justin.b@itncorp.com,adrian.a@itncorp.com,chelsea.a@itncorp.com,rd.o@airconcierge.com,suzie.g@airconcierge.com,asia.a@airconcierge.com,bars.b@airconcierge.com,adam.i@itncorp.com,ramon.p@airconcierge.com,isabel.a@airconcierge.com");
        //json.setFilterFieldName("*");
        //json.setFilterValues("*");
        //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export_cut.json");
        //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export.json");
        //json.setOutputCSVFilePath("/home/onekriach/Downloads/work/EMC_export.csv");
        json.wholeFile = Boolean.TRUE;
        json.toCSVFile();
        System.out.println("Done!!");
    }
}
