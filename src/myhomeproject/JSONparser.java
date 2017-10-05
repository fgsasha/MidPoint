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

    public JSONparser(String direction, String emcJsonFile, String emcCSVFile, String hrmJsonFile, String hrmCSVFile, String filterFieldName, String filterValues) {

        //json.setFilterFieldName("*");
        //json.setFilterValues("*");
        this.wholeFile = Boolean.TRUE;
        this.setSourceHRData(direction);//EMC-HRM, EMC, HRM
        this.setEmcJsonFile(emcJsonFile);
        this.setEmcCSVFile(emcCSVFile);
        this.setHrmJsonFile(hrmJsonFile);
        this.setHrmCSVFile(hrmCSVFile);
        this.setFilterFieldName(filterFieldName);
        this.setFilterValues(filterValues);
    }

    public void run() throws IOException {

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
                System.out.println("emcJsonfiles path: "+ emcJsonfiles[k]);

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


            //--------------- multiEMC ------------------
            //json.setSourceHRData("EMC-HRM");
            //json.setInputJSONFilePath("/home/onekriach/Downloads/work/EMC_export_cut.json");
            //Временно очищаем то что получили от HRM, что бы собрать HashMap из множества файлов EMC (каждая компания имеет собстевнный json вывод)  
            this.jsonMapHRM.clear();
            this.jsonMapEMC.clear();

            String[] emcJsonfiles = this.getListAllFilesByMask(emcJsonFile);
            Map emcJsonMaps = new HashMap<String, String>();

            for (int k = 0; k < emcJsonfiles.length; k++) {
                System.out.println("emcJsonfiles path: "+ emcJsonfiles[k]);

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
            this.jsonMapEMC.clear();
            this.jsonMapHRM.clear();            
            this.setInputJSONFilePath(this.hrmJsonFile);
            this.setOutputCSVFilePath(this.hrmCSVFile);
            this.HRMJsonToArray();
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
                    String checkedValue = arrayJson.get(filterFieldnames[s]).toString();
                    //System.out.println(checkedValue);
                    Boolean innerCheckResult = this.checkInList(checkedValue, hrmFilterValues, this.delimiter);
                    if (innerCheckResult) {
                        checkResult = innerCheckResult;
                    }
                }
            }
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

        jsonMapHRM = hmap;
        System.out.println(jsonMapEMC.isEmpty());
        if (jsonMapEMC.isEmpty() == false) {
            String[] returnArray = this.joinCSVFrom2HashMaps(jsonMapHRM, jsonMapEMC);

            return (String[]) returnArray;
        } else {
            return (String[]) returnArray;
        }
    }

    public String[] EMCJsonToArray() throws IOException {

        JSONObject obj = new JSONObject(this.StringFromStream());
        JSONObject objGetByKey = obj.getJSONObject("result");
        String[] ks = objGetByKey.keySet().toArray(new String[0]);

        JSONObject firstRow = (JSONObject) objGetByKey.get("1");
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
        return mainEmailStr;
    }

    Boolean checkInList(String checkedValue, String inputString, String delimiter) {
        Boolean result = Boolean.FALSE;
        if (checkedValue == null || checkedValue.isEmpty()) {
            result = false;
        } else {
            String[] arrayInputData = inputString.split(delimiter);
            for (int i = 0; i < arrayInputData.length; i++) {
                if (checkedValue.equalsIgnoreCase(arrayInputData[i]) || checkedValue.contains(arrayInputData[i])) {
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
        if (sourceHRData.equalsIgnoreCase("HRM-EMC")) {
            k = this.HRMJsonToArray();
        }

        if (sourceHRData.equalsIgnoreCase("EMC-HRM")) {
            k = this.EMCJsonToArray();
        }

        if (sourceHRData.equalsIgnoreCase("multiEMC-HRM")|| sourceHRData.equalsIgnoreCase("HRM-multiEMC")) {
            k = this.outputArray;

        }

        if (k != null) {
            System.out.println(k.length);
            for (int i = 0; i < k.length; i++) {
                if (k[i] != null) {
                    writer.println(k[i]);
                }
            }
        }

        writer.close();
        System.out.println("OutputFile: "+this.outputCSVFilePath);

    }

    private String[] joinCSVFrom2HashMaps(Map jsonMapHRM, Map jsonMapEMC) {
        //String[] returnArray= new String[jsonMapHRM.size()];
        String csvFieldNames = (String) jsonMapHRM.get("csvFieldNames") + this.delimiter + (String) jsonMapEMC.get("csvFieldNames");

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

    public static void main(String[] args) throws IOException {
        String direction = "HRM-multiEMC";//EMC-HRM, EMC, HRM, multiEMC-HRM, HRM-multiEMC
        //String emcJsonFile = "/home/onekriach/Downloads/work/EMC_export.json";        
        //String emcCSVFile = "/home/onekriach/Downloads/work/EMC_export_27-09.csv";
        String emcJsonFile = "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc*.json";
        String emcCSVFile = "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emcMultiplyJsons.csv";
        String hrmJsonFile = "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/export_05-10_fresh_HRM_data_ALL.json";
        String hrmCSVFile = "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/hrmMultiplyJsons_Anastasia.csv";
        String filterFieldName = "emailWork,emailPrimary";
        //String filterFieldName = "*";
        String filterValues = "*";

        JSONparser json = new JSONparser(direction, emcJsonFile, emcCSVFile, hrmJsonFile, hrmCSVFile, filterFieldName, filterValues);
        json.run();

    }

    private String[] getListAllFilesByMask(String emcJsonFile) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String[] returnString = {
            "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_ITN.json",
            "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_SLT.json",
            "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_PH.json",
            "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_CA.json",
            "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_ME.json",
            "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_UK.json",
            "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_BOG.json"
              //  , "/home/onekriach/Downloads/work/multipleJsonEMC/drive-download-20171005T194716Z-001/EMC/emc_Yaturist.json"
        };

        return returnString;
    }

}
