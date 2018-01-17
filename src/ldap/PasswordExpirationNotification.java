/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ldap;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

/**
 *
 * @author onekriach
 */
public class PasswordExpirationNotification {

    public static void main(String[] args) {
        PasswordExpirationNotification sup = new PasswordExpirationNotification();
        sup.run();
        sup.close();

    }

    private void run() {

    }

    void getLdapAccountsToSendNotification() throws NamingException {
        LdapUtils util = new LdapUtils();
        DirContext ctx = util.connect();
        LdapFilter lf = new LdapFilter();
        String ldapSearchBase = null;
        String accountName = null;
        SearchResult findAccountByAccountName = lf.findAccountByAccountName(ctx, ldapSearchBase, accountName);
        
    }

    void sendNotification() {
        //https://www.journaldev.com/2532/javamail-example-send-mail-in-java-smtp
    }

    void exportResultInFile() {

    }

    private void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
