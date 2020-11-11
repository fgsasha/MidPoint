/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata;

import hrdata.util.FileUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import mail.EmailUtil;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;

/**
 *
 * @author o.nekriach
 */
public class JiraEmployeesEvaluationTest {

    JiraEmployeesData jed;
    private String etalonFields;
    private String etalonEmployees;
    private Properties prop;
    int splitLenth = -100;
    final String MAILSUBJECT = "Jira-Idm Sync Validation error";
    final String[] EMPLOYEESTATUSES = {"Employed", "Dismissed", "Maternity Leave","Sick Leave", "Draft"};
    final String DISSMISALJIRAFIELD = "Dismiss";
    String MASSCHECKFIELDS = "Status,Employee,Dismissal,Business Email";
    final String SUMMARY = "Summary";
    final String ISSUE = "Issue key";

    public void setJiraInst(JiraEmployeesData jiraInst) {
        this.jed = jiraInst;
    }

    JiraEmployeesEvaluationTest(JiraEmployeesData jira) throws JiraException {
        if (jira == null) {
            throw new NullPointerException("Jira client is not initialized");
        } else {
            setJiraInst(jira);
        }
        initEtalonData();
    }

    void initEtalonData() {
        etalonFields = jed.getEtalonFields();
        etalonEmployees = jed.getEtalonEmployees();
        prop = jed.getProp();
        if (etalonFields == null || etalonEmployees == null || prop == null) {
            throw new NullPointerException("Etalon: fields or issue keys are not set in " + jed.JIRAPROPERTIESFILE);
        }
    }

    Boolean validateUserDataToEtalon() throws JiraException, IOException, URISyntaxException, Exception {
        Boolean checkResult = false;
        String[] input = etalonEmployees.split(jed.DELIMITER);
        for (int k = 0; k < input.length; k++) {
            Issue issue = jed.ctx.getIssue(input[k]);
            if (prop.getProperty(input[k]) == null || (etalonFields.split(jed.DELIMITER, splitLenth).length != prop.getProperty(input[k]).split(jed.DELIMITER, splitLenth).length)) {
                throw new NullPointerException("\nThere is no Etalon data for key " + input[k] + " in file " + jed.JIRAPROPERTIESFILE + " \nOr number of fields in Etalon User is less then Etalon Data fields number.");
            }
            if (jed.getOneEmployeeRecord(issue, etalonFields).equals(prop.getProperty(input[k])) || (jed.getOneEmployeeRecord(issue, etalonFields).isEmpty() && prop.getProperty(input[k]).isEmpty())) {
                checkResult = true;
            } else {
                checkResult = false;
                String msg = getResultMessage(input[k], jed.getOneEmployeeRecord(issue, etalonFields), prop.getProperty(input[k]), etalonFields);
                sendEmail(msg);
                throw new VerifyError("Jira user data did not pass validation through Etalon data. Either something was changed in Jira HREM project or Etalon value should be updated in config file " + jed.JIRAPROPERTIESFILE);
            }
        }
        return checkResult;
    }

    Boolean validateEmployeeStatus(String input) throws MessagingException, UnsupportedEncodingException {
        Boolean checkResult = false;
        for (String status : EMPLOYEESTATUSES) {
            if (status.equalsIgnoreCase(input)) {
                checkResult = true;
                break;
            }
        }
        if (!checkResult) {
            sendEmail("Employee status new or not valid: " + input);
            throw new VerifyError("Employee status new or not valid: " + input);
        }

        return checkResult;
    }

    Boolean validateMassUpdate(Map<String, Map<String, String>> inputMap) throws IOException, MessagingException {
        Boolean checkResult = false;
        Map<String, Integer> threshold = jed.getValidationThreshold();
        String checkFields = "";
        if (threshold == null || threshold.isEmpty()) {
            checkResult = true;
            return checkResult;
        } else {
            Iterator<String> iter = threshold.keySet().iterator();
            while (iter.hasNext()) {
                checkFields = checkFields + jed.DELIMITER + iter.next();
            }
            checkFields = checkFields.substring(1);
        }
        String fileName = jed.getFileName();
        String cacheFileName = fileName;
        FileUtil futil = new FileUtil();
        if (!futil.fileExist(cacheFileName)) {
            checkResult = true;
            System.out.println("File cache  " + cacheFileName + " have not found. Skip validateMassUpdate evaluation and continue processing of online data");
            return checkResult;
        }
        BufferedReader br = futil.readDataFromFile(cacheFileName);
        Map<String, Map<String, String>> cachedMap = futil.getCsvToMap(jed.getPrimaryKey(), br);
        Map<String, ArrayList<HashMap<String, String[]>>> result = getChangesCompareToCache(checkFields, cachedMap, inputMap); // for test MASSCHECKFIELDS

        // Compare threshold and checkresult
        Iterator<String> iter2 = threshold.keySet().iterator();
        while (iter2.hasNext()) {
            String field = iter2.next();
            if (field != null && !result.isEmpty() && result.get(field) != null) {
                System.out.println("Number updates of checked field " + field + ": " + result.get(field).size());
            }
            if (!result.containsKey(field) || result.get(field).size() < threshold.get(field)) {
                result.remove(field);
            }
        }
        if (result.isEmpty()) {
            checkResult = true;
            return checkResult;
        }

        String msg = "Due to the update of many HR data, synchronization with Jira was stopped. Please solve the situation manually";
        msg = msg + "<br/>" + getHtmlTableFromMap(result);

        sendEmail(msg);
        throw new VerifyError("There are many updates sync was stoped. Please resolve situation manualy");

        //checkResult=false;
        //return checkResult;
    }

    Boolean validateDismissalDate(Issue issue) throws IOException, URISyntaxException, Exception {
        Boolean checkResult = false;
        String dissmisal = jed.getHREMFieldValue(DISSMISALJIRAFIELD, issue);
        if (dissmisal != null && !dissmisal.isEmpty()) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            Date dismissalDate = df.parse(dissmisal.toString());
            long dismissalDatePlusOneDay = dismissalDate.getTime() + 24 * 60 * 60 * 1000;
            dismissalDate = new Date(dismissalDatePlusOneDay);
            if (dismissalDate.before(now)) {
                String msg = "User " + issue.getKey() + " has status !Dismissed but has Dismissal date in the Past";
                sendEmail(msg);
                throw new VerifyError("User " + issue.getKey() + " has status !Dismissed but has Dismissal date in the Past");
            } else {
                checkResult = true;
            }
        }
        return checkResult;
    }

    private String getResultMessage(String issue, String jiraData, String etalonData, String fields) {
        String msg = null;
        String[] jiraDataAr = jiraData.split(jed.DELIMITER, splitLenth);
        String[] etalonDataAr = etalonData.split(jed.DELIMITER, splitLenth);
        String[] fieldsAr = fields.split(jed.DELIMITER, splitLenth);
        if (jiraDataAr.length != etalonDataAr.length) {
            msg = "Etalon user data has wrong number of elements compare to Jira data<br/>" + "Fields     : " + fields + "<br/>Etalon data: " + etalonData + "<br/>Jira data  : " + jiraData;
        } else {
            for (int i = 0; i < fieldsAr.length; i++) {
                int s = i + 1;
                if (!etalonDataAr[i].equals(jiraDataAr[i])) {
                    if (msg == null) {
                        msg = "<p>" + s + ". Field name: " + fieldsAr[i] + "<br/>  Jira data: " + jiraDataAr[i] + "<br/>    Expected data: " + etalonDataAr[i] + "<br/></p>";
                    } else {
                        msg = "<p>" + msg + s + ". Field name: " + fieldsAr[i] + "<br/>  Jira data: " + jiraDataAr[i] + "<br/>  Expected data: " + etalonDataAr[i] + "<br/></p>";
                    }
                }
            }

            if (msg == null) {
                msg = "Something wrong in Jira HREM project with user " + issue;
            } else {
                String msg2 = "Something was changed in Jira HREM project. Syncronization is impossible. Please contact Jira administrator.<br/>If everething is correct in Jira , please updated  Etalon value in config file ";
                msg2 = msg2 + jed.JIRAPROPERTIESFILE + "<br/>";
                msg2 = msg2 + "<br/>Problem employee card id: " + issue + "<br/>Field number#";
                msg = msg2 + msg;
            }
        }
        return msg;
    }

    /**
     * Send email in case of an error with details. All parameters constanta or
     * can be set via jira.properties
     *
     * @param msg Body text of the message
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    private void sendEmail(String msg) throws MessagingException, UnsupportedEncodingException {
        EmailUtil mail = new EmailUtil();
        mail.setSmtpHostServer(jed.getSmtpHostServer());
        mail.setPort(jed.getPort());
        mail.setStarttlsEnable("true");
        mail.setSmtpAuth("false");
        mail.setMailDebug(jed.getMailDebug());
        mail.setFromAddress(jed.getFromAddress());
        Session mlx = mail.Initialization();
        mail.sendEmail(mlx, jed.getAdminEmail(), MAILSUBJECT, msg);
    }

    private Map<String, ArrayList<HashMap<String, String[]>>> getChangesCompareToCache(String MASSCHECKFIELDS, Map<String, Map<String, String>> cached, Map<String, Map<String, String>> current) {
        // HashMap<FIELD, ArrayList<HashMap<PRIMARYKEY, {OLDVALUE, NEWVALUE}>>>();
        Map<String, ArrayList<HashMap<String, String[]>>> output = new HashMap<String, ArrayList<HashMap<String, String[]>>>();
        String[] checkFieldsAr = MASSCHECKFIELDS.split(jed.DELIMITER, splitLenth);
        if (current.isEmpty()) {
            throw new VerifyError("Online export is Empty");
        }
        Iterator<String> curit = current.keySet().iterator();
        //iterate through all employees in online export
        while (curit.hasNext()) {
            String primaryKey = curit.next();
            if (primaryKey.isEmpty() || !cached.containsKey(primaryKey)) {
                continue;
            }
            // Compare all values from checkFieldsAr through cached and current data. If different put to the output
            for (String field : checkFieldsAr) {
                String summary = current.get(primaryKey).get(SUMMARY);
                String issue = current.get(primaryKey).get(ISSUE);
                String cachedValue = cached.get(primaryKey).get(field);
                String currentValue = current.get(primaryKey).get(field);
                if ((cachedValue == null && currentValue != null) || (cachedValue != null && currentValue == null) || !cachedValue.equalsIgnoreCase(currentValue)) {
                    if (output.isEmpty() || !output.containsKey(field) || output.get(field).isEmpty()) {
                        String[] values = {cachedValue, currentValue, issue, summary};
                        HashMap valuesMap = new <String, String[]>HashMap();
                        valuesMap.put(primaryKey, values);
                        ArrayList list = new <HashMap<String, String[]>>ArrayList();
                        list.add(valuesMap);
                        output.put(field, list);
                    } else {
                        {
                            String[] values = {cachedValue, currentValue, issue, summary};
                            HashMap valuesMap = new <String, String[]>HashMap();
                            valuesMap.put(primaryKey, values);
                            output.get(field).add(valuesMap);

                        }

                    }
                }

            }

        }
        return output;
    }

    private String getHtmlTableFromMap(Map<String, ArrayList<HashMap<String, String[]>>> result) {
        //Convert result to table
        StringBuilder stringMapTable = new StringBuilder();
        stringMapTable.append("<table>");
        stringMapTable.append("<tr><td>" + "Field Name" + "</td><td>" + "Employee ID" + "</td><td>" + "Jira ID" + "</td><td>" + "Full Name" + "</td><td>" + "OLD Value" + "</td><td>" + "NEW Value" + "</td></tr>");
        Iterator it = result.keySet().iterator();
        while (it.hasNext()) {
            String field = it.next().toString();
            ArrayList<HashMap<String, String[]>> arr = result.get(field);
            for (int i = 0; i < arr.size(); i++) {
                HashMap<String, String[]> arrayEl = arr.get(i);
                Iterator it2 = arrayEl.keySet().iterator();
                while (it2.hasNext()) {
                    String primaryKey = it2.next().toString();

                    stringMapTable.append("<tr><td>" + field + "</td><td>" + primaryKey + "</td><td>" + arrayEl.get(primaryKey)[2] + "</td><td>" + arrayEl.get(primaryKey)[3] + "</td><td>" + arrayEl.get(primaryKey)[0] + "</td><td>" + arrayEl.get(primaryKey)[1] + "</td></tr>");

                }
            }
        }

        String mapTable = stringMapTable.toString();

        return mapTable;
    }

}
