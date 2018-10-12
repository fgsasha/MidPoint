/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author o.nekriach
 */
public class FileUtil {

    int SPLITLENGTH = -100;
    String DELIMITER = ",";

   public  BufferedReader readDataFromFile(String file) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        PrintStream out = new PrintStream(new FileOutputStream("file"));
        String csvFile;
        br = new BufferedReader(new FileReader(file));
        return br;
    }

    /**
     * Get HashMap from buffer of csv format and set primary field for HashMap
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
            }
            fildsPosition.put(fieldsAr[i], i);
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
            j = j++;
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
