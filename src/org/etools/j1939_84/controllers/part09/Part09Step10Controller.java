/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Either;
import org.etools.j1939tools.j1939.model.SpnFmi;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.9.10 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part09Step10Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part09Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step10Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.9.10.1.a. DS DM7 with TID 250 and each SPN+FMI from list in part 1 to the OBD ECU that supports the SPN and
        // FMI with test results.
        for (OBDModuleInformation moduleInformation : getDataRepository().getObdModules()) {
            String moduleName = moduleInformation.getModuleName();
            int address = moduleInformation.getSourceAddress();

            var scaledTestResults = moduleInformation.getScaledTestResults()
                                                     .stream()
                                                     .map(SpnFmi::of)
                                                     .distinct()
                                                     .map(k -> requestTestResults(address, k.spn, k.fmi))
                                                     .flatMap(Collection::stream)
                                                     .map(DM30ScaledTestResultsPacket::getTestResults)
                                                     .flatMap(Collection::stream)
                                                     .peek(str -> {
                                                         // 6.9.10.2.a. Fail if any test result not initialized.
                                                         if (!str.isInitialized()) {
                                                             addFailure("6.9.10.2.a - " + moduleName
                                                                     + " reported test result for SPN = " + str.getSpn()
                                                                     + ", FMI = " + str.getFmi()
                                                                     + " is not initialized");
                                                         }
                                                     })
                                                     .collect(Collectors.toList());

            // 6.9.10.2.b. Fail if any difference in what ECU+SPN+FMI combinations have test results compared to the
            // combinations identified in part 1 as having test results.
            var prevResults = toString(moduleInformation.getScaledTestResults());
            var currentResults = toString(scaledTestResults);
            if (!currentResults.equals(prevResults)) {
                addFailure("6.9.10.2.b - " + moduleName
                        + " reported different SPN+FMI combinations for tests results compared to the combinations in part 1");
            }
        }

    }

    private List<DM30ScaledTestResultsPacket> requestTestResults(int address, int spn, int fmi) {
        return getCommunicationsModule()
                                        .requestTestResult(getListener(), address, 250, spn, fmi)
                                        .getPacket()
                                        .map(o -> {
                                            return o.resolve(results -> results,// just return the results as requested
                                                             ack -> dm7Tid247(address, spn, ack));
                                        })
                                        .stream()
                                        .collect(Collectors.toList());
    }

    private DM30ScaledTestResultsPacket dm7Tid247(int address, int spn, AcknowledgmentPacket ack) {
        // 6.9.10.1.b If the response for the TID 250 query is NACK (control byte = 1), then send DS DM7, for the SP
        // using TID 247 and FMI 31 to obtain all test results for the SPN. [Item b. will be performed for all SPs where
        // TID 250 is not supported by the OBD ECU.]
        if (ack.getResponse() == Response.NACK) {
            return getCommunicationsModule().requestTestResult(getListener(), address, 247, spn, 31)
                                            .getPacket()
                                            // ignore any [N]ACK
                                            .flatMap(e -> e.left)
                                            .orElse(null);
        } else {
            return null;
        }
    }

    private static String toString(List<ScaledTestResult> testResults) {
        return testResults.stream().map(r -> r.getSpn() + ":" + r.getFmi()).sorted().collect(Collectors.joining(","));
    }

}
