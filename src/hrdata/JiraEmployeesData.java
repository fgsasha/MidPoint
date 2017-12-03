/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestException;
import net.sf.json.JSON;
import org.json.*;

/**
 *
 * @author onekriach
 */
public class JiraEmployeesData {

    private final JiraClient ctx;
    private final BasicCredentials creds;
    private String file = "";
    private Map fMap = new HashMap();
    private Map valuesMap = new HashMap();
    String delimiter = ",";

    /**
     *
     * @param jiraURL the value of jira URL like https://atlassian.net
     * @param secret the value of secret
     * @throws JiraException
     */
    public JiraEmployeesData(String jiraURL, String secret, String fileName) throws JiraException, RestException, IOException, URISyntaxException {
        creds = new BasicCredentials("iam.security", secret);
        ctx = new JiraClient(jiraURL, creds);
        file = fileName;

    }

    public void run() throws JiraException, RestException, IOException, URISyntaxException, ParseException {

        // display time and date using toString()
        System.out.println("Started at: " + new Date().toString());

        String fieldList = this.getFieldsList();
        ArrayList allEmp = this.getAllEmployees();
        allEmp = this.getUniqueEmployees(allEmp);

        valuesMap.put("fieldList", fieldList);

        for (int k = 0; k < allEmp.size(); k++) {
            Issue issue = (Issue) allEmp.get(k);
            valuesMap.put(issue.getKey(), this.getOneEmployeeRecord(issue, fieldList));
        }

        this.exportDataToCSV(file);

        // display time and date using toString()
        System.out.println("Finished at: " + new Date().toString());
    }

    public ArrayList getAllEmployees() throws JiraException {

        ArrayList all = new ArrayList();

//            /* Search for issues */
        Issue.SearchResult sr = ctx.searchIssues("project=HREM");
        System.out.println("Total: " + sr.total);
        Iterator<Issue> it = sr.iterator();
        while (it.hasNext()) {
            Issue issueSR = it.next();
            all.add(issueSR);
            System.out.println("issueSR: " + issueSR.getKey() + " : "
                    + issueSR.getSummary());
        }
        return all;

    }

    public ArrayList getActiveEmployees() throws JiraException {
        ArrayList all = new ArrayList();

        ///* Search for issues  - only active employees/
        Issue.SearchResult sr = ctx.searchIssues("project=HREM and Status!=Dismissed");
        System.out.println("Total: " + sr.total);
        Iterator<Issue> it = sr.iterator();
        while (it.hasNext()) {
            Issue issueSR = it.next();
            all.add(issueSR);
            System.out.println("issueSR: " + issueSR.getKey() + " : "
                    + issueSR.getSummary());
        }
        return all;
    }

    public ArrayList getFiredEmployees() throws JiraException {
        ArrayList all = new ArrayList();

        ///* Search for issues - only dismissed employees/
        Issue.SearchResult sr = ctx.searchIssues("project=HREM and Status=Dismissed");
        System.out.println("Total: " + sr.total);
        Iterator<Issue> it = sr.iterator();
        while (it.hasNext()) {
            Issue issueSR = it.next();
            all.add(issueSR);
            System.out.println("issueSR: " + issueSR.getKey() + " : "
                    + issueSR.getSummary());
        }
        return all;
    }

    public ArrayList getUniqueEmployees(ArrayList allemp) throws RestException, IOException, URISyntaxException, ParseException {
        ArrayList output = new ArrayList();
        Map mapL1 = new HashMap();

        String idCode = "";
        for (int k = 0; k < allemp.size(); k++) {
            Issue issue = (Issue) allemp.get(k);
            // group employees by their ID Code and get their status
            idCode = this.getFieldValue("ID Code", issue);
            String status = "";
            if (this.getFieldValue("Status", issue).equalsIgnoreCase("Dismissed")) {
                status = this.getFieldValue("Dismissal", issue);
            } else {
                //status= this.getFieldValue("Status", issue);
                status = "Employed"; // for easiest searching
            }

            Map mapL2 = new HashMap();
            if (mapL1.containsKey(idCode)) {

                mapL2 = (Map) mapL1.get(idCode);
                mapL2.put(issue, status);
                mapL1.replace(idCode, mapL2);
            } else {
                mapL2.put(issue, status);
                mapL1.put(idCode, mapL2);
            }
        }
        //Get  unique employees issue cosidering employees status. For one employee only one record
        Iterator keys = mapL1.keySet().iterator();
        while (keys.hasNext()) {
            String idCodeL1 = (String) keys.next();
            Map mapL2 = new HashMap();
            mapL2 = (Map) mapL1.get(idCodeL1);
            Iterator keysL2 = mapL2.keySet().iterator();

            while (keysL2.hasNext()) {
                Issue key = (Issue) keysL2.next();

                if (mapL2.containsValue("Employed")) {
                    if (mapL2.get(key).toString().equalsIgnoreCase("Employed")) {
                        output.add(key);
                        break;
                    }
                } else {
                    //TODO
                    String recent = this.getRecent(mapL2.values());
                    if (mapL2.get(key).toString().equalsIgnoreCase(recent)) {
                        output.add(key);
                        break;
                    }

                }

            }

        }

        return output;

    }

    public String getFieldsList() {
        //String fieldList="Summary,Issue key,Issue id,Issue Type,Status,Project key,Project name,Project type,Project lead,Project description,Project url,Assignee,Reporter,Creator,Created,Updated,Last Viewed,Resolved,Due Date,Environment,Original Estimate,Remaining Estimate,Time Spent,Work Ratio,Σ Original Estimate,Σ Remaining Estimate,Σ Time Spent,Security Level,Employment Request,Linked Profile,Remunerations,Vacations,Attachment,1-month check,2-months check,2-weeks check,3-months final check,Actual Address,Birthday,Brand,Business Email,Cell Phone,City,Co-manager,Company,Department,Development,Dismissal,Employee,Employee Review,Employment,End of Trial,Epic Color,Epic Link,Epic Name,Epic Status,External issue ID,First Name,Former Name,Home Phone,ID Code,Impact,Investigation reason,Last Name,Manager,Middle Name,New Position,Notes,Operational categorization,Original Form,Pending reason,Personal Email,Position,Postal Code,Queue,Raised during,Rank,Reference,Registered Address,Request Type,Request participants,Residency,Root cause,Satisfaction rating,Skype,Story Points,Supervisors,Test sessions,Testing status,Urgency,Vacation,Workplace,[CHART] Date of First Response,Comment";
        String fieldList = "Summary,Issue key,Issue id,Issue Type,Status,Created,Updated,Birthday,Business Email,Cell Phone,Co-manager,Company,Department,Dismissal,Employee,Employment,End of Trial,First Name,Former Name,Home Phone,ID Code,Issued Tangibles,Last Name,Manager,Middle Name,Original Form,Personal Email,Position,HRUID";
        return fieldList;
    }

    public String getFieldValue(String name, Issue issue) throws RestException, IOException, URISyntaxException {
        String output = "";

        if (name.equalsIgnoreCase("Issue key")) {
            output = issue.getKey();
        } else if (name.equalsIgnoreCase("Issue id")) {
            output = issue.getId();
        } else if (name.equalsIgnoreCase("Issue Type")) {
            output = issue.getIssueType().toString();
        } else if (name.equalsIgnoreCase("Status")) {
            output = issue.getStatus().toString();
        } else if (name.equalsIgnoreCase("Business Email")) {
            output = issue.getField(getFieldKeyByName(name).toString()).toString();
            if (output.isEmpty() || output.equals("null")) {
                output = issue.getField(getFieldKeyByName("Employee").toString()).toString();
                if (output.contains("/rest/api/2/user?username")) {
                    output = new JSONObject(output).getString("emailAddress").toString();
                }
            }
        } else if (name.equalsIgnoreCase("HRUID")) {
            String personId = issue.getField(getFieldKeyByName("ID Code").toString()).toString();
            String salt = issue.getField(getFieldKeyByName("Birthday").toString()).toString();
            if (!personId.equals("null") && !salt.equals("null")) {
                ConvertUtils cu = new ConvertUtils();
                output = cu.getHrIdNumberRev(personId, salt);
            } else {
                output = "null";
            }
        } else if (!(this.getFieldKeyByName(name) == null || this.getFieldKeyByName(name).isEmpty())) {

            //System.out.println(name+" : "+this.getFieldKeyByName(name));
            if (issue.getField(getFieldKeyByName(name).toString()) != null) {
                output = issue.getField(getFieldKeyByName(name).toString()).toString();
                if (output.contains("/rest/api/2/customFieldOption")) {
                    output = new JSONObject(output).getString("value").toString();
                }
                if (output.contains("/rest/api/2/user?username")) {
                    output = new JSONObject(output).getString("name").toString();
                }
            }
        }
        output = output.replace(delimiter, "").replace("null", "").replace("?", "");
        return output;
    }

    public String getFieldKeyByName(String name) throws RestException, IOException, URISyntaxException {
        String output = "";
        if (fMap.containsKey(name)) {
            output = (String) fMap.get(name);
        } else {
            JSON js = ctx.getRestClient().get("/rest/api/2/field");
            JSONArray jsarray = new JSONArray(js.toString());

            for (int i = 0; i < jsarray.length(); i++) {
                JSONObject jso = (JSONObject) jsarray.get(i);
                String value = jso.getString("name");
                //System.out.println("name="+value);
                if (name.equalsIgnoreCase(value)) {
                    output = jso.getString("key");
                    break;
                }
            }
            fMap.put(name, output);
        }
        return output;
    }

    public String getOneEmployeeRecord(Issue issue, String fieldList) throws RestException, IOException, URISyntaxException {
        String output = "";
        for (int i = 0; i < fieldList.split(delimiter).length; i++) {
            //System.out.println(fieldList.split(",")[i] + "=" + this.getFieldValue(fieldList.split(",")[i], issue));

            if (i < fieldList.split(delimiter).length - 1) {
                output = output + this.getFieldValue(fieldList.split(delimiter)[i], issue) + delimiter;
            } else {
                output = output + this.getFieldValue(fieldList.split(delimiter)[i], issue);
            }

        }
        //System.out.println(output);
        return output;
    }

    public void exportDataToCSV(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");

        if (!valuesMap.isEmpty()) {
            writer.println(valuesMap.get("fieldList"));
            Iterator key = valuesMap.keySet().iterator();
            while (key.hasNext()) {
                String keyElement = (String) key.next();
                if (!keyElement.equals("fieldList")) {
                    String value = (String) valuesMap.get(keyElement);
                    writer.println(value);
                }
            }

        }
        writer.close();

    }

    public static void main(String[] args) throws JiraException, FileNotFoundException, IOException, RestException, URISyntaxException, ParseException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        Properties prop = new Properties();
        InputStream input = null;

        input = new FileInputStream("jira.properties");

        // load a properties file
        prop.load(input);

        // get the property value and print it out
        System.out.println("#############################jsonparser.properties#################################");
        System.out.println("jiraURL: " + prop.getProperty("jiraURL"));
        String jiraURL = prop.getProperty("jiraURL");
        System.out.println("jiraSecret: " + "secret");
        String secret = prop.getProperty("jiraSecret");
        System.out.println("fileName: " + prop.getProperty("fileName"));
        String fileName = prop.getProperty("fileName");

        JiraEmployeesData jira = new JiraEmployeesData(jiraURL, secret, fileName);
        jira.run();

    }

    private String getRecent(Collection values) throws ParseException {
        String output="";
        Date outputDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Iterator iter = values.iterator();
        //2017-11-10
        while(iter.hasNext()){
            Date date = formatter.parse((String) iter.next());
            if(outputDate==null || date.before(outputDate)){
            outputDate=date;            
            }
         }     
        output=formatter.format(outputDate);
        System.out.println("output="+output);
        return output;
    }

}
