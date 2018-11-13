/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author o.nekriach
 */
public class FileUtil {

    int SPLITLENGTH = -100;
    String DELIMITER = ",";

    public BufferedReader readDataFromFile(String file) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        return br;
    }

    public static String readFileToString(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public void writeStringToFile(String fileName, String input) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(input);
        writer.close();
    }

    /**
     * Get HashMap from buffer of csv format and set primary field for HashMap
     * Value1,Value2,Value3 Value4,Value5,Value4
     *
     * @param fieldName primary field of output HashMap
     * @param br reading file buffer
     * @throws IOException
     */
    public Map<String, Map<String, String>> getCsvToMap(String fieldName, BufferedReader br) throws IOException {
        Map output = new <String, Map<String, String>>HashMap();
        String primaryKey = null;
        int primaryKeyPosition = 0;
        Map fildsPosition = new <Integer, String>HashMap();

        String line = br.readLine();
        String fieldsCSV = new String(line);
        String[] fieldsAr = fieldsCSV.split(DELIMITER, SPLITLENGTH);
        for (int i = 0; i < fieldsAr.length; i++) {
            if (fieldsAr[i].equalsIgnoreCase(fieldName)) {
                primaryKey = fieldsAr[i];
                primaryKeyPosition = i;
                fildsPosition.put(i, fieldsAr[i]);
            }
            fildsPosition.put(i, fieldsAr[i]);
        }
        if (primaryKey == null) {
            throw new VerifyError("Primary key " + fieldName + " not found in input CSV file");
        }
        int j = 0;
        while (line != null) {
            if (j > 0) {
                Map fld = new <String, String>HashMap();
                String[] lineAr = line.split(DELIMITER, SPLITLENGTH);
                for (int k = 0; k < lineAr.length; k++) {
                    fld.put(fildsPosition.get(k), lineAr[k]);
                }
                output.put(lineAr[primaryKeyPosition], fld);
            }
            j = j + 1;
            line = br.readLine();
        }
        br.close();
        if (output.size() == 0) {
            return null;
        }
        return output;
    }

    /**
     * Get HashMap from String[] of csv format and set primary field for HashMap
     * Value1,Value2,Value3 Value4,Value5,Value4
     *
     * @param fieldName primary field of output HashMap
     * @param csv input csv data of String[] type
     * @throws IOException
     */
    public Map<String, Map<String, String>> getCsvToMap(String fieldName, String[] csv) throws IOException {
        Map output = new <String, Map<String, String>>HashMap();
        String primaryKey = null;
        int primaryKeyPosition = 0;
        Map fildsPosition = new <Integer, String>HashMap();

        String fieldsCSV = new String(csv[0]);
        String[] fieldsAr = fieldsCSV.split(DELIMITER, SPLITLENGTH);
        for (int i = 0; i < fieldsAr.length; i++) {
            if (fieldsAr[i].equalsIgnoreCase(fieldName)) {
                primaryKey = fieldsAr[i];
                primaryKeyPosition = i;
                fildsPosition.put(i, fieldsAr[i]);
            }
            fildsPosition.put(i, fieldsAr[i]);
        }
        if (primaryKey == null) {
            throw new VerifyError("Primary key " + fieldName + " not found in input CSV array");
        }

        for (int j = 1; j < csv.length; j++) {

            Map fld = new <String, String>HashMap();
            String[] lineAr = csv[j].split(DELIMITER, SPLITLENGTH);
            for (int k = 0; k < lineAr.length; k++) {
                fld.put(fildsPosition.get(k), lineAr[k]);
            }
            output.put(lineAr[primaryKeyPosition], fld);
        }

        if (output.size() == 0) {
            return null;
        }
        return output;
    }

    /**
     * Get HashMap from buffer of csv format and set primary field for HashMap
     * Value1,Value2,Value3 Value1,Value4,Value5 Value6,Value7,Value8
     * Value6,Value9,Value10
     *
     * @param fieldName primary field of output HashMap
     * @param br reading file buffer
     * @throws IOException
     */
    public Map<String, ArrayList<Map<String, String>>> getCsvToComplexMap(String fieldName, BufferedReader br) throws IOException {
        Map output = new <String, ArrayList<Map<String, String>>>HashMap();
        String primaryKey = null;
        int primaryKeyPosition = 0;
        Map fildsPosition = new <Integer, String>HashMap();

        String line = br.readLine();
        String fieldsCSV = new String(line);
        String[] fieldsAr = fieldsCSV.split(DELIMITER, SPLITLENGTH);
        for (int i = 0; i < fieldsAr.length; i++) {
            if (fieldsAr[i].equalsIgnoreCase(fieldName)) {
                primaryKey = fieldsAr[i];
                primaryKeyPosition = i;
                fildsPosition.put(i, fieldsAr[i]);
            }
            fildsPosition.put(i, fieldsAr[i]);
        }
        if (primaryKey == null) {
            throw new VerifyError("Primary key " + fieldName + " not found in input CSV file");
        }
        int j = 0;
        while (line != null) {
            if (j > 0) {
                // TODO Map<String, ArrayList<Map<String, String>>>                
                Map fld = new <String, String>HashMap();
                String[] lineAr = line.split(DELIMITER, SPLITLENGTH);
                for (int k = 0; k < lineAr.length; k++) {
                    fld.put(fildsPosition.get(k), lineAr[k]);
                }
                if (!output.containsKey(lineAr[primaryKeyPosition])) {
                    ArrayList<Map<String, String>> gr = new ArrayList<Map<String, String>>();
                    gr.add(fld);
                    output.put(lineAr[primaryKeyPosition], gr);
                } else {
                    ArrayList<Map<String, String>> exArr = (ArrayList<Map<String, String>>) output.get(lineAr[primaryKeyPosition]);

                    exArr.add(fld);
                    output.replace(lineAr[primaryKeyPosition], exArr);
                }

            }
            j = j + 1;
            line = br.readLine();
        }
        br.close();
        if (output.size() == 0) {
            return null;
        }
        return output;
    }

    public Boolean fileExist(String fileName) {
        Boolean checkResult = false;
        File f = new File(fileName);
        if (f.exists() && !f.isDirectory()) {
            checkResult = true;
        }
        return checkResult;
    }
}
