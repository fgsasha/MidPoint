/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author onekriach
 */
//http://www.unitconversion.org/numbers/base-10-to-base-36-conversion.html
//You can find useful link how to convert to base36 in different languages https://en.wikipedia.org/wiki/Base36

public class ConvertUtils {

    private static final String personId = "99999999999";
    private static final String CODE_BASE_36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Character ZERO = '0';

    /**
     * Converting a number to a base 36.
     *
     * @param numToConvert The number to convert
     * @return The number passed in parameter converted to base 36
     */
    public static String toBase36(Long numToConvert) {
        if (numToConvert < 0) {
            throw new NumberFormatException("The value of the input number'" + numToConvert + "' is less than zero!");
        }
        Long num = numToConvert;
        String text = StringUtils.EMPTY;
        int j = (int) Math.ceil(Math.log(num) / Math.log(CODE_BASE_36.length()));
        for (int i = 0; i < (j > 0 ? j : 1); i++) {
            text = CODE_BASE_36.charAt(Integer.parseInt(String.valueOf(num % CODE_BASE_36.length()))) + text;
            num /= CODE_BASE_36.length();
        }
        return text;
    }

    /**
     * Converting a number to a base 36.
     *
     * @param numToConvert The number to convert
     * @param length The length of the final result
     * @return The number passed in parameter converted to base 36 on x
     * characters (x corresponding to length, the 2nd parameter). Return string has fixed length.
     */
    public static String toBase36(Long numToConvert, int length) {
        return StringUtils.leftPad(toBase36(numToConvert), length, ZERO);
    }

    public String getHrIdNumberRev(String personId, String salt) {
        String hrIdNumber = "";
        //replace all none digital charachters
        String idSup = personId.replaceAll("[^\\d]", "");
        String empSup = salt.replaceAll("[^\\d]", "");
       // Get encoded string in long
        long longNumber = Long.parseLong(this.encodeString(idSup, empSup));
        
        // Convert long string to Base 36. Employee should have fixed length in 12 symbols
        hrIdNumber = this.toBase36(longNumber, 12);
        return hrIdNumber;
    }
    /**
     * Get encoded string by encode string with a salt
     *
     * @param personId The string to encode
     * @param salt The salt of encoded string
     * @return The encoded string
     */
    public String encodeString(String personId, String salt) {
        String output = null;
        //Reverse encoded string
        String reversePersonId = new StringBuffer(personId).reverse().toString();

        //Salt should have some length (if salt length > 6 then use the last 6 digits)
        String saltSub = null;
        if (salt.length() > 6) {
            int cut = salt.length() - 6;
            saltSub = salt.substring(cut);
        } else {
            saltSub = salt;
        }
        
        //Insert salt symbol by symbol after every 2 reversePersonId characters 
        char[] ret_arr = new char[reversePersonId.length() + saltSub.length()];
        char[] id_arr = reversePersonId.toCharArray();
        char[] s_arr = saltSub.toCharArray();
        int k = 0;
        int l = 0;
        for (int i = 0; i < reversePersonId.length(); i++) {
            if (i % 2 == 0 && l < saltSub.length()) {
                ret_arr[k] = s_arr[l];
                l = l + 1;
                k = k + 1;
            }
            ret_arr[k] = id_arr[i];
            k = k + 1;
        }
        output = new String(ret_arr);

        return output;
    }

    public static void main(String[] args) {
        //example PID 
        String pid = "2369";
        ConvertUtils c = new ConvertUtils();
        String employeeId = c.getHrIdNumberRev(personId, pid); //For HRM personId always is 99999999999
        System.out.println("Result should be equal: 002Y4MBN3X1B = " + employeeId);
        System.out.println("Result is correct: " + employeeId.equals("002Y4MBN3X1B"));
    }

}
