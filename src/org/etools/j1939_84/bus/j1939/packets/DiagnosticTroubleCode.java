/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.Objects;

import org.etools.j1939_84.bus.j1939.Lookup;

/**
 * Contains the information about a Diagnostic Trouble Code (DTC)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
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

    public DiagnosticTroubleCode(int[] data) {

        spn = SupportedSPN.parseSPN(data);

        // Byte: 2 bits 5-1 FMI, (most significant at bit 5)
        fmi = (data[2] & 0x1F);

        // Byte: 3 bit 8 SPN Conversion Method (shall be sent as a 0)
        cm = (data[3] & 0x80) >> 7;

        // Byte: 3 bits 7-1 Occurrence Count
        oc = (data[3] & 0x7F);
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
        return (getSuspectParameterNumber() == that.getSuspectParameterNumber() &&
                getOccurrenceCount() == that.getOccurrenceCount() &&
                getFailureModeIndicator() == that.getFailureModeIndicator() &&
                getConversionMethod() == that.getConversionMethod() &&
                getSuspectParameterNumber() == that.getSuspectParameterNumber());
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
                getOccurrenceCount(),
                getFailureModeIndicator(),
                getConversionMethod(),
                getSuspectParameterNumber());
    }

    @Override
    public String toString() {
        String result = "DTC: ";
        result += " (" + getSuspectParameterNumber() + ") " + Lookup.getSpnName(getSuspectParameterNumber());
        result += " " + Lookup.getFmiDescription(getFailureModeIndicator()) + " (" + getFailureModeIndicator() + ")";
        if (getOccurrenceCount() != 0x3F && getOccurrenceCount() != 127) {
            result += " " + getOccurrenceCount() + " times";
        }
        return result;
    }

}
