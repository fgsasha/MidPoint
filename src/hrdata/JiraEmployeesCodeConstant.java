package hrdata;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;


/**
 *
 * @author o.nekriach
 * Mapping provided by Pavel Avens
 */
public class JiraEmployeesCodeConstant {
    private Map<String, String> displaynameNameHMap = new HashMap<String, String>();
    private Map<String, String> constFromFile = new HashMap<String, String>();
    private String fileName;

    static final String BIRTHDAY = "Birthday";
    static final String BUSINESSEMAIL = "Business Email";
    static final String CELLPHONE = "Cell Phone";
    static final String COMPANY = "Company";
    static final String DEPARTMENT = "Department";
    static final String EMPLOYEE = "Employee";
    static final String EMPLOYMENT = "Employment";
    static final String ENDOFTRIAL = "End of Trial";
    static final String HOMEPHONE = "Home Phone";
    static final String IDCODE = "ID Code";
    static final String MANAGER = "Manager";
    static final String PERSONALEMAIL = "Personal Email";
    static final String POSITION = "Position";
    static final String TRANSLITERATION = "Transliteration";
    static final String LEVEL1 = "Level 1";
    static final String LEVEL2 = "Level 2";
    static final String LEVEL3 = "Level 3";
    static final String LEVEL4 = "Level 4";
    static final String LEVEL5 = "Level 5";
    static final String LEVEL6 = "Level 6";
    static final String LEVEL7 = "Level 7";
    static final String LEVEL8 = "Level 8";
    static final String LEVEL9 = "Level 9";
    static final String TYPE = "Type";
    static final String DIVISION = "Division";
    static final String SUBDIVISION = "Subdivision";

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private void initNameByDisplayName() {
        displaynameNameHMap.put(BIRTHDAY, "birthday");
        displaynameNameHMap.put(BUSINESSEMAIL, "businessEmail");
        displaynameNameHMap.put(CELLPHONE, "cellPhone");
        displaynameNameHMap.put(COMPANY, "company");
        displaynameNameHMap.put(DEPARTMENT, "department");
        displaynameNameHMap.put(EMPLOYEE, "employee");
        displaynameNameHMap.put(EMPLOYMENT, "employment");
        displaynameNameHMap.put(ENDOFTRIAL, "endOfTrial");
        displaynameNameHMap.put(HOMEPHONE, "homePhone");
        displaynameNameHMap.put(IDCODE, "idCode");
        displaynameNameHMap.put(MANAGER, "manager");
        displaynameNameHMap.put(PERSONALEMAIL, "personalEmail");
        displaynameNameHMap.put(TRANSLITERATION, "transliteration");
        displaynameNameHMap.put(LEVEL1, "level1");
        displaynameNameHMap.put(LEVEL2, "level2");
        displaynameNameHMap.put(LEVEL3, "level3");
        displaynameNameHMap.put(LEVEL4, "level4");
        displaynameNameHMap.put(LEVEL5, "level5");
        displaynameNameHMap.put(LEVEL6, "level6");
        displaynameNameHMap.put(LEVEL7, "level7");
        displaynameNameHMap.put(LEVEL8, "level8");
        displaynameNameHMap.put(LEVEL9, "level9");
        displaynameNameHMap.put(TYPE, "type");
        displaynameNameHMap.put(DIVISION, "division");
        displaynameNameHMap.put(SUBDIVISION, "Subdivision");
    }

    public Map<String, String> getDisplaynameNameHMap() {
        return this.displaynameNameHMap;
    }

    private void initCodeConstantFromFile() throws FileNotFoundException, IOException, Exception {
        if (fileName == null) {
            throw new Exception("Please set file name");
        }
        Properties prop = new Properties();
        InputStream input = null;
        input = new FileInputStream(fileName);

        // load a file
        prop.load(input);
        Set<Map.Entry<Object, Object>> code = prop.entrySet();

        constFromFile = new HashMap<String, String>();
        for (Entry<Object, Object> entry : code) {
            constFromFile.put(entry.getKey().toString(), entry.getValue().toString());
        }
        input.close();
    }

    String getNameByDisplayName(String displayName) {
        String name = null;
        if (displaynameNameHMap.isEmpty()) {
            initNameByDisplayName();
        }
        if (!displaynameNameHMap.isEmpty() && displayName != null) {
            name = displaynameNameHMap.get(displayName);
        }
        return name;
    }

    public String getIDbyDisplayNameFromFile(String displayName) throws IOException, Exception {
        String id = null;
        String name = getNameByDisplayName(displayName);
        if (name == null) {
            name = displayName;
        }
        if (constFromFile.isEmpty()) {
            initCodeConstantFromFile();
        }
        if (!constFromFile.isEmpty() && name != null) {
            id = constFromFile.get(name);
        }
        return id;
    }

}
