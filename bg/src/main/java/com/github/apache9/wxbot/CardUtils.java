package com.github.apache9.wxbot;

/**
 * @author Apache9
 */
public class CardUtils {

    public static String formatCardNumber(String number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append(' ');
            }
            sb.append(number.charAt(i));
        }
        return sb.toString();
    }
}
