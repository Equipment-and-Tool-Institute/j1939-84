/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.packets;


import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;

import org.etools.j1939tools.bus.Packet;

public class DM34NTEStatus extends GenericPacket {

    public static final int PGN = 40960; // 0xA000
    private AreaStatus noxNTEControlAreaStatus;
    private AreaStatus noxNTECarveOutAreaStatus;
    private AreaStatus noxNTEDeficiencyAreaStatus;
    private AreaStatus pmNTEControlAreaStatus;
    private AreaStatus pmNTECarveOutAreaStatus;
    private AreaStatus pmNTEDeficiencyAreaStatus;

    public DM34NTEStatus(Packet packet) {
        super(packet);
    }

    public static DM34NTEStatus create(int source,
                                       int destination,
                                       AreaStatus noxNTEControlAreaStatus,
                                       AreaStatus noxNTECarveOutAreaStatus,
                                       AreaStatus noxNTEDeficiencyAreaStatus,
                                       AreaStatus pmNTEControlAreaStatus,
                                       AreaStatus pmNTECarveOutAreaStatus,
                                       AreaStatus pmNTEDeficiencyAreaStatus) {

        byte[] data = new byte[8];
        for (int i = 2; i < data.length; i++) {
            data[i] = (byte) 0xFF;
        }

        data[0] |= (byte) (noxNTEControlAreaStatus.value << 6);
        data[0] |= (byte) (noxNTECarveOutAreaStatus.value << 4);
        data[0] |= (byte) (noxNTEDeficiencyAreaStatus.value << 2);
        data[0] |= 3;

        data[1] |= (byte) (pmNTEControlAreaStatus.value << 6);
        data[1] |= (byte) (pmNTECarveOutAreaStatus.value << 4);
        data[1] |= (byte) (pmNTEDeficiencyAreaStatus.value << 2);
        data[1] |= 3;

        return new DM34NTEStatus(Packet.create(PGN | destination, source, data));
    }

    @Override public String getName() {
        return "DM34 NTE Status";
    }

    @Override
    public String toString() {
        String result = getStringPrefix() + " {" + NL;
        result += "                          NOx NTE Control Area Status = " + getNoxNTEControlAreaStatus() + NL;
        result += "  Manufacturer-specific NOx NTE Carve-out Area Status = " + getNoxNTECarveOutAreaStatus() + NL;
        result += "                       NOx NTE Deficiency Area Status = " + getNoxNTEDeficiencyAreaStatus() + NL;
        result += "                           PM NTE Control Area Status = " + getPmNTEControlAreaStatus() + NL;
        result += "   Manufacturer-specific PM NTE Carve-out Area Status = " + getPmNTECarveOutAreaStatus() + NL;
        result += "                        PM NTE Deficiency Area Status = " + getPmNTEDeficiencyAreaStatus() + NL;
        result += "}" + NL;
        return result;
    }

    public AreaStatus getNoxNTEControlAreaStatus() {
        if (noxNTEControlAreaStatus == null) {
            parsePacket();
        }
        return noxNTEControlAreaStatus;
    }

    public AreaStatus getNoxNTECarveOutAreaStatus() {
        if (noxNTECarveOutAreaStatus == null) {
            parsePacket();
        }
        return noxNTECarveOutAreaStatus;
    }

    public AreaStatus getNoxNTEDeficiencyAreaStatus() {
        if (noxNTEDeficiencyAreaStatus == null) {
            parsePacket();
        }
        return noxNTEDeficiencyAreaStatus;
    }

    public AreaStatus getPmNTEControlAreaStatus() {
        if (pmNTEControlAreaStatus == null) {
            parsePacket();
        }
        return pmNTEControlAreaStatus;
    }

    public AreaStatus getPmNTECarveOutAreaStatus() {
        if (pmNTECarveOutAreaStatus == null) {
            parsePacket();
        }
        return pmNTECarveOutAreaStatus;
    }

    public AreaStatus getPmNTEDeficiencyAreaStatus() {
        if (pmNTEDeficiencyAreaStatus == null) {
            parsePacket();
        }
        return pmNTEDeficiencyAreaStatus;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private void parsePacket() {
        noxNTEControlAreaStatus = AreaStatus.findValue(getShaveAndAHaircut(0, 0xC0, 6));
        noxNTECarveOutAreaStatus = AreaStatus.findValue(getShaveAndAHaircut(0, 0x30, 4));
        noxNTEDeficiencyAreaStatus = AreaStatus.findValue(getShaveAndAHaircut(0, 0x0C, 2));

        pmNTEControlAreaStatus = AreaStatus.findValue(getShaveAndAHaircut(1, 0xC0, 6));
        pmNTECarveOutAreaStatus = AreaStatus.findValue(getShaveAndAHaircut(1, 0x30, 4));
        pmNTEDeficiencyAreaStatus = AreaStatus.findValue(getShaveAndAHaircut(1, 0x0C, 2));
    }

    public enum AreaStatus {
        OUTSIDE("Outside Area", 0),
        INSIDE("Inside Area", 1),
        RESERVED("Reserved", 2),
        NOT_AVAILABLE("Not available", 3),
        UNKNOWN("Unknown", -1);

        private final String string;
        private final int value;

        AreaStatus(String string, int value) {
            this.string = string;
            this.value = value;
        }

        private static AreaStatus findValue(int value) {
            return Arrays.stream(AreaStatus.values()).filter(e -> e.value == value).findFirst().orElse(UNKNOWN);
        }

        @Override
        public String toString() {
            return string + " (" + value + ")";
        }
    }
}
