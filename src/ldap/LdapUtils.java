/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

/**
 *
 * @author onekriach
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.*;
import javax.naming.directory.*;

public class LdapUtils {

    private String ldapURL = "ldaps://example.com:636";
    private String auth = "simple";
    private String bindUser = "uid=administrator,ou=Services,dc=example,dc=com";
    private String cred = "<secret>";
    private String ldapSearchBase = "dc=example,dc=com";

    public void setLdapSearchBase(String ldapSearchBase) {
        this.ldapSearchBase = ldapSearchBase;
    }

    private Hashtable<String, String> environment = new Hashtable<String, String>();

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

    LdapUtils() {
    }

    LdapUtils(String fileProperties) throws FileNotFoundException, IOException {
        System.out.println("----------------------------------------------------------------");
        String inputParameter = null;
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream(new File("ldap.properties"));

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

        if (verbose != null && verbose.equalsIgnoreCase("true")) {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            System.out.println("#############################jsonparser.properties#################################");
            System.out.println("verbose: " + verbose);
            System.out.println("ldapURL: " + ldapURL);
            System.out.println("bindUser: " + bindUser);
            System.out.println("cred: " + "<secret>");
            System.out.println("ldapSearchBase: " + ldapSearchBase);
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
            System.out.println(context.getEnvironment());

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
}
