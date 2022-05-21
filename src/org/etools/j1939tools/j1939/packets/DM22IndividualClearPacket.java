/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;

import org.etools.j1939tools.bus.Packet;

public class DM22IndividualClearPacket extends GenericPacket {

    public static final int PGN = 49920; // 0xC300

    public static DM22IndividualClearPacket create(int source,
                                                   int destination,
                                                   ControlByte controlByte,
                                                   ControlByteSpecificIndicator acknowledgementCode,
                                                   int spn,
                                                   int fmi) {
        int[] data = new int[8];
        data[0] = controlByte.value;
        data[1] = acknowledgementCode.value;
        data[2] = 0xFF;
        data[3] = 0xFF;
        data[4] = 0xFF;
        data[5] = spn & 0xFF;
        data[6] = (spn >> 8) & 0xFF;
        data[7] = (((spn >> 16) & 0b111) << 5);
        int maskedFMI = fmi & 0x1F;
        data[7] = data[7] | maskedFMI;
        return new DM22IndividualClearPacket(Packet.create(PGN | destination, source, data));
    }

    public static Packet createRequest(int source, int destination, ControlByte controlByte, int spn, int fmi) {
        int[] data = new int[8];
        data[0] = controlByte.value;
        data[1] = 0xFF;
        data[2] = 0xFF;
        data[3] = 0xFF;
        data[4] = 0xFF;
        data[5] = spn & 0xFF;
        data[6] = (spn >> 8) & 0xFF;
        data[7] = (((spn >> 16) & 0b111) << 5) | (fmi & 0x1F);
        return Packet.create(PGN | destination, source, data);
    }

    public DM22IndividualClearPacket(Packet packet) {
        super(packet);
    }

    public enum ControlByte {
        CLR_PA_REQ(1, "Request to Clear Previously Active DTC"),
        CLR_PA_ACK(2, "Positive Acknowledgement of Clear Previously Active DTC"),
        CLR_PA_NACK(3, "Negative Acknowledgement of Clear Previously Active DTC"),
        CLR_ACT_REQ(17, "Request to Clear Active DTC"),
        CLR_ACT_ACK(18, "Positive Acknowledgement of Clear Active DTC"),
        CLR_ACT_NACK(19, "Negative Acknowledgement of Clear Active DTC"),
        ERROR(0xFE, "Error"),
        NOT_SUPPORTED(0xFF, "Not Available"),
        UNKNOWN(-1, "Reserved for SAE Assignment");

        private final int value;
        private final String string;

        ControlByte(int value, String string) {
            this.value = value;
            this.string = string;
        }

        @Override
        public String toString() {
            return string + " (" + value + ")";
        }

        public static ControlByte find(int value) {
            return Arrays.stream(ControlByte.values()).filter(e -> e.value == value).findFirst().orElse(UNKNOWN);
        }
    }

    public enum ControlByteSpecificIndicator {
        GENERAL_NACK(0, "General Negative Acknowledge"),
        ACCESS_DENIED(1, "Access Denied"),
        UNKNOWN_DTC(2, "Diagnostic Trouble Code unknown"),
        DTC_NOT_PA(3, "Diagnostic Trouble Code no longer Previously Active"),
        DTC_NOT_ACTIVE(4, "Diagnostic Trouble Code no longer Active"),
        ERROR(0xFE, "Error"),
        NOT_SUPPORTED(0xFF, "Not Available"),
        UNKNOWN(-1, "Reserved for SAE Assignment");

        private final int value;
        private final String string;

        ControlByteSpecificIndicator(int value, String string) {
            this.value = value;
            this.string = string;
        }

        @Override
        public String toString() {
            return string + " (" + value + ")";
        }

        public static ControlByteSpecificIndicator find(int value) {
            return Arrays.stream(ControlByteSpecificIndicator.values())
                         .filter(e -> e.value == value)
                         .findFirst()
                         .orElse(UNKNOWN);
        }
    }

    public ControlByte getControlByte() {
        return ControlByte.find(getPacket().get(0));
    }

    public ControlByteSpecificIndicator getControlByteSpecificIndicator() {
        return ControlByteSpecificIndicator.find(getAcknowledgementCode());
    }

    public int getAcknowledgementCode() {
        return getPacket().get(1);
    }

    public int getSpn() {
        return (getPacket().get(5) & 0xFF) + ((getPacket().get(6) & 0xFF) << 8) + (getPacket().get(7) & 0xE0 << 16);
    }

    public int getFmi() {
        return getPacket().get(7) & 0x1F;
    }

    @Override
    public String toString() {
        String string = getStringPrefix() + NL;
        string += "Control Byte: " + getControlByte() + NL;
        string += "Acknowledgement Code: " + getControlByteSpecificIndicator() + NL;
        string += "SPN = " + getSpn() + "; FMI = " + getFmi() + NL;
        return string;
    }
}
