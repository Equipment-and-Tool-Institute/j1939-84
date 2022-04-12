/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.model;

import java.util.Arrays;

public class SpnDataParser {

    static public byte[] parse(byte[] data, SpnDefinition definition, int bitLength) {
        int startByte = definition.getStartByte() - 1;
        int startBit = definition.getStartBit();
        if (startByte < 0 || startBit < 0) {
            return new byte[0];
        }

        int byteLength;
        if (bitLength == -1) {
            byteLength = data.length;
        } else {
            byteLength = (bitLength / 8);
            if (bitLength % 8 != 0) {
                byteLength++;
            }
        }

        int endByte = startByte + byteLength;
        if (endByte > data.length) {
            return new byte[0];
        }

        byte[] subData = Arrays.copyOfRange(data, startByte, endByte);
        if (startBit != 1) {
            for (int i = 0; i < subData.length; i++) {
                subData[i] = (byte) (subData[i] >> (startBit - 1));
            }
        }

        long longData = bytesToLong(subData);
        long maskedValue;
        if (bitLength == -1) {
            maskedValue = longData;
        } else {
            maskedValue = longData & (~0L >>> (64 - bitLength));
        }
        byte[] bytes = longToBytes(maskedValue);

        return Arrays.copyOfRange(bytes, 0, byteLength);
    }

    static private byte[] longToBytes(long x) {
        byte[] result = new byte[8];
        long value = x;

        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (value & 0xFF);
            value = value >> 8;
        }
        return result;
    }

    static private long bytesToLong(byte[] data) {
        long value = 0;
        for (int i = 0; i < data.length; i++) {
            value += ((long) (data[i] & 0xFF)) << i * 8;
        }
        return value;
    }
}
