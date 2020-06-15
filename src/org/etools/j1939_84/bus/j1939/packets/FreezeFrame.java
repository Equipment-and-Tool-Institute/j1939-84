/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class FreezeFrame {

    private final DiagnosticTroubleCode dtc;

    private final int[] spnData;

    public FreezeFrame(DiagnosticTroubleCode dtc, int[] spnData) {
        this.dtc = dtc;
        this.spnData = Arrays.copyOfRange(spnData, 0, spnData.length);
    }

    public DiagnosticTroubleCode getDtc() {
        return dtc;
    }

    public int[] getSpnData() {
        return spnData;
    }

    @Override
    public String toString() {
        String result = "";
        result += dtc + NL;
        result += "SPN Data: "
                + Arrays.stream(spnData).mapToObj(x -> String.format("%02X", x)).collect(Collectors.joining(" "));
        return result;
    }

}
