/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myhomeproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
/**
 *
 * @author Nekriach
 */

public class MyHomeProject {

    public static void main(String[] args) {
        MyHomeProject obj = new MyHomeProject();
        obj.run();
    }

    public void run() {

        String csvFile = "/Users/Dorjee/Desktop/sampleCSV.csv"; //Path of file to be read.
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";        
        String[] column;

        int count = 0;

        try {

            PrintStream out = new PrintStream(new FileOutputStream("OutputLDIFFile.ldif"));
            br = new BufferedReader(new FileReader(csvFile));

            while ((line = br.readLine()) != null) {

                // using comma as separator
                column = line.split(csvSplitBy);

                //End format of the ouput file.
                //Change according to .CSV file.
                //Count used to exclude the reading of the first line.
                if (count > 0) {
                    out.println("dn: cn="+column[5]+", ou="+column[7]+", o=Data"+
                            "\nchangetype: " + column[2]
                            + "\nou: " + column[7]
                            + "\nobjectClass: " + column[3]
                            + "\nobjectClass: " + column[4]
                            + "\nobjectClass: " + column[5]
                            + "\nobjectClass: " + column[6]
                            + "\ncn: " + column[5]
                            + "\nuid: "+column[4]
                            + "\nSAMAccountName: "+column[1]
                            + "\ngivenName: "+column[0]
                            + "\nsn: "+column[3]
                            + "\n"
                    );
                }
                count++;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Done");
    }

}