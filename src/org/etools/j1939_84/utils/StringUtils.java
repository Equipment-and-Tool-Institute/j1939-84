/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.utils;

import java.util.regex.Pattern;

/**
 * @date ${date}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The class to properly interact with a string and perform various
 *         operations on strings.
 *
 */
public class StringUtils {

    private static final Pattern NON_NUMERIC_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern NON_PRINTABLE_PATTERN = Pattern.compile(".*[^\\p{Print}].*");

    /*
     * @param string String to be checked for non-numeric Ascii characteres
     */
    public static boolean containsOnlyNumericAsciiCharacters(String string) {
        return NON_NUMERIC_PATTERN.matcher(string).matches();

    }

    /*
     * @param string String to be checked for non-printable Ascii characteres
     */
    public static boolean containsNonPrintableAsciiCharacter(String string) {
        return NON_PRINTABLE_PATTERN.matcher(string).matches();
    }

}
