/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

import javax.naming.directory.DirContext;

/**
 *
 * @author onekriach
 */
public class PasswordExpirationNotification {
    
    public static void main(String [] args)
	{
         PasswordExpirationNotification sup= new PasswordExpirationNotification();
         sup.run();
         sup.close();
        
        
        }

    private void run() {
    
   
    
    }
    void getLdapAccountsToSendNotification(){
        LdapUtils util = new LdapUtils();
        DirContext ctx = util.connect();
        LdapFilter lf = new LdapFilter();
        getAllSearchedAccounts();
    }
    
    void sendNotification(){
    
    
    }
    
    void exportResultInFile(){
    
    }
    private void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
