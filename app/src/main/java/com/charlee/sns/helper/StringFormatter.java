package com.charlee.sns.helper;

/**
 */
public class StringFormatter {

    public static final String getUserDetailsFormattedNumber(int number) {
        if (number < 10000) {
            return String.valueOf(number);
        } else {
            return (number / 1000) + "k";
        }
    }
}
