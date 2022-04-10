/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.model;

import java.util.Objects;

import org.etools.j1939tools.j1939.packets.ScaledTestResult;

public class SpnFmi {
    public final int spn;
    public final int fmi;

    public static SpnFmi of(int spn, int fmi) {
        return new SpnFmi(spn, fmi);
    }

    public static SpnFmi of(ScaledTestResult str) {
        return new SpnFmi(str.getSpn(), str.getFmi());
    }

    private SpnFmi(int spn, int fmi) {
        this.spn = spn;
        this.fmi = fmi;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpnFmi)) {
            return false;
        }

        SpnFmi spnFmi = (SpnFmi) o;
        return spn == spnFmi.spn && fmi == spnFmi.fmi;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spn, fmi);
    }
}
