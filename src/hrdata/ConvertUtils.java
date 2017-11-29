/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata;

import java.math.BigInteger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author onekriach
 */
//http://www.unitconversion.org/numbers/base-10-to-base-36-conversion.html
public class ConvertUtils {

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
     * characters (x corresponding to length, the 2nd parameter)
     */
    public static String toBase36(Long numToConvert, int length) {
        return StringUtils.leftPad(toBase36(numToConvert), length, ZERO);
    }

    public String getHrIdNumber(String personId, String salt) {
        String hrIdNumber = "";
        String idSup = personId.replaceAll("[^\\d]", "");
        //System.out.println("idSup="+idSup);

        String empSup = salt.replaceAll("[^\\d]", "");
        //System.out.println("empSup="+empSup);

        BigInteger idSupB = new BigInteger(idSup);
        BigInteger empSupB = new BigInteger(empSup);
        BigInteger sum;
        sum = idSupB.add(empSupB);
        //System.out.println("sum="+sum);

        long longNumber = Long.parseLong(sum.toString());
        hrIdNumber = this.toBase36(longNumber, 8);
        //System.out.println("Base36="+hrIdNumber);

        return hrIdNumber;
    }

    public static void main(String[] args) {
        String personId = "450199-15120";
        String salt = "2017-11-07";
        ConvertUtils c = new ConvertUtils();
        System.out.println("getHrIdNumber=" + c.getHrIdNumber(personId, salt));
    }

}
