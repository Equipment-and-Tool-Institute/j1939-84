/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939tools.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The class to properly interact with a string and perform various
 * operations on strings.
 */
public class StringUtils {

    public static String padLeft(String input, int length) {
        return String.format("%" + length + "s", input);
    }

    public static String padRight(String input, int length) {
        return String.format("%-" + length + "s", input);
    }

    public static String center(String string, int length) {
        StringBuilder sb = new StringBuilder(length);
        int newLength = (length - string.length()) / 2;
        if (newLength > 0) {
            sb.setLength(newLength);
            sb.append(string);
            sb.setLength(length);
        } else {
            sb.append(string);
        }

        return sb.toString().replace('\0', ' ');
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
