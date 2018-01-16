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
import java.util.Hashtable;
import javax.naming.*;
import javax.naming.directory.*;

public class LdapUtils {

    private static Hashtable<String, String> environment = new Hashtable<String, String>();

    public LdapUtils() {
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, "ldap://<hostname>:389");
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, "<Login DN>");
        environment.put(Context.SECURITY_CREDENTIALS, "<password>");

    }

    public DirContext connect() {
        DirContext context = null;
        try {
            context = new InitialDirContext(environment);
            System.out.println("Connected..");
            System.out.println(context.getEnvironment());
            context.close();
        } catch (AuthenticationNotSupportedException exception) {
            System.out.println("The authentication is not supported by the server");
        } catch (AuthenticationException exception) {
            System.out.println("Incorrect password or username");
        } catch (NamingException exception) {
            System.out.println("Error when trying to create the context");
        }
        return context;
    }
    public void close(DirContext context) throws NamingException{
    if(context!=null)
    {
    context.close();
    }
    }    
}
