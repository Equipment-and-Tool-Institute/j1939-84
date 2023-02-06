/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.CommunicationsModule;

public class SectionA5Verifier extends SectionVerifier {
    private final SectionA5MessageVerifier verifier;

    private final SectionA5NoxGhgVerifier sectionA5NoxGhgVerifier;

    public SectionA5Verifier(int partNumber, int stepNumber) {
        this(DataRepository.getInstance(),
             new SectionA5MessageVerifier(partNumber, stepNumber),
             new SectionA5NoxGhgVerifier(partNumber, stepNumber),
             new CommunicationsModule(),
             new VehicleInformationModule(),
             partNumber,
             stepNumber);
    }

    SectionA5Verifier(DataRepository dataRepository,
                      SectionA5MessageVerifier verifier,
                      SectionA5NoxGhgVerifier sectionA5NoxGhgVerifier,
                      CommunicationsModule communicationsModule,
                      VehicleInformationModule vehicleInformationModule,
                      int partNumber,
                      int stepNumber) {

        super(dataRepository,
              communicationsModule,
              vehicleInformationModule,
              partNumber,
              stepNumber);

        this.verifier = verifier;
        this.sectionA5NoxGhgVerifier = sectionA5NoxGhgVerifier;
    }

    public void setJ1939(J1939 j1939) {
        verifier.setJ1939(j1939);
        sectionA5NoxGhgVerifier.setJ1939(j1939);
    }

    public void verifyDataErased(ResultsListener listener, String section) {
        listener.onResult(NL + section + " - Checking for erased diagnostic information");
        getDataRepository().getObdModuleAddresses().forEach(a -> checkModuleData(listener, section, a, true));
    }

    public void verifyDataNotErased(ResultsListener listener, String section) {
        listener.onResult(NL + section + " - Checking for erased diagnostic information");
        getDataRepository().getObdModuleAddresses().forEach(a -> checkModuleData(listener, section, a, false));
    }

    public void verifyDataNotPartialErased(ResultsListener listener,
                                           String section1,
                                           String section2,
                                           boolean verifyIsErased) {
        listener.onResult(NL + section1 + " - Checking for erased diagnostic information");

        Set<Boolean> results = new HashSet<>();

        // section1 - Fail if any ECU partially erases diagnostic information (pass if it erases either all or none).
        for (int address : getDataRepository().getObdModuleAddresses()) {
            Result dataAsSameResult = checkModuleDataAsSame(listener, section1, address, verifyIsErased);
            if (dataAsSameResult.isMixed) {
                addFailure(listener,
                           section1 + " - " + Lookup.getAddressName(address)
                                   + " partially erased diagnostic information");
            }
            results.add(dataAsSameResult.isErased);

            verifyGhgNOxBinngData(listener, address, verifyIsErased);
        }

        // section2 - Fail if one or more than one ECU erases diagnostic information and one or more other ECUs do not
        // erase diagnostic information. See Section A.5.
        if (results.size() != 1) {
            addFailure(listener,
                       section2 + " - One or more than one ECU erased diagnostic information and one or more other ECUs did not erase diagnostic information");
        }
    }

    /** public for testing **/
    public void verifyGhgNOxBinngData(ResultsListener listener, int address, boolean isErased) {
        List<GenericPacket> packets = sectionA5NoxGhgVerifier.requestAllGhgNox(address, listener);

        if (getDataRepository().getObdModule(address).supportsSpn(12675)) {
            sectionA5NoxGhgVerifier.verifyDataSpn12675(listener,
                                                       getPartNumber(),
                                                       getStepNumber(),
                                                       packets,
                                                       isErased);
        }

        if (getDataRepository().getObdModule(address).supportsSpn(12730)) {
            sectionA5NoxGhgVerifier.verifyDataSpn12730(listener,
                                                       getPartNumber(),
                                                       getStepNumber(),
                                                       packets,
                                                       isErased);
        }

        if (getDataRepository().getObdModule(address).supportsSpn(12691)) {
            sectionA5NoxGhgVerifier.verifyDataSpn12691(listener,
                                                       getPartNumber(),
                                                       getStepNumber(),
                                                       packets,
                                                       isErased);
        }

        if (getDataRepository().getObdModule(address).supportsSpn(12797)) {
            sectionA5NoxGhgVerifier.verifyDataSpn12797(listener,
                                                       getPartNumber(),
                                                       getStepNumber(),
                                                       packets,
                                                       isErased);
        }

        if (getDataRepository().getObdModule(address).supportsSpn(12783)) {
            sectionA5NoxGhgVerifier.verifyDataSpn12783(listener,
                                                       getPartNumber(),
                                                       getStepNumber(),
                                                       packets,
                                                       isErased);
        }
    }

    private void checkModuleData(ResultsListener listener,
                                 String section,
                                 int address,
                                 boolean asErased) {

        verifier.checkDM6(listener, section, address, asErased);
        verifier.checkDM12(listener, section, address, asErased);
        verifier.checkDM23(listener, section, address, asErased);
        verifier.checkDM29(listener, section, address, asErased);
        verifier.checkDM5(listener, section, address, asErased);
        verifier.checkDM25(listener, section, address, asErased);
        verifier.checkDM31(listener, section, address, asErased);
        verifier.checkDM21(listener, section, address, asErased);
        verifier.checkDM26(listener, section, address, asErased);
        verifier.checkTestResults(listener, section, address, asErased);
        if (!asErased) {
            verifier.checkDM20(listener, section, address);
            verifier.checkDM28(listener, section, address);
            verifier.checkDM33(listener, section, address);
            verifier.checkEngineRunTime(listener, section, address);
            verifier.checkEngineIdleTime(listener, section, address);
        }
    }

    private Result checkModuleDataAsSame(ResultsListener listener,
                                         String section,
                                         int address,
                                         boolean verifyIsErased) {
        Set<Boolean> results = new HashSet<>();

        results.add(verifier.checkDM6(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM12(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM23(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM29(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM5(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM25(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM31(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM21(listener, section, address, verifyIsErased));
        results.add(verifier.checkDM26(listener, section, address, verifyIsErased));
        results.add(verifier.checkTestResults(listener, section, address, verifyIsErased));
        if (!verifyIsErased) {
            results.add(verifier.checkDM20(listener, section, address));
            results.add(verifier.checkDM28(listener, section, address));
            results.add(verifier.checkDM33(listener, section, address));
            results.add(verifier.checkEngineRunTime(listener, section, address));
            results.add(verifier.checkEngineIdleTime(listener, section, address));
        }

        boolean isErased = results.iterator().next();
        boolean isMixed = results.size() != 1;
        return new Result(isErased, isMixed);
    }

}
