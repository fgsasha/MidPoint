/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userreview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author o.nekriach
 */
public class LinuxUserStatus {

    private String argument;
    private int passGt;
    private String dateFormat = "MM/dd/yyyy";
    private BufferedReader br;
    private String defaultFile = "user_passwd-s.txt";
    private ArrayList<String> usersRawLine = new ArrayList<String>();
    private String outputFile = "userReviewResult.txt";

    public LinuxUserStatus(String arg) {
        this.setArgument(arg);
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    private String getCase(String arg) {
        String i = "0";
        if (arg.matches("[pP][\\d]+")) {
            i = "1";
            passGt = Integer.parseInt(this.getArgument().replaceAll("[^\\d]", ""));

        } else if (arg.equalsIgnoreCase("L")) {
            i = "2";
        } else if (arg.equalsIgnoreCase("NP")) {
            i = "3";
        } else if (arg.equalsIgnoreCase("P")) {
            i = "4";
        } else {
            throw new Error("!!! Invalid argument has been passed Should be one of \r\nlocked password (L), has no password (NP), has a usable password (P), has a usable password older 90 days (P90)");
        }
        return i;
    }

//#!/bin/bash
//for i in $( sed 's/:.*//' /etc/passwd ); do
//passwd -S $i
//done
    void getUserstatus() throws IOException, ParseException {
        if (this.checkPermisions()) {
            //-----------------------------------------------
            System.out.println("Available keys: locked password (L), has no password (NP), has a usable password (P), has a usable password older 90 days (P90)");
            System.out.println("Input key: " + getArgument() + "\n...");
            //-----------------------------------------------
            ArrayList<String> outputArr = new ArrayList<String>();
            String[] users = this.readUsers();
            String k = this.getCase(this.getArgument());
            for (int i = 0; i < users.length; i++) {
                String userStatus = readUserStatus(users[i]);
                switch (k) {
                    case "1":

                        if (userStatus.startsWith(users[i] + " P") && this.getPasswordAge(userStatus.split("\\s")[2]) >= passGt) {
                            //System.out.println(userStatus);
                            //System.out.println(this.getPasswordAge(userStatus.split("\\s")[2]));
                            System.out.println(users[i]);
                            outputArr.add(users[i]);
                            break;
                        }

                        break;
                    case "2":

                        if (userStatus.startsWith(users[i] + " L")) {
                            System.out.println(users[i]);
                            outputArr.add(users[i]);
                            break;
                        }

                    case "3":
                        if (userStatus.startsWith(users[i] + " NP")) {
                            System.out.println(users[i]);
                            outputArr.add(users[i]);
                            break;
                        }

                        break;
                    case "4":
                        if (userStatus.contains(users[i] + " P")) {
                            System.out.println(users[i]);
                            outputArr.add(users[i]);
                            break;
                        }

                }

            }
            this.writeToFile(outputArr);

        } else {
            throw new Error("!!! You shoud be root/have sudo to run script or use offline file user_passwd-s.txt !!!");
        }
    }

    Boolean checkPermisions() throws IOException {
        Boolean result = false;
        try {

            FileInputStream input = new FileInputStream(new File(defaultFile));
            //InputStreamReader isr = new InputStreamReader(input, Charset.forName("UTF-8"));
            br = new BufferedReader(new InputStreamReader(input));

            result = true;

        } catch (FileNotFoundException ex) {
            //Logger.getLogger(LinuxUserStatus.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("There is no file " + defaultFile + " in script directory\ncontinue online, by reading users info from local workstation files /etc...\n");

            File file = new File("/etc/shadow");
            if (file.canRead()) {
                result = true;
            } else {
                result = false;
            }

        }
        return result;
    }

    String[] readUsers() throws IOException {
        String[] user_arr;
        if (br == null) {
            //----read all users from /etc/passwd-----------
            //    Process proc = Runtime.getRuntime().exec("/bin/sed 's/:.*//' /etc/passwd");
            //    java.io.InputStream is = proc.getInputStream();
            //    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            //----------------------------------------------

            String[] command = {"/bin/sh", "-c", "cat /etc/passwd | sed 's/:.*//'"};
            Process proc = Runtime.getRuntime().exec(command);
            // exec("cat /etc/passwd | sed \"s/:.*//\"");
            java.io.InputStream is = proc.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            String val = "";
            int i = 0;
            if (s.hasNextLine()) {
                val = s.next();
                //System.out.println(i+":"+val);

            } else {
                val = "";
            }
            user_arr = val.split("\n");
        } else {

            String line;
            ArrayList<String> users = new ArrayList<String>();

            BufferedReader br2 = br;
            while ((line = br2.readLine()) != null) {
                //System.out.println(line);
                users.add(line.split("\\s")[0]);
                usersRawLine.add(line);
            }
            user_arr = users.toArray(new String[users.size()]);
        }
        return user_arr;
    }

    String readUserStatus(String user) throws IOException {
        String val = "";

        if (br == null) {
            //read user status 
            Process proc = Runtime.getRuntime().exec("passwd -S " + user);
            java.io.InputStream is = proc.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");

            int i = 0;
            if (s.hasNextLine()) {
                val = s.next();
                //System.out.println(i+":"+val);

            } else {
                val = "";
            }
        } else {

            for (int h = 0; h < usersRawLine.size(); h++) {
                String line = usersRawLine.get(h);
                if (line.toLowerCase().startsWith(user.toLowerCase())) {
                    //System.out.println(line);
                    val = line;
                    break;
                }
            }

        }

        return val;
    }

    private int getPasswordAge(String inputDate) throws ParseException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        int passAge;
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Date currentDate = new Date();
        Date passSetDate = formatter.parse(inputDate);
        long diff = currentDate.getTime() - passSetDate.getTime();
        long daysL = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        passAge = (int) daysL;
        return passAge;
    }

    void writeToFile(ArrayList inputArr) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
        for (int c = 0; c < inputArr.size(); c++) {
            writer.println(inputArr.get(c));
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        //locked password (L), has no password (NP), has a usable password (P), has a usable password older 90 days (P90)
        System.out.println("Scripting directory = " + System.getProperty("user.dir"));
        String arg = "L";
        try {
            LinuxUserStatus lu = new LinuxUserStatus(args[0]);
            lu.getUserstatus();
        } catch (ArrayIndexOutOfBoundsException exception) {
            System.out.println("Util syntax is not correct. \nUse one of the possible key. Available keys: locked password (L), has no password (NP), has a usable password (P), has a usable password older 90 days (P90)");

        }
    }

}
