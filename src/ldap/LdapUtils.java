/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

/**
 *
 * @author o.nekriach
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.naming.*;
import javax.naming.directory.*;

public class LdapUtils {

    private String ldapURL = "ldaps://example.com:636";
    private String auth = "simple";
    private String bindUser = "uid=administrator,ou=Services,dc=example,dc=com";
    private String cred = "<secret>";
    private String ldapSearchBase = "dc=example,dc=com";
    private String accountsLdapSearchFilter = "(&(objectClass=inetOrgPerson)(uid=*))";
    private String policiesLdapSearchFilter = "(objectClass=pwdPolicy)";
    private Hashtable<String, String> environment = new Hashtable<String, String>();
    private static Map<String, String> pwdMaxAgeDaysPolicies = new HashMap<>();
    private String defaultPolicyDN = "cn=DefaultPasswordPolicy,ou=PwPolicy,dc=example,dc=com";

    public void setLdapSearchBase(String ldapSearchBase) {
        this.ldapSearchBase = ldapSearchBase;
    }

    public void setLdapURL(String ldapURL) {
        this.ldapURL = ldapURL;
    }

    public void setBindUser(String bindUser) {
        this.bindUser = bindUser;
    }

    public void setCred(String cred) {
        this.cred = cred;
    }

    public void setEnvironment(Hashtable<String, String> environment) {
        this.environment = environment;
    }

    private void setAccountsLdapSearchFilter(String accountsLdapSearchFilter) {
        this.accountsLdapSearchFilter = accountsLdapSearchFilter;
    }

    public void setPoliciesLdapSearchFilter(String policiesLdapSearchFilter) {
        this.policiesLdapSearchFilter = policiesLdapSearchFilter;
    }

    public static void setPwdMaxAgePolicies(NamingEnumeration<SearchResult> policies) throws NamingException {
        pwdMaxAgeDaysPolicies = getMapPolicyPwdMaxAge(policies);
    }

    public String getDefaultPolicyDN() {
        return defaultPolicyDN;
    }

    public void setDefaultPolicyDN(String defaultPolicyDN) {
        this.defaultPolicyDN = defaultPolicyDN;
    }

    public String getLdapSearchBase() {
        return ldapSearchBase;
    }

    public static Map<String, String> getPwdMaxAgeDaysPolicies() {
        return pwdMaxAgeDaysPolicies;
    }

    LdapUtils() {
    }

    LdapUtils(String fileProperties) throws FileNotFoundException, IOException {
        System.out.println("----------------------------------------------------------------");
        String inputParameter = null;
        Properties prop = new Properties();
        FileInputStream input;
        if (fileProperties == null || fileProperties.isEmpty()) {
            input = new FileInputStream(new File("ldap.properties"));
        } else {
            input = new FileInputStream(new File(fileProperties));
        }
        // load a properties file
        prop.load(new InputStreamReader(input, Charset.forName("UTF-8")));

        // get the property value and print it out
        String verbose = prop.getProperty("verbose", "true");
        String ldapURL = prop.getProperty("ldapURL", "ldaps://example.com:636");
        this.setLdapURL(ldapURL);
        String bindUser = prop.getProperty("bindUser", "uid=administrator,ou=Services,dc=example,dc=com");
        this.setBindUser(bindUser);
        String cred = prop.getProperty("cred", "secret");
        this.setCred(cred);
        String ldapSearchBase = prop.getProperty("ldapSearchBase", "dc=example,dc=com");
        this.setLdapSearchBase(ldapSearchBase);
        String ldapSearchFilter = prop.getProperty("ldapSearchFilter", "(&(objectClass=inetOrgPerson)(uid=*))");
        this.setAccountsLdapSearchFilter(ldapSearchFilter);
        String defaultPolicyDN = prop.getProperty("defaultPolicyDN", "cn=DefaultPasswordPolicy,ou=PwPolicy,dc=example,dc=com");
        this.setDefaultPolicyDN(defaultPolicyDN);

        if (verbose != null && verbose.equalsIgnoreCase("true")) {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            System.out.println("#############################jsonparser.properties#################################");
            System.out.println("verbose: " + verbose);
            System.out.println("ldapURL: " + ldapURL);
            System.out.println("bindUser: " + bindUser);
            System.out.println("cred: " + "<secret>");
            System.out.println("ldapSearchBase: " + ldapSearchBase);
            System.out.println("ldapSearchFilter: " + ldapSearchFilter);
            System.out.println("defaultPolicyDN: " + defaultPolicyDN);
            System.out.println("#############################EOF#################################");
        }

    }

    public void Initialization() {

        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapURL);
        environment.put(Context.SECURITY_AUTHENTICATION, auth);
        environment.put(Context.SECURITY_PRINCIPAL, bindUser);
        environment.put(Context.SECURITY_CREDENTIALS, cred);

    }

    public DirContext connect() {
        this.Initialization();
        DirContext context = null;
        try {
            context = new InitialDirContext(environment);
            System.out.println("Connected..");
            // System.out.println(context.getEnvironment());

        } catch (AuthenticationNotSupportedException exception) {
            System.out.println("The authentication is not supported by the server");
        } catch (AuthenticationException exception) {
            System.out.println("Incorrect password or username");
        } catch (NamingException exception) {
            System.out.println("Error when trying to create the context");
        }
        return context;
    }

    public void close(DirContext context) throws NamingException {
        if (context != null) {
            context.close();
        }
    }

    String getAccountsLdapSearchFilter() {
        return accountsLdapSearchFilter;
    }

    public String getPoliciesLdapSearchFilter() {
        return policiesLdapSearchFilter;
    }

    /**
     * Return map. DN of password policy as key and pwdMaxAge in days as value
     * from input SearchResult
     *
     * @param policies - LDAP SearchResult
     * @return DN of password policy as key and pwdMaxAge in days as value
     * @throws NamingException
     */
    static Map<String, String> getMapPolicyPwdMaxAge(NamingEnumeration<SearchResult> policies) throws NamingException {
        Map<String, String> output = new HashMap<>();
        int i = 0;
        while (policies.hasMore()) {
            SearchResult policy = policies.next();
            String policyDN = policy.getNameInNamespace();
            Attributes attributes = (Attributes) policy.getAttributes();
            String pwdMaxAge = null;
            if (attributes.get("pwdMaxAge") != null) {
                pwdMaxAge = (String) attributes.get("pwdMaxAge").get();
                int pwdMaxAgeDays = (int) (Integer.parseInt(pwdMaxAge) / (60 * 60 * 24));
                output.put(policyDN, Integer.toString(pwdMaxAgeDays));
            }
        }
        return output;
    }
}
