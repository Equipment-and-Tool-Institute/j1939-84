/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import java.util.Arrays;
import java.util.Objects;

import org.etools.j1939tools.j1939.Lookup;

/**
 * Contains the information about a Diagnostic Trouble Code (DTC)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DiagnosticTroubleCode {

    /**
     * The Conversion Method
     */
    private final int cm;

    /**
     * The Failure Mode Indicator
     */
    private final int fmi;

    /**
     * The Occurrence Count
     */
    private final int oc;

    /**
     * The Suspect Parameter Number
     */
    private final int spn;

    /**
     * The raw message bytes
     */
    private final int[] data;

    public DiagnosticTroubleCode(int[] data) {

        this.data = Arrays.copyOf(data, data.length);

        spn = SupportedSPN.parseSPN(data);

        // Byte: 2 bits 5-1 FMI, (most significant at bit 5)
        fmi = (data[2] & 0x1F);

        // Byte: 3 bit 8 SPN Conversion Method (shall be sent as a 0)
        cm = (data[3] & 0x80) >> 7;

        // Byte: 3 bits 7-1 Occurrence Count
        oc = (data[3] & 0x7F);
    }

    public static DiagnosticTroubleCode create(int spn, int fmi, int cm, int oc) {

        // spn bytes 1 - 3 use the spn
        int spnByte1 = spn & 0xFF;
        int spnByte2 = (spn >> 8) & 0xFF;

        int spnByte3 = 0xFF;
        spnByte3 &= (spn >> 16) << 5;

        int[] bytes = new int[4];
        bytes[0] = spnByte1; // spn bytes 1
        bytes[1] = spnByte2; // spn bytes 2
        bytes[2] = (spnByte3 | (fmi & 0x1F)); // spn bytes 3 && fmi
        bytes[3] = ((cm << 7) | (oc & 0x7F)); // cm & oc

        return new DiagnosticTroubleCode(bytes);
    }

    /**
     * Returns the raw message that was used to create the object
     *
     * @return int[]
     */
    public int[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the Conversation Method, range 0 to 1
     *
     * @return int
     */
    public int getConversionMethod() {
        return cm;
    }

    /**
     * Returns the Failure Mode Indicator, range 0 to 31
     *
     * @return int
     */
    public int getFailureModeIndicator() {
        return fmi;
    }

    /**
     * Returns the Occurrence Count, range 0 to 127
     *
     * @return int
     */
    public int getOccurrenceCount() {
        return oc;
    }

    /**
     * Returns the Suspect Parameter Number, range 0 to 524287
     *
     * @return int
     */
    public int getSuspectParameterNumber() {
        return spn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSuspectParameterNumber(),
                            getFailureModeIndicator());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof DiagnosticTroubleCode)) {
            return false;
        }

        DiagnosticTroubleCode that = (DiagnosticTroubleCode) obj;
        return getSuspectParameterNumber() == that.getSuspectParameterNumber() &&
                getFailureModeIndicator() == that.getFailureModeIndicator();
    }

    @Override
    public String toString() {
        String result = "DTC " + getSuspectParameterNumber() + ":" + getFailureModeIndicator() + " - ";
        result += Lookup.getSpnName(getSuspectParameterNumber()) + ", ";
        result += Lookup.getFmiDescription(getFailureModeIndicator());
        if (getOccurrenceCount() != 0x3F && getOccurrenceCount() != 127) {
            result += " - " + getOccurrenceCount() + " times";
        }
        return result;
    }

}
