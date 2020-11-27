/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import java.util.Arrays;

public class SpnDataParser {

    public byte[] parse(byte[] data, SpnDefinition definition) {
        int startByte = definition.startByte - 1;
        int startBit = definition.startBit;
        int bitLength = definition.bitLength;

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

    private byte[] longToBytes(long x) {
        byte[] result = new byte[8];
        long value = x;

        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (value & 0xFF);
            value = value >> 8;
        }
        return result;
    }

    private long bytesToLong(byte[] data) {
        long value = 0;
        for (int i = 0; i < data.length; i++) {
            value += ((long) (data[i] & 0xFF)) << i * 8;
        }
        return value;
    }
}
