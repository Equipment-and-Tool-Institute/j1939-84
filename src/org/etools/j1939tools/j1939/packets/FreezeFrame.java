/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class FreezeFrame {

    private final DiagnosticTroubleCode dtc;
    private final int[] spnData;
    private final List<Spn> spns = new ArrayList<>();

    public FreezeFrame(DiagnosticTroubleCode dtc, Spn... spns) {
        this(dtc, Arrays.asList(spns));
    }

    public FreezeFrame(DiagnosticTroubleCode dtc, List<Spn> spns) {
        this.dtc = dtc;
        spnData = spns.stream().flatMapToInt(s -> Arrays.stream(s.getData())).toArray();
    }

    public FreezeFrame(DiagnosticTroubleCode dtc, int[] spnData) {
        this.dtc = dtc;
        this.spnData = Arrays.copyOfRange(spnData, 0, spnData.length);
    }

    public DiagnosticTroubleCode getDtc() {
        return dtc;
    }

    public int[] getSpnData() {
        return Arrays.copyOf(spnData, spnData.length);
    }

    public int[] getData() {
        int[] joinedData = CollectionUtils.join(dtc.getData(), spnData);
        return CollectionUtils.join(new int[] { joinedData.length }, joinedData);
    }

    public List<Spn> getSPNs() {
        return spns;
    }

    public void setSPNs(List<Spn> spns) {
        this.spns.clear();
        this.spns.addAll(spns);
    }

    public Spn getSpn(int spnId) {
        return getSPNs().stream().filter(s -> s.getId() == spnId).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("  Freeze Frame: {").append(NL);
        result.append("    Data Length: ").append(spnData.length).append(NL);
        result.append("    ").append(getDtc()).append(NL);
        int[] spnData = getSpnData();
        result.append("    SPN Data: ")
              .append(Arrays.stream(spnData)
                            .mapToObj(x -> String.format("%02X", x))
                            .collect(Collectors.joining(" ")))
              .append(NL);
        getSPNs().stream().forEach(spn -> {
            result.append("    ").append(spn.toString()).append(NL);
        });

        result.append("  }");

        return result.toString();
    }

}
