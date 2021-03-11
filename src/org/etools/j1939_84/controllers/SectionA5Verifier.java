/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;

import java.util.HashSet;
import java.util.Set;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;

public class SectionA5Verifier {
    private final DataRepository dataRepository;
    private final int partNumber;
    private final int stepNumber;
    private final SectionA5MessageVerifier verifier;

    public SectionA5Verifier(int partNumber, int stepNumber) {
        this(DataRepository.getInstance(),
             new SectionA5MessageVerifier(partNumber, stepNumber),
             partNumber,
             stepNumber);
    }

    SectionA5Verifier(DataRepository dataRepository,
                      SectionA5MessageVerifier verifier,
                      int partNumber,
                      int stepNumber) {
        this.dataRepository = dataRepository;
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
        this.verifier = verifier;
    }

    public void setJ1939(J1939 j1939) {
        verifier.setJ1939(j1939);
    }

    public void verifyDataErased(ResultsListener listener, String section) {
        for (int address : dataRepository.getObdModuleAddresses()) {
            if (!checkModuleData(listener, section, address, true)) {
                addFailure(listener, section + " - " + Lookup.getAddressName(address) + " did not erase data");
            }
        }
    }

    public void verifyDataNotErased(ResultsListener listener, String section) {
        for (int address : dataRepository.getObdModuleAddresses()) {
            if (!checkModuleData(listener, section, address, false)) {
                addFailure(listener, section + " - " + Lookup.getAddressName(address) + " erased data");
            }
        }
    }

    public void verifyDataNotPartialErased(ResultsListener listener, String section1, String section2) {

        Set<Boolean> results = new HashSet<>();

        // section1 - Fail if any ECU partially erases diagnostic information (pass if it erases either all or none).
        for (int address : dataRepository.getObdModuleAddresses()) {
            Result moduleResult = checkModuleDataAsSame(listener, address);
            if (moduleResult.isMixed) {
                addFailure(listener,
                           section1 + " - " + Lookup.getAddressName(address)
                                   + " partially erased diagnostic information");
            }
            results.add(moduleResult.isErased);
        }

        // section2 - Fail if one or more than one ECU erases diagnostic information and one or more other ECUs do not
        // erase diagnostic information. See Section A.5.
        if (results.size() != 1) {
            addFailure(listener,
                       section2 + " - One or more than one ECU erased diagnostic information and one or more other ECUs did not erase diagnostic information");
        }
    }

    private boolean checkModuleData(ResultsListener listener,
                                    String section,
                                    int address,
                                    boolean asErased) {

        boolean result = true;
        result &= verifier.checkDM6(listener, section, address, asErased, true);
        result &= verifier.checkDM12(listener, section, address, asErased, true);
        result &= verifier.checkDM23(listener, section, address, asErased, true);
        result &= verifier.checkDM29(listener, section, address, asErased, true);
        result &= verifier.checkDM5(listener, section, address, asErased, true);
        result &= verifier.checkDM25(listener, section, address, asErased, true);
        result &= verifier.checkDM31(listener, section, address, asErased, true);
        result &= verifier.checkDM21(listener, section, address, asErased, true);
        result &= verifier.checkDM26(listener, section, address, asErased, true);
        result &= verifier.checkTestResults(listener, section, address, asErased, true);
        if (!asErased) {
            result &= verifier.checkDM20(listener, section, address, true);
            result &= verifier.checkDM28(listener, section, address, true);
            result &= verifier.checkDM33(listener, section, address, true);
            result &= verifier.checkEngineRunTime(listener, section, address, true);
            result &= verifier.checkEngineIdleTime(listener, section, address, true);
        }
        return result;
    }

    private Result checkModuleDataAsSame(ResultsListener listener, int address) {
        Set<Boolean> results = new HashSet<>();

        results.add(verifier.checkDM6(listener, null, address, false, false));
        results.add(verifier.checkDM12(listener, null, address, false, false));
        results.add(verifier.checkDM23(listener, null, address, false, false));
        results.add(verifier.checkDM29(listener, null, address, false, false));
        results.add(verifier.checkDM5(listener, null, address, false, false));
        results.add(verifier.checkDM25(listener, null, address, false, false));
        results.add(verifier.checkDM31(listener, null, address, false, false));
        results.add(verifier.checkDM21(listener, null, address, false, false));
        results.add(verifier.checkDM26(listener, null, address, false, false));
        results.add(verifier.checkTestResults(listener, null, address, false, false));
        results.add(verifier.checkDM20(listener, null, address, false));
        results.add(verifier.checkDM28(listener, null, address, false));
        results.add(verifier.checkDM33(listener, null, address, false));
        results.add(verifier.checkEngineRunTime(listener, null, address, false));
        results.add(verifier.checkEngineIdleTime(listener, null, address, false));

        boolean isErased = results.iterator().next();
        boolean isMixed = results.size() != 1;
        return new Result(isErased, isMixed);
    }

    private void addFailure(ResultsListener listener, String message) {
        listener.addOutcome(partNumber, stepNumber, FAIL, message);
    }

    private static class Result {
        public final boolean isErased;
        public final boolean isMixed;

        private Result(boolean isErased, boolean isMixed) {
            this.isErased = isErased;
            this.isMixed = isMixed;
        }
    }

}
