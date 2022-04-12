/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.regex.Pattern;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *         <p>
 *         The class to properly interact with a string and perform various
 *         operations on strings.
 */
public class StringUtils {

    private static final Pattern NON_NUMERIC_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern NON_PRINTABLE_PATTERN = Pattern.compile(".*[^\\p{Print}].*");

    /*
     * @param string String to be checked for non-printable ASCII characters
     */
    public static boolean containsNonPrintableAsciiCharacter(String string) {
        return NON_PRINTABLE_PATTERN.matcher(string).matches();
    }

    /*
     * @param String to be checked for non-printable ASCII characters
     */
    public static boolean containsNonPrintableAsciiCharacter(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if ((Byte.toUnsignedInt(bytes[i]) < (byte) 32) || (Byte.toUnsignedInt(bytes[i]) > 127)) {
                return true;
            }
        }
        return false;
    }

    /*
     * @param String to be checked for non-numeric ASCII characters
     */
    public static boolean containsOnlyNumericAsciiCharacters(String string) {
        return NON_NUMERIC_PATTERN.matcher(string).matches();
    }

    public static String stripLeadingAndTrailingNulls(String input) {
        if (input == null) {
            return "";
        } else {
            return stripLeadingNulls(stripTrailingNulls(input));
        }
    }

    private static String stripLeadingNulls(String input) {
        byte[] bytes = input.getBytes(UTF_8);
        int index = 0;
        boolean isNull = true;
        while (isNull && index < input.length()) {
            isNull = bytes[index++] == 0x00;
        }
        if (index == input.length()) {
            return input;
        } else {
            return input.substring(--index);
        }
    }

    private static String stripTrailingNulls(String input) {
        byte[] bytes = input.getBytes(UTF_8);
        int index = input.length();
        boolean isNull = true;
        while (isNull && index > 0) {
            isNull = bytes[--index] == 0x00;
        }
        if (index == 0) {
            return "";
        } else {
            return input.substring(0, ++index);
        }
    }

}
