/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import org.etools.j1939_84.model.Outcome;

public class TableA1Row {
    private final int spn;

    private final int minimumModelYear;

    private final boolean isCompressionIgnition;

    private final boolean isSparkIgnition;

    private final boolean isElectric;

    private final boolean isHybrid;

    private final Outcome outcomeIfTwoResponses;

    private final Outcome outcomeIfProvidedByNonObd;

    private final Outcome outcomeIfProvidedAndNotSupported;

    TableA1Row(int spn,
               int minimumModelYear,
               boolean isCompressionIgnition,
               boolean isSparkIgnition,
               boolean isElectric,
               boolean isHybrid,
               Outcome outcomeIfTwoResponses,
               Outcome outcomeIfProvidedByNonObd,
               Outcome outcomeIfProvidedAndNotSupported) {
        this.spn = spn;
        this.isCompressionIgnition = isCompressionIgnition;
        this.isSparkIgnition = isSparkIgnition;
        this.isElectric = isElectric;
        this.isHybrid = isHybrid;
        this.minimumModelYear = minimumModelYear;
        this.outcomeIfTwoResponses = outcomeIfTwoResponses;
        this.outcomeIfProvidedByNonObd = outcomeIfProvidedByNonObd;
        this.outcomeIfProvidedAndNotSupported = outcomeIfProvidedAndNotSupported;
    }

    public int getSpn() {
        return spn;
    }

    public int getMinimumModelYear() {
        return minimumModelYear;
    }

    public boolean isCompressionIgnition() {
        return isCompressionIgnition;
    }

    public boolean isSparkIgnition() {
        return isSparkIgnition;
    }

    public boolean isElectric() {
        return isElectric;
    }

    public boolean isHybrid() {
        return isHybrid;
    }

    public Outcome getOutcomeIfTwoResponses() {
        return outcomeIfTwoResponses;
    }

    public Outcome getOutcomeIfProvidedByNonObd() {
        return outcomeIfProvidedByNonObd;
    }

    public Outcome getOutcomeIfProvidedAndNotSupported() {
        return outcomeIfProvidedAndNotSupported;
    }

}
