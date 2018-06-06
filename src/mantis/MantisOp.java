/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mantis.MantisHttpClient.USERNAME;
import sun.util.logging.resources.logging;

/**
 *
 * @author onekriach
 */
public class MantisOp {

    final static String USERNAME = "username";
    final static String EMAIL = "email";
    final static String REALNAME = "realname";
    final static String ACCESSLEVEL = "access_level";
    final static String ENABLED = "enabled";
    final static String PROTECTED = "protected";
    final static String OP_USERID = "user_id";
    final static String OP_UPDATETOKEN = "manage_user_update_token";
    final static String OP_CREATETOKEN = "manage_user_create_token";
    private static HashMap<String, String> mp = new HashMap<String, String>();
    private static String url = "";
    private static String adm = "";
    private static String pw = "";
    Logger log = Logger.getLogger(MantisUtil.class.getName());

    void execute() throws IOException {
        log.info("InputUser data:: " + mp);
        MantisHttpClient client = new mantis.MantisHttpClient();
        client.init();
        client.connect(url, adm, pw);
        client.updateUserProfile(mp);
    }

    public static void main(String[] args) throws IOException {

        try {
            url = args[0];
            adm = args[1];
            pw = args[2];
            mp.put(USERNAME, args[3]);
            mp.put(REALNAME, args[4]);
            mp.put(EMAIL, args[5]);
            if (args.length >= 7) {
                mp.put(ENABLED, args[6]);
            }
            if (args.length >= 8) {
                mp.put(ACCESSLEVEL, args[7]);
            }
            if (args.length >= 9) {
                mp.put(PROTECTED, args[8]);
            }
            MantisOp op = new MantisOp();
            op.execute();

        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("\nNumber of arguments is wrong. Must be 7 to 9 arguments." + System.lineSeparator()
                    + "0: https URL" + System.lineSeparator()
                    + "1: Admin username" + System.lineSeparator()
                    + "2: secret" + System.lineSeparator()
                    + "3: USERNAME" + System.lineSeparator()
                    + "4: REALNAME" + System.lineSeparator()
                    + "5: EMAIL" + System.lineSeparator()
                    + "6: ENABLED (optional)" + System.lineSeparator()
                    + "7: ACCESSLEVEL (optional)" + System.lineSeparator()
                    + "8: PROTECTED (optional)" + System.lineSeparator()
            );
        }
    }

}
