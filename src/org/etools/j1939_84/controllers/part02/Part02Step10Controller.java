/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.10 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results
 */
public class Part02Step10Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part02Step10Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             dataRepository,
             new VehicleInformationModule(),
             new CommunicationsModule(),
             DateTimeModule.getInstance());
    }

    Part02Step10Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           DataRepository dataRepository,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DateTimeModule dateTimeModule) {
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

        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {

            int sourceAddress = obdModule.getSourceAddress();
            String moduleName = Lookup.getAddressName(sourceAddress);

            // 6.2.10.1.a. DS DM7 to each OBD ECU with TID 247+ for each DM24 SPN +FMI 31 provided by OBD ECU’s DM24
            // response.
            List<ScaledTestResult> newTestResults = obdModule.getTestResultSPNs()
                                                             .stream()
                                                             .flatMap(spn -> getCommunicationsModule().requestTestResults(getListener(),
                                                                                                                          sourceAddress,
                                                                                                                          247,
                                                                                                                          spn.getSpn(),
                                                                                                                          31)
                                                                                                      .stream())
                                                             .flatMap(p -> p.getTestResults().stream())
                                                             .collect(Collectors.toList());

            // 6.2.10.2.a. Fail if there is any difference in each ECU’s provided test result labels
            // (SPN and FMI combinations) from the test results received in part 1 test 12, paragraph 6.1.12.
            // Changes in measurements are expected, changes in the number and content of SPN and FMI combinations
            // are not expected and shall fail if additional SPN and FMI combinations are found,
            // or if any SPN and FMI combinations go missing.

            String oldResults = obdModule.getScaledTestResults()
                                         .stream()
                                         .map(r -> r.getSpn() + ":" + r.getFmi())
                                         .sorted()
                                         .collect(Collectors.joining(","));
            String newResults = newTestResults.stream()
                                              .map(r -> r.getSpn() + ":" + r.getFmi())
                                              .sorted()
                                              .collect(Collectors.joining(","));

            if (!oldResults.equals(newResults)) {
                addFailure("6.2.10.2.a - " + moduleName
                        + " provided different test result labels from the test results received in part 1 test 12");
            }

            if (!newTestResults.isEmpty()) {
                // 6.2.10.3.a. Warn if all test results show initialized (either 0xFB00/0xFFFF/0xFFFF
                // or 0x0000/0x0000/0x0000) results across all SPNs requested.
                boolean allInitialized = true;
                for (ScaledTestResult result : newTestResults) {
                    int max = result.getTestMaximum();
                    int min = result.getTestMinimum();
                    int value = result.getTestValue();
                    boolean isNotSupported = value == 0xFB00 && min == 0xFFFF && max == 0xFFFF;
                    boolean isInitialized = value == 0x0000 && min == 0x0000 && max == 0x0000;
                    if (!isNotSupported && !isInitialized) {
                        allInitialized = false;
                        break;
                    }
                }
                if (allInitialized) {
                    addWarning("6.2.10.3.a - All test results from " + moduleName + " are still initialized");
                }
            }

            obdModule.getSupportedSPNs()
                    .stream()
                    .filter(SupportedSPN::supportsRationalityFaultData)
                    .forEach(spn -> {
                        getCommunicationsModule().requestDM58(getListener(),
                                                              obdModule.getSourceAddress(),
                                                              spn.getSpn())
                                .requestResult()
                                .getEither()
                                .stream()
                                .findFirst()
                                .ifPresentOrElse(response -> {
                                                     // // 6.2.10.5.a - Fail if NACK received for DM7 PG
                                                     // from OBD ECU
                                                     if (response.right.isPresent()) {
                                                         addFailure("6.2.10.5.a - NACK received for DM7 PG from OBD ECU from "
                                                                            + obdModule.getModuleName() + " for SP " + spn);
                                                     } else {
                                                         DM58RationalityFaultSpData packet = response.left.orElse(null);
                                                         // 6.2.10.5.c. Fail, if expected unused bytes in DM58 are
                                                         // not padded with FFh
                                                         if (packet != null && !areUnusedBytesPaddedWithFFh(packet)) {
                                                             addFailure(
                                                                     "6.2.10.5.c - Unused bytes in DM58 are not padded with FFh in the response from "
                                                                             + obdModule.getModuleName()
                                                                             + " for SP " + spn);
                                                         }
                                                         // 6.2.10.5.d. Fail, if data returned is greater than FBh
                                                         // (for 1 byte SP), FBFFh (for 2 byte SP), or FBFFFFFFh (for 4 byte SP)
                                                         if (packet != null && isGreaterThanFb(packet)) {
                                                             addFailure(
                                                                     "6.2.10.5.d - Data returned is greater than 0xFB... threshold from "
                                                                             + obdModule.getModuleName()
                                                                             + " for " + spn);
                                                         }
                                                     }
                                                 },
                                                 () -> {
                                                     // 6.2.10.5.b. Fail, if DM58 not received
                                                     addFailure("6.2.10.5.b. DM58 not received from "
                                                                        + obdModule.getModuleName()
                                                                        + " for SP " + spn);
                                                 });
                    });
            getDm58AndVerifyData(obdModule.getSourceAddress());
        }
    }
    private void getDm58AndVerifyData(int moduleAddress) {

        // 6.2.10.6 Actions2:
        // a. DS DM7 with TID 245 (for DM58) using FMI 31 for each SP identified as supporting DM58 in a DM24 response
        // In step 6.1.4.1 to the SP’s respective OBD ECU.
        // b. Display the scaled engineering value for the requested SP.
        var nonRatFaultSps = getDataRepository().getObdModules()
                .stream()
                .filter(module -> module.getSourceAddress() == moduleAddress)
                .flatMap(m -> m.getSupportedSPNs().stream())
                .filter(supported -> !supported.supportsRationalityFaultData())
                .collect(Collectors.toList());

        if (nonRatFaultSps.isEmpty()) {
            getListener().onResult("6.2.10.6.a - No SPs found that do NOT indicate support for DM58 in the DM24 response from "
                                           + Lookup.getAddressName(moduleAddress));
        } else {
            int requestSpn = nonRatFaultSps.stream()
                    .filter(SupportedSPN::supportsScaledTestResults)
                    .findFirst()
                    .orElseGet(() -> nonRatFaultSps.get(0))
                    .getSpn();

            var packet = getCommunicationsModule().requestDM58(getListener(), moduleAddress, requestSpn)
                    .requestResult()
                    .getAcks()
                    .stream()
                    .findFirst()
                    .orElse(null);

            // 6.2.10.7 Fail/Warn criteria3:
            // a. Fail if a NACK is not received
            if (packet == null || packet.getResponse() != AcknowledgmentPacket.Response.NACK) {
                addFailure("6.2.10.7.a - NACK not received for DM7 PG from OBD ECU "
                                   + Lookup.getAddressName(moduleAddress) + " for SPN " + requestSpn);
            }
        }
    }

}
