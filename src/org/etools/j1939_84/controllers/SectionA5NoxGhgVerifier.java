/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939tools.j1939.packets.ParsedPacket.NOT_AVAILABLE;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_GREEN_HOUSE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_HYBRID_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_GREEN_HOUSE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_HYBRID_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_HYBRID_CHG_DEPLETING_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_PG;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_ACTIVITY_PGs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_PGs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_ACTIVE_100_HOURS_PGs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_STORED_100_HOURS_PGs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.utils.CollectionUtils;

public class SectionA5NoxGhgVerifier extends SectionVerifier {
    private final boolean afterClear;

    SectionA5NoxGhgVerifier(boolean afterClear, int partNumber, int stepNumber) {
        this(true,
             DataRepository.getInstance(),
             new CommunicationsModule(),
             new VehicleInformationModule(),
             partNumber,
             stepNumber);
    }

    protected SectionA5NoxGhgVerifier(boolean afterClear,
                                      DataRepository dataRepository,
                                      CommunicationsModule communicationsModule,
                                      VehicleInformationModule vehInfoModule,
                                      int partNumber,
                                      int stepNumber) {

        super(dataRepository, communicationsModule, vehInfoModule, partNumber, stepNumber);
        this.afterClear = afterClear;
    }

    public void verifyDataSpn12730(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   List<GenericPacket> packets) {
        listener.onProgress(partNumber,
                            stepNumber,
                            "A.5.3 - Checking GHG spn values against previous spn values");

        // A. Lifetime Accumulators
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        // after a DM11, or DM3 from a tool.

        // B. Stored 100-hour Accumulators.
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        List<Integer> pgns = new ArrayList<>();
        pgns.add(GHG_TRACKING_LIFETIME_PG);
        pgns.add(GHG_STORED_100_HR);
        verifyPgValuesSameAsTwo(getPartNumber(), getStepNumber(), listener, pgns, packets);

        // C. Active 100-hour Arrays.
        // The SPNs in the following following messages shall be equal to zero only after global DM11 command. They
        // should be no less than their corresponding part 2 values at any other time.
        List<Integer> pgs = new ArrayList<>();
        pgs.add(GHG_ACTIVE_100_HR);
        verifyPgValuesZero(getPartNumber(), getStepNumber(), listener, pgs, packets);

    }

    public void verifyDataSpn12675(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   List<GenericPacket> packets) {
        listener.onProgress(partNumber,
                            stepNumber,
                            "A.5.3 - Checking NOx spn values against previous spn values");

        // A. Lifetime Accumulators
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        // after a DM11, or DM3 from a tool.

        // B. Stored 100-hour Accumulators.
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        List<Integer> pgns = Arrays.stream(CollectionUtils.join(NOx_LIFETIME_PGs,
                                                                NOx_LIFETIME_ACTIVITY_PGs,
                                                                NOx_TRACKING_STORED_100_HOURS_PGs))
                                   .boxed()
                                   .collect(Collectors.toList());
        verifyPgValuesSameAsTwo(getPartNumber(), getStepNumber(), listener, pgns, packets);
        // C. Active 100-hour Arrays.
        // The SPNs in the following following messages shall be equal to zero only after global DM11 command. They
        // should be no less than their corresponding part 2 values at any other time.
        List<Integer> pgs = Arrays.stream(NOx_TRACKING_ACTIVE_100_HOURS_PGs).boxed().collect(Collectors.toList());
        verifyPgValuesZero(getPartNumber(), getStepNumber(), listener, pgs, packets);

    }

    public void verifyDataSpn12691(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   List<GenericPacket> packets) {
        listener.onProgress(partNumber,
                            stepNumber,
                            "A.5.3 - Checking GHG spn values against previous spn values");

        // A. Lifetime Accumulators
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        // after a DM11, or DM3 from a tool.

        // B. Stored 100-hour Accumulators.
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        List<Integer> pgs = new ArrayList<>();
        pgs.add(GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG);
        pgs.add(GHG_STORED_GREEN_HOUSE_100_HR);
        verifyPgValuesSameAsTwo(getPartNumber(), getStepNumber(), listener, pgs, packets);

        // C. Active 100-hour Arrays.
        // The SPNs in the following following messages shall be equal to zero only after global DM11 command. They
        // should be no less than their corresponding part 2 values at any other time.
        List<Integer> pgns = new ArrayList<>();
        pgns.add(GHG_ACTIVE_GREEN_HOUSE_100_HR);
        verifyPgValuesZero(getPartNumber(), getStepNumber(), listener, pgns, packets);

    }

    public void verifyDataSpn12797(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   List<GenericPacket> packets) {
        listener.onProgress(partNumber,
                            stepNumber,
                            "A.5.3 - Checking GHG spn values against previous spn values");

        // A. Lifetime Accumulators
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        // after a DM11, or DM3 from a tool.

        // B. Stored 100-hour Accumulators.
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        List<Integer> pgns = new ArrayList<>();
        pgns.add(GHG_TRACKING_LIFETIME_HYBRID_PG);
        pgns.add(GHG_STORED_HYBRID_100_HR);
        verifyPgValuesSameAsTwo(getPartNumber(), getStepNumber(), listener, pgns, packets);

        // C. Active 100-hour Arrays.
        // The SPNs in the following following messages shall be equal to zero only after global DM11 command. They
        // should be no less than their corresponding part 2 values at any other time.
        List<Integer> pgs = new ArrayList<>();
        pgs.add(GHG_ACTIVE_HYBRID_100_HR);
        verifyPgValuesZero(getPartNumber(), getStepNumber(), listener, pgs, packets);

    }

    public void verifyDataSpn12783(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   List<GenericPacket> packets) {
        listener.onProgress(partNumber,
                            stepNumber,
                            "A.5.3 - Checking GHG spn values against previous spn values");

        // A. Lifetime Accumulators
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        // after a DM11, or DM3 from a tool.

        // B. Stored 100-hour Accumulators.
        // The SPNs in the following PGs shall be greater than or equal the corresponding values observed in Part 2,
        List<Integer> pgns = new ArrayList<>();
        pgns.add(GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        pgns.add(GHG_STORED_HYBRID_CHG_DEPLETING_100_HR);
        verifyPgValuesSameAsTwo(getPartNumber(), getStepNumber(), listener, pgns, packets);
        // C. Active 100-hour Arrays.
        // The SPNs in the following following messages shall be equal to zero only after global DM11 command. They
        // should be no less than their corresponding part 2 values at any other time.
        List<Integer> pgs = new ArrayList<>();
        pgs.add(GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR);
        verifyPgValuesZero(getPartNumber(), getStepNumber(), listener, pgs, packets);
    }

    private void verifyPgValuesSameAsTwo(int partNumber,
                                         int stepNumber,
                                         ResultsListener listener,
                                         List<Integer> pgns,
                                         List<GenericPacket> packets) {
        packets.forEach(packet -> {

            if (pgns.contains(packet.getPgnDefinition().getId())) {
                var partTwoPacket = get(packet.getPgnDefinition().getId(), packet.getSourceAddress(), 2);
                if (partTwoPacket == null) {
                    listener.addOutcome(partNumber,
                                        stepNumber,
                                        Outcome.INFO,
                                        "Section A.5.A - Massage from part 2 for PG "
                                                + packet.getPgnDefinition().getId()
                                                + " is missing so verification of values skipped");
                }

                packet.getSpns().forEach(spn -> {
                    if (spn.hasValue() && partTwoPacket != null) {
                        var partOneValue = partTwoPacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                        if (partOneValue > spn.getValue()) {
                            listener.addOutcome(partNumber,
                                                stepNumber,
                                                Outcome.FAIL,
                                                "Section A.5.A - Value received from " + packet.getModuleName() +
                                                        " in part 2 was greater than current value for "
                                                        + spn);
                        }
                    }
                });
            }
        });
    }

    private void verifyPgValuesZero(int partNumber,
                                    int stepNumber,
                                    ResultsListener listener,
                                    List<Integer> pgns,
                                    List<GenericPacket> packets) {
        if (afterClear) {
            packets.forEach(packet -> {
                if (pgns.contains(packet.getPgnDefinition().getId())) {
                    packet.getSpns().forEach(spn -> {
                        if (spn.hasValue() && spn.getValue() > 0) {
                            listener.addOutcome(partNumber,
                                                stepNumber,
                                                Outcome.FAIL,
                                                "Section A.5.C - Value received from " + packet.getModuleName()
                                                        + " is greater than 0"
                                                        + " for " + spn);
                        }
                    });
                }
            });
        } else {
            verifyPgValuesSameAsTwo(partNumber, stepNumber, listener, pgns, packets);
        }
    }

    protected <T extends GenericPacket> T get(int pg, int address, int partNumber) {
        OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(address);
        return obdModuleInformation == null ? null : obdModuleInformation.get(pg, partNumber);
    }

    public List<GenericPacket> requestAllGhgNox(int address, ResultsListener listener) {
        return getCommunicationsModule().requestAllGhgNox(address, listener);
    }
}
