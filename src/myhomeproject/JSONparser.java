/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myhomeproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.DataFormatException;

/**
 *
 * @author onekriach
 */
public class JSONparser {

    private String sourceHRData = "HRM-EMC";
    private String inputJSONFilePath;
    private String outputCSVFilePath;
    private String emcJsonFile;
    private String emcCSVFile;
    private String hrmJsonFile;
    private String hrmCSVFile;
    private Boolean wholeFile = Boolean.TRUE;
    private String delimiter = ",";
    private String replaceSymbol = "~";
    private Integer numberOfrows = 5;
    private String[] returnArray = new String[numberOfrows + 1];
    private String filterFieldName = "*"; // "PID";
    private String hrmFilterFieldName = "*";
    private String emcFilterFieldName = "*";
    private String filterValues = "*"; //"00000,00001";
    private String hrmFilterValues = "*";
    private String emcFilterValues = "*";
    private Map jsonMapEMC = new HashMap< String, String>();
    private Map jsonMapHRM = new HashMap< String, String>();
    private String[] outputArray;

    public JSONparser(String direction, String emcJsonFile, String hrmJsonFile, String filterFieldName, String filterValues) {
        //Офлайн Конструктор для чтения из фалов
        DateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String currentDate = dateFormat.format(date);
        this.wholeFile = Boolean.TRUE;
        this.setSourceHRData(direction);//EMC-HRM, EMC, HRM
        this.setEmcJsonFile(emcJsonFile);
        if (emcJsonFile.contains("*")) {
            this.setEmcCSVFile(emcJsonFile.replace(new File(emcJsonFile).getName(), "emc_export_from_multiple_files" + currentDate + ".csv"));
        } else {
            this.setEmcCSVFile(emcJsonFile.replace(".json", currentDate + ".csv"));
        }
        this.setHrmJsonFile(hrmJsonFile);
        this.setHrmCSVFile(hrmJsonFile.replace(".json", currentDate + ".csv"));
        this.setFilterFieldName(filterFieldName);
        this.setFilterValues(filterValues);
    }

    public JSONparser(String direction, String emcJsonFile, String hrmURL, String hrmOUTCSVFile, String filterFieldName, String filterValues) throws DataFormatException,IOException {
        //Онлайн конструктор для чтения HRM-JSON  из URL и чтение EMC-JSON из фалов
        DateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String currentDate = dateFormat.format(date);
        this.wholeFile = Boolean.TRUE;
        this.setSourceHRData(direction);//EMC-HRM, EMC, HRM
        this.setEmcJsonFile(emcJsonFile);
        if (emcJsonFile.contains("*")) {
            this.setEmcCSVFile(emcJsonFile.replace(new File(emcJsonFile).getName(), "emc_export_from_multiple_files" + currentDate + ".csv"));
        } else {
            this.setEmcCSVFile(emcJsonFile.replace(".json", currentDate + ".csv"));
        }
        this.setHrmJsonFile(hrmOUTCSVFile.replace(".csv", ".json.TMP"));
        this.getJSONFromURL(hrmURL, getHrmJsonFile());
        this.setHrmCSVFile(hrmOUTCSVFile);
        this.setFilterFieldName(filterFieldName);
        this.setFilterValues(filterValues);
    }

    public void run() throws IOException {

        if (this.sourceHRData.equalsIgnoreCase("HRM")) {

            //-------------- HRM ------------------
            this.hrmFilterFieldName = this.filterFieldName;
            this.hrmFilterValues = this.filterValues;
            this.setInputJSONFilePath(this.hrmJsonFile);
            this.setOutputCSVFilePath(this.hrmCSVFile);
            // Get CSV file
            this.toCSVFile();
        }

        if (this.sourceHRData.equalsIgnoreCase("EMC")) {
            //--------------- EMC ------------------

            this.emcFilterFieldName = this.filterFieldName;
            this.emcFilterValues = this.filterValues;
            this.setInputJSONFilePath(this.emcJsonFile);
            this.setOutputCSVFilePath(this.emcCSVFile);
            // Get CSV file
            this.toCSVFile();
        }

        if (this.sourceHRData.equalsIgnoreCase("HRM-EMC")) {

            // Если "HRM-EMC" то секция EMC первая,  если "EMC-HRM" то HRM.
            // В связке "HRM-EMC" - первый элемент основа данных  он не может быть нулевым, второй элемент может быть нулевым (как аналог левого join в таблицах SQL)
            // Если нужны данные только EMC или HRM то оставляем соответствующую секцию другую коментируем)
            //--------------- EMC ------------------
            //json.setSourceHRData("EMC-HRM");
            //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export_cut.json");
            this.setInputJSONFilePath(this.emcJsonFile);
            this.setOutputCSVFilePath(this.emcCSVFile);
            this.EMCJsonToArray();

            //-------------- HRM ------------------
            this.hrmFilterFieldName = this.filterFieldName;
            this.hrmFilterValues = this.filterValues;
            this.setInputJSONFilePath(this.hrmJsonFile);
            this.setOutputCSVFilePath(this.hrmCSVFile);
            // Get CSV file
            this.toCSVFile();
        }

        if (this.sourceHRData.equalsIgnoreCase("EMC-HRM")) {

            // Если "HRM-EMC" то EMC первый,  если "EMC-HRM" то HRM. 
            // Если нужны данные только EMC или HRM то оставляем соответствующую секцию другую коментируем)
            //-------------- HRM ------------------
            this.setInputJSONFilePath(this.hrmJsonFile);
            this.setOutputCSVFilePath(this.hrmCSVFile);
            this.HRMJsonToArray();

            //--------------- EMC ------------------
            //json.setSourceHRData("EMC-HRM");
            //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export_cut.json");
            this.emcFilterFieldName = this.filterFieldName;
            this.emcFilterValues = this.filterValues;
            this.setInputJSONFilePath(this.emcJsonFile);
            this.setOutputCSVFilePath(this.emcCSVFile);

            // Get CSV file
            this.toCSVFile();

        }

        if (this.sourceHRData.equalsIgnoreCase("multiEMC-HRM")) {

            // Если "HRM-EMC" то EMC первый,  если "EMC-HRM" то HRM. 
            // Если нужны данные только EMC или HRM то оставляем соответствующую секцию другую коментируем)
            //-------------- HRM ------------------
            this.setInputJSONFilePath(this.hrmJsonFile);
            this.setOutputCSVFilePath(this.hrmCSVFile);
            this.HRMJsonToArray();

            //--------------- multiEMC ------------------
            //json.setSourceHRData("EMC-HRM");
            //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export_cut.json");
            //Временно очищаем то что получили от HRM, что бы собрать HashMap из множества файлов EMC (каждая компания имеет собстевнный json вывод)  
            Map jsonMapHRM_2 = new HashMap(this.jsonMapHRM);
            this.jsonMapHRM.clear();
            this.jsonMapEMC.clear();

            this.emcFilterFieldName = this.filterFieldName;
            this.emcFilterValues = this.filterValues;

            String[] emcJsonfiles = this.getListAllFilesByMask(emcJsonFile);
            Map emcJsonMaps = new HashMap<String, String>();

            for (int k = 0; k < emcJsonfiles.length; k++) {
                System.out.println("emcJsonfiles path: " + emcJsonfiles[k]);

                if (k == emcJsonfiles.length - 1) {
                    //TODO
                    this.setInputJSONFilePath(emcJsonfiles[k]);
                    this.setOutputCSVFilePath(this.emcCSVFile);

                    this.EMCJsonToArray();
                    if (emcJsonMaps.isEmpty()) {
                        emcJsonMaps = this.jsonMapEMC;
                    } else {
                        Iterator iter = this.jsonMapEMC.keySet().iterator();
                        while (iter.hasNext()) {
                            String keyValue = (String) iter.next();
                            emcJsonMaps.putIfAbsent(keyValue, this.jsonMapEMC.get(keyValue));
                        }
                        this.jsonMapEMC.clear();
                    }
                    this.outputArray = this.joinCSVFrom2HashMaps(emcJsonMaps, jsonMapHRM_2);
                    this.toCSVFile();

                } else {
                    this.setInputJSONFilePath(emcJsonfiles[k]);
                    this.setOutputCSVFilePath(this.emcCSVFile);
                    this.EMCJsonToArray();
                    if (emcJsonMaps.isEmpty()) {
                        emcJsonMaps = this.jsonMapEMC;
                    } else {
                        Iterator iter = this.jsonMapEMC.keySet().iterator();
                        while (iter.hasNext()) {
                            String keyValue = (String) iter.next();
                            emcJsonMaps.putIfAbsent(keyValue, this.jsonMapEMC.get(keyValue));
                        }
                        this.jsonMapEMC.clear();
                    }

                }

            }

            System.out.println("emcJsonMaps size=" + emcJsonMaps.size());

//            this.setInputJSONFilePath(this.emcJsonFile);
//            this.setOutputCSVFilePath(this.emcCSVFile);
//
//            // Get CSV file
//            this.toCSVFile();
        }

        if (this.sourceHRData.equalsIgnoreCase("HRM-multiEMC")) {
            //this.setInputJSONFilePath(this.hrmJsonFile);
            //this.setOutputCSVFilePath(this.hrmCSVFile);

            //--------------- multiEMC ------------------
            //json.setSourceHRData("EMC-HRM");
            //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export_cut.json");
            //Временно очищаем то что получили от HRM, что бы собрать HashMap из множества файлов EMC (каждая компания имеет собстевнный json вывод)  
            this.jsonMapHRM.clear();
            this.jsonMapEMC.clear();

            String[] emcJsonfiles = this.getListAllFilesByMask(emcJsonFile);
            Map emcJsonMaps = new HashMap<String, String>();

            for (int k = 0; k < emcJsonfiles.length; k++) {
                System.out.println("emcJsonfiles path: " + emcJsonfiles[k]);

                if (k == emcJsonfiles.length - 1) {
                    //TODO
                    this.setInputJSONFilePath(emcJsonfiles[k]);
                    this.setOutputCSVFilePath(this.emcCSVFile);

                    this.EMCJsonToArray();
                    if (emcJsonMaps.isEmpty()) {
                        emcJsonMaps = this.jsonMapEMC;
                    } else {
                        Iterator iter = this.jsonMapEMC.keySet().iterator();
                        while (iter.hasNext()) {
                            String keyValue = (String) iter.next();
                            emcJsonMaps.putIfAbsent(keyValue, this.jsonMapEMC.get(keyValue));
                        }
                        this.jsonMapEMC.clear();
                    }

                } else {
                    this.setInputJSONFilePath(emcJsonfiles[k]);
                    this.setOutputCSVFilePath(this.emcCSVFile);
                    this.EMCJsonToArray();
                    if (emcJsonMaps.isEmpty()) {
                        emcJsonMaps = this.jsonMapEMC;
                    } else {
                        Iterator iter = this.jsonMapEMC.keySet().iterator();
                        while (iter.hasNext()) {
                            String keyValue = (String) iter.next();
                            emcJsonMaps.putIfAbsent(keyValue, this.jsonMapEMC.get(keyValue));
                        }
                        this.jsonMapEMC.clear();
                    }

                }

            }

            System.out.println("emcJsonMaps size=" + emcJsonMaps.size());

            //-------------- HRM ------------------
            this.hrmFilterFieldName = this.filterFieldName;
            this.hrmFilterValues = this.filterValues;
            emcJsonMaps = new HashMap<String, String>(emcJsonMaps);
            this.jsonMapEMC.clear();
            // this.jsonMapHRM.clear();
            this.setInputJSONFilePath(this.hrmJsonFile);
            this.setOutputCSVFilePath(this.hrmCSVFile);
            this.jsonMapHRM = new HashMap<String, String>();
            this.HRMJsonToArray();
            System.out.println("this.jsonMapHRM=" + this.jsonMapHRM.size());
            this.outputArray = this.joinCSVFrom2HashMaps(this.jsonMapHRM, emcJsonMaps);
            this.toCSVFile();

        }

        System.out.println("Done!!");
    }

    public void setEmcJsonFile(String emcJsonFile) {
        this.emcJsonFile = emcJsonFile;
    }

    public void setEmcCSVFile(String emcCSVFile) {
        this.emcCSVFile = emcCSVFile;
    }

    public void setHrmJsonFile(String hrmJsonFile) {
        this.hrmJsonFile = hrmJsonFile;
    }

    public String getHrmJsonFile() {
        return this.hrmJsonFile;
    }

    public void setHrmCSVFile(String hrmCSVFile) {
        this.hrmCSVFile = hrmCSVFile;
    }

    public void setSourceHRData(String sourceHRData) {
        this.sourceHRData = sourceHRData;
    }

    public String getSourceHRData() {
        return sourceHRData;
    }

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

    public String[] HRMJsonToArray() throws IOException {

        JSONObject obj = new JSONObject(this.StringFromStream());
        JSONArray array = obj.getJSONArray("records");
        JSONObject firstRow = array.getJSONObject(0);
        Iterator<String> keySetIter = firstRow.keys();

        String csvStringValues = "";
        String[] filterFieldnames = hrmFilterFieldName.split(this.delimiter);
        Boolean checkResult = Boolean.FALSE;
        Array returnArrayList;

        String pid = "";
        Map hmap = new HashMap<String, String>();

        String csvKeysString = "";
        String keySetIternextValue = "";
        String[] keys = new String[firstRow.keySet().size()];
        int k = 0;
        while (keySetIter.hasNext()) {
            keySetIternextValue = keySetIter.next();

            keys[k] = keySetIternextValue.toString();

            csvKeysString = csvKeysString + keySetIternextValue;
            if (keySetIter.hasNext()) {
                csvKeysString = csvKeysString + this.delimiter;
            }
            k = k + 1;
        }
        //System.out.println(csvKeysString);

        if (this.wholeFile) {
            returnArray = new String[array.length() + 1];
            this.numberOfrows = array.length();
        } else {
            returnArray = new String[numberOfrows + 1];
        }

        returnArray[0] = csvKeysString;
        hmap.put("csvFieldNames", csvKeysString);

        for (int i = 0; i < numberOfrows; i++) {

            JSONObject arrayJson = array.getJSONObject(i);
            //System.out.println("hrmFilterFieldValue" + hrmFilterFieldValue);

            if (hrmFilterFieldName.equalsIgnoreCase("*") || hrmFilterValues.equalsIgnoreCase("*")) {
                checkResult = true;
            } else {

                for (int s = 0; s < filterFieldnames.length; s++) {
                    //System.out.println(filterFieldnames[s].toString());
                    String checkedValue = "";
                    if (arrayJson.has(filterFieldnames[s])) {
                        checkedValue = arrayJson.get(filterFieldnames[s]).toString();
                    }
                    //System.out.println(checkedValue);
                    Boolean innerCheckResult = this.checkInList(checkedValue, hrmFilterValues, this.delimiter);

                    if (innerCheckResult) {
                        checkResult = innerCheckResult;
                    }
                }
            }
            //System.out.println("checkResult="+checkResult);
            if (checkResult) {
                // System.out.println(checkedValue);

                for (int p = 0; p < keys.length; p++) {
                    pid = arrayJson.get("PID").toString();
                    csvStringValues = csvStringValues + arrayJson.get(keys[p].trim()).toString();
                    if (p < keys.length - 1) {
                        csvStringValues = csvStringValues + this.delimiter;
                    }

                }
            }

            //добавляем в хеш мап не пустые csv строки и ключ hrmid(pid)
            if (csvStringValues.isEmpty() == false) {
                System.out.println(pid + ": " + csvStringValues);
                hmap.put(pid, csvStringValues);
                //System.out.println(csvStringValues);
            }
            returnArray[i + 1] = csvStringValues;

            //Сбрасываем все в начальные значения
            csvStringValues = "";
            checkResult = false;
            pid = "";
        }

        List<String> list = new ArrayList<String>();

        for (String s : returnArray) {
            if (s != null && s.length() > 0) {
                list.add(s);
            }
        }

        returnArray = list.toArray(new String[list.size()]);
        System.out.println("HRM array lenghth:" + returnArray.length);
        this.jsonMapHRM = hmap;
        System.out.println(jsonMapEMC.isEmpty());
        if (jsonMapEMC.isEmpty() == false) {
            String[] returnArray = this.joinCSVFrom2HashMaps(this.jsonMapHRM, this.jsonMapEMC);

            return (String[]) returnArray;
        } else {
            return (String[]) returnArray;
        }
    }

    public String[] EMCJsonToArray() throws IOException {

        JSONObject obj = new JSONObject(this.StringFromStream());
        JSONObject objGetByKey = obj.getJSONObject("result");
        String[] ks = objGetByKey.keySet().toArray(new String[0]);

        JSONObject firstRow = new JSONObject();
        if (objGetByKey.isNull("1") == false) {

            firstRow = (JSONObject) objGetByKey.get("1");
        } else {
            Iterator<String> recordKey = objGetByKey.keys();

            while (recordKey.hasNext()) {
                String key = recordKey.next();
                System.out.println("key=" + key);

                if (objGetByKey.getJSONObject(key).has("companies")) {

                    firstRow = (JSONObject) objGetByKey.get(key);
                    break;
                } else {
                    // System.out.println("login:"+objGetByKey.getJSONObject(key).get("login"));
                }
            }

        }

        String[] filterFieldnames = emcFilterFieldName.split(this.delimiter);
        Boolean checkResult = Boolean.FALSE;

        Iterator<String> keySetIter = firstRow.keys();
        String hrmId = "";
        Map hmap = new HashMap<String, String>();

        String csvKeysString = "";
        String keySetIternextValue = "";
        String[] keys = new String[firstRow.keySet().size()];
        int k = 0;
        while (keySetIter.hasNext()) {
            keySetIternextValue = keySetIter.next();

            keys[k] = keySetIternextValue.toString();

            csvKeysString = csvKeysString + keySetIternextValue;

            if (keySetIter.hasNext()) {
                csvKeysString = csvKeysString + this.delimiter;
            }
            k = k + 1;
        }
        //System.out.println(csvKeysString);

        //делаем подмену ключевых полей на те, что уже использовались в idm
        csvKeysString = csvKeysString.replace("isActive", "isActiveEMC");

        if (this.wholeFile) {
            returnArray = new String[ks.length + 1];
            this.numberOfrows = ks.length;
        }

        returnArray[0] = csvKeysString;
        hmap.put("csvFieldNames", csvKeysString);

        for (int i = 0; i < ks.length; i++) {
            //System.out.println(ks[i]);

            JSONObject obj2 = (JSONObject) objGetByKey.get(ks[i]);

            if (emcFilterFieldName.equalsIgnoreCase("*") || emcFilterValues.equalsIgnoreCase("*")) {
                checkResult = true;
            } else {

                for (int s = 0; s < filterFieldnames.length; s++) {
                    //System.out.println(filterFieldnames[s].toString());
                    String checkedValue = "";
                    if (obj2.has(filterFieldnames[s])) {
                        checkedValue = obj2.get(filterFieldnames[s]).toString();

                        checkedValue = checkedValue.replace(this.delimiter, replaceSymbol);
                        Boolean innerCheckResult = this.checkInList(checkedValue, emcFilterValues, this.delimiter);
                        //System.out.println("checkedValue: "+checkedValue+", emcFilterValues: "+emcFilterValues);
                        if (innerCheckResult) {
                            checkResult = innerCheckResult;
                        }
                    }
                }
            }

            // Если нужны все пользователи то тогда есть смысл внести изменения Есть ли у пользователей логины и активны ли они 
            //oldValue  sourceId password
            if (obj2.isNull("login") == false && obj2.get("isActive").equals("1")) {
                String csvStringValues = "";
                if (obj2.isNull("hrmId") == false) {
                    hrmId = obj2.get("hrmId").toString();
                } else {
                    hrmId = "dummy-" + i;
                }
                for (int p = 0; p < keys.length; p++) {

                    String toAdd = "";

                    if (obj2.isNull(keys[p].toString()) == false) {

                        toAdd = obj2.get(keys[p].toString()).toString();

                        if (keys[p].equalsIgnoreCase("login")) {
                            toAdd = toAdd.toLowerCase();

                        }

                        if (keys[p].equalsIgnoreCase("emails")) {
                            String mainEmail = getMainEmail(toAdd);
                            //Замена исходного значения "emails" 
                            toAdd = mainEmail;

                        }

                        if (keys[p].equalsIgnoreCase("companies")) {
                            String companies = getCompanies(toAdd);
                            //Замена исходного значения "companies" 
                            toAdd = companies;

                        }

                        if (keys[p].equalsIgnoreCase("groups")) {
                            String groups = getGroups(toAdd);
                            //Замена исходного значения "groups" 
                            toAdd = groups;

                        }

                        if (keys[p].equalsIgnoreCase("photos")) {
                            String photos = getPhotos(toAdd);
                            //Замена исходного значения "photos" 
                            toAdd = photos;

                        }

                        if (keys[p].equalsIgnoreCase("roles")) {
                            String roles = toAdd.replace("[", "").replace("]", "").replace("\"", "");
                            //Замена исходного значения "roles" 
                            toAdd = roles;

                        }

                        if (keys[p].equalsIgnoreCase("password")) {
                            String roles = toAdd.replace("[", "").replace("]", "").replace("\"", "");
                            //Замена исходного значения "password" 
                            toAdd = "";

                        }

                        if (toAdd.contains(this.delimiter)) {
                            toAdd = toAdd.replaceAll(this.delimiter, replaceSymbol);
                            //System.out.println("FIND!!!");
                        }
                        //System.out.println(keys[p]);
                        if (keys[p].equalsIgnoreCase("extensions") || keys[p].equalsIgnoreCase("settings")) {
                            toAdd = "";
                        }

                    }

                    //System.out.println("checkResultEMC="+checkResult.toString());    
                    if (checkResult) {

                        csvStringValues = csvStringValues + toAdd;

                        if (p < keys.length - 1) {
                            csvStringValues = csvStringValues + this.delimiter;
                        }
                    }

                }

                //добавляем в хеш мап не пустые csv строки и ключ hrmid(pid)
                if (csvStringValues.isEmpty() == false) {
                    // System.out.println(hrmId + ": " + csvStringValues);
                    hmap.put(hrmId, csvStringValues);
                    //System.out.println(csvStringValues);
                }

                returnArray[i + 1] = csvStringValues;

                //Сбрасываем все в начальные значения
                csvStringValues = "";
                hrmId = "";
                checkResult = false;
            }
        }
        System.out.println(hmap.size());
        jsonMapEMC = hmap;

        System.out.println(jsonMapHRM.isEmpty());
        if (jsonMapHRM.isEmpty() == false) {
            String[] returnArrayT = this.joinCSVFrom2HashMaps(jsonMapEMC, jsonMapHRM);

            return (String[]) returnArrayT;
        } else {
            return (String[]) returnArray;
        }
    }

    //toDo
    private String getPhotos(String toAdd) {

        toAdd = toAdd.replace(this.replaceSymbol, this.delimiter);
        JSONArray array = new JSONArray(toAdd);
        String photos = "";

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            photos = photos + obj.getString("photo");
            if (i < array.length() - 1) {
                photos = photos + this.delimiter;
            }

        }
        return photos;
    }

    private String getGroups(String toAdd) {

        toAdd = toAdd.replace(this.replaceSymbol, this.delimiter);
        JSONArray array = new JSONArray(toAdd);
        String groups = "";

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            groups = groups + obj.getString("name");
            if (i < array.length() - 1) {
                groups = groups + this.delimiter;
            }

        }
        return groups;
    }

    private String getCompanies(String toAdd) {
        toAdd = toAdd.replace(this.replaceSymbol, this.delimiter);
        JSONObject obj = new JSONObject(toAdd);
        String companies = "";
        String keySetIternextValue = "";
        Iterator<String> keySetIter = obj.keys();
        String[] keys = new String[obj.keySet().size()];
        int k = 0;
        while (keySetIter.hasNext()) {
            keySetIternextValue = keySetIter.next();
            companies = companies + obj.getString(keySetIternextValue.toString());
            if (keySetIter.hasNext()) {
                companies = companies + this.delimiter;
            }

        }
        return companies;
    }

    private String getMainEmail(String toAdd) {
        toAdd = toAdd.replace(this.replaceSymbol, this.delimiter);
        JSONObject obj = new JSONObject(toAdd);
        Map mainEmail = new HashMap<String, Integer>();
        String mainEmailStr = "";

        String keySetIternextValue = "";
        Iterator<String> keySetIter = obj.keys();
        String[] keys = new String[obj.keySet().size()];
        int k = 0;
        while (keySetIter.hasNext()) {

            keySetIternextValue = keySetIter.next();

            if (obj.get(keySetIternextValue.toString()) != null && !obj.get(keySetIternextValue.toString()).toString().isEmpty() && !obj.get(keySetIternextValue.toString()).toString().equalsIgnoreCase("null")) {

                keys[k] = obj.get(keySetIternextValue.toString()).toString();
                if (mainEmail.containsKey(obj.get(keySetIternextValue.toString()))) {

                    int value = (int) mainEmail.get(obj.get(keySetIternextValue.toString()));
                    mainEmail.replace(obj.get(keySetIternextValue.toString()), value + 1);

                } else {
                    mainEmail.put(obj.get(keySetIternextValue.toString()), 1);

                }
            }
            k = k + 1;
        }

        if (!mainEmail.isEmpty()) {
//            System.out.println("\nNumberofEmails=" + mainEmail.size());
//            System.out.println(": keysLength=" + keys.length);

            for (int i = keys.length; i > 0; i--) {

                if (mainEmail.containsValue(i)) {
//                    System.out.println(mainEmail.toString());
//                    System.out.println("Value=" + i);
//                    System.out.println("MainEmail: "+keys[i-1]);
                    mainEmailStr = keys[i - 1];
                    break;

                }

            }

        }

        //System.out.println(mainEmailStr);
        return mainEmailStr.toLowerCase();
    }

    Boolean checkInList(String checkedValue, String inputString, String delimiter) {
        Boolean result = Boolean.FALSE;
        if (checkedValue == null || checkedValue.isEmpty()) {
            result = false;
        } else {
            String[] arrayInputData = inputString.toLowerCase().split(delimiter);
            for (int i = 0; i < arrayInputData.length; i++) {
                if (checkedValue.equalsIgnoreCase(arrayInputData[i]) || checkedValue.toLowerCase().contains(arrayInputData[i])) {
                    return true;
                } else {
                    result = false;

                }

            }
        }
        return result;
    }

    private void toCSVFile() throws IOException {

        PrintWriter writer = new PrintWriter(this.outputCSVFilePath, "UTF-8");
        String[] k = null;
        if (sourceHRData.equalsIgnoreCase("HRM-EMC") || sourceHRData.equalsIgnoreCase("HRM")) {
            k = this.HRMJsonToArray();
        }

        if (sourceHRData.equalsIgnoreCase("EMC-HRM") || sourceHRData.equalsIgnoreCase("EMC")) {
            k = this.EMCJsonToArray();
        }

        if (sourceHRData.equalsIgnoreCase("multiEMC-HRM") || sourceHRData.equalsIgnoreCase("HRM-multiEMC")) {
            k = this.outputArray;

        }

        if (k != null) {
            System.out.println("Output csv number of rows:" + k.length);
            for (int i = 0; i < k.length; i++) {
                System.out.println("k[i]=" + k[i]);
                if (k[i] != null) {
                    writer.println(this.cleanOfSpecSymbols(k[i]));
                }
            }
        }

        writer.close();
        System.out.println("OutputFile: " + this.outputCSVFilePath);

    }

    private String[] joinCSVFrom2HashMaps(Map jsonMapHRM, Map jsonMapEMC) {
        //String[] returnArray= new String[jsonMapHRM.size()];
        String csvFieldNames = "";
        if (jsonMapEMC.isEmpty() == false) {
            csvFieldNames = (String) jsonMapHRM.get("csvFieldNames") + this.delimiter + (String) jsonMapEMC.get("csvFieldNames");
        } else {
            csvFieldNames = (String) jsonMapHRM.get("csvFieldNames");
        }

        //returnArray[0]=csvFieldNames;
        Iterator keySet = jsonMapHRM.keySet().iterator();
        List list = new ArrayList<String>();
        list.add(0, csvFieldNames);
        String key = "";
        while (keySet.hasNext()) {
            key = (String) keySet.next();
            if (key.equalsIgnoreCase("csvFieldNames") == false) {
                list.add((String) jsonMapHRM.get(key) + this.delimiter + (String) jsonMapEMC.get(key));
            }

        }

        returnArray = (String[]) list.toArray(new String[jsonMapHRM.size()]);

        return returnArray;
    }

    private String[] getListAllFilesByMask(String emcJsonFile) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        File fileObj = new File(emcJsonFile);
        String fileMask = fileObj.getName();
        String dirPath = emcJsonFile.replace(fileMask, "");

        //System.out.println("dir.getName(): "+dir.getName());
        File dir = new File(dirPath);
        List<String> textFiles = new ArrayList<String>();

        for (File file : dir.listFiles()) {
            if (file.isHidden() == false) {
                //System.out.println("file: "+file.getName());

                if (fileMask.equalsIgnoreCase("*")) {
                    String toAdd = file.getName();
                    System.out.println("fileNames: " + toAdd);
                    textFiles.add(dirPath + toAdd);
                }

                if (file.getName().matches(fileMask)) {
                    String toAdd = file.getName();
                    System.out.println("fileNames: " + toAdd);
                    textFiles.add(dirPath + toAdd);

                }

            }
        }
        String[] returnString = textFiles.toArray(new String[textFiles.size()]);

        return returnString;
    }

    private String cleanOfSpecSymbols(String inputString) {

        return inputString.replace("'", "");

    }

    private void getJSONFromURL(String hrmURL, String hrmJsonFile) throws DataFormatException,IOException  {

        HTMLutils html = new HTMLutils();
        String content = new String(html.readFromUrl(hrmURL));
        if (!content.startsWith("{")) {
            throw new DataFormatException(hrmURL + " output does not contain valid JSON format");
        }

        PrintWriter writer = new PrintWriter(hrmJsonFile, "UTF-8");
        writer.print(content);
        writer.close();

    }

    public static void main(String[] args) throws DataFormatException,IOException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
        //System.setProperty("javax.net.ssl.trustStore", "/etc/ssl/certs/java/cacerts");
        //System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        //System.setProperty("javax.net.debug", "ssl");

        Properties prop = new Properties();
        InputStream input = null;

        input = new FileInputStream("jsonparser.properties");

        // load a properties file
        prop.load(input);

        // get the property value and print it out
        System.out.println("direction: " + prop.getProperty("direction"));
        String direction = prop.getProperty("direction");

        System.out.println("emcJsonFile: " + prop.getProperty("emcJsonFile"));
        String emcJsonFile = prop.getProperty("emcJsonFile");

        System.out.println("hrmJsonFile: " + prop.getProperty("hrmJsonFile"));
        String hrmJsonFile = prop.getProperty("hrmJsonFile");

        System.out.println("filterFieldName: " + prop.getProperty("filterFieldName"));
        String filterFieldName = prop.getProperty("filterFieldName");

        System.out.println("filterValues: " + prop.getProperty("filterValues"));
        String filterValues = prop.getProperty("filterValues");

        System.out.println("hrmURL: " + prop.getProperty("hrmURL"));
        String hrmURL = prop.getProperty("hrmURL");

        System.out.println("hrmCSVFile: " + prop.getProperty("hrmOUTCSVFile"));
        String hrmOUTCSVFile = prop.getProperty("hrmOUTCSVFile");

        JSONparser json;
        if (hrmURL != null) {

            json = new JSONparser(direction, emcJsonFile, hrmURL, hrmOUTCSVFile, filterFieldName, filterValues);

        } else {

            json = new JSONparser(direction, emcJsonFile, hrmJsonFile, filterFieldName, filterValues);
        }

        json.run();

    }

}
