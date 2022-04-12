/*
 * Copyright (c) 2020. Electronic Tools Institute
 */

package org.etools.j1939tools.utils;

import java.util.Collection;

public class CollectionUtils {

    public static boolean areTwoCollectionsEqual(Collection<?> collectionA, Collection<?> collectionB) {

        // verify null checks
        if (collectionA == null && collectionB == null) {
            return true;
        }

        if (collectionA == null || collectionB == null) {
            return false;
        }

        // verify basic attributes
        if (collectionA.size() != collectionB.size()) {
            return false;
        }

        // ensure contents are the same
        for (Object itemA : collectionA) {
            if (!collectionB.contains(itemA)) {
                return false;
            }
        }

        return true;
    }

    public static byte[] join(byte[]... byteArrays) {
        byte[] bytes = new byte[0];
        for (byte[] byteArray : byteArrays) {
            bytes = addAll(bytes, byteArray);
        }
        return bytes;
    }

    public static int[] join(int[]... intArrays) {
        int[] ints = new int[0];
        for (int[] intArray : intArrays) {
            ints = addAll(ints, intArray);
        }
        return ints;
    }

    public static byte[] addAll(byte[] array1, byte... array2) {
        if (array1 == null) {
            return array2 == null ? null : array2.clone();
        } else if (array2 == null) {
            return array1.clone();
        } else {
            byte[] joinedArray = new byte[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static int[] addAll(int[] array1, int... array2) {
        if (array1 == null) {
            return array2 == null ? null : array2.clone();
        } else if (array2 == null) {
            return array1.clone();
        } else {
            int[] joinedArray = new int[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static int[] toIntArray(byte[] bytes) {
        int[] array = new int[bytes.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = bytes[i] & 0xFF;
        }
        return array;
    }

    public static byte[] toByteArray(int[] data) {
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) (data[i] & 0xFF);
        }
        return bytes;
    }

}
