/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.ArrayList;
import java.util.Collection;
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
import org.etools.j1939tools.j1939.model.SpnFmi;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.4.12 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part04Step12Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part04Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step12Controller(Executor executor,
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

        for (OBDModuleInformation obdModuleInformation : getDataRepository().getObdModules()) {
            int moduleAddress = obdModuleInformation.getSourceAddress();

            if (!obdModuleInformation.getScaledTestResults().isEmpty()) {

                // 6.4.12.1.a. DS DM7 to each OBD ECU that provided test results in part 1 using TID 246, SPN 5846, and
                // FMI 31.
                var resultPackets = getCommunicationsModule().requestTestResults(getListener(),
                                                                                 moduleAddress,
                                                                                 246,
                                                                                 5846,
                                                                                 31);

                List<DM30ScaledTestResultsPacket> packets = new ArrayList<>(resultPackets);
                if (packets.isEmpty()) {
                    // 6.4.12.1.a.i. If TID 246 method not supported, use DS DM7 with TID 247 + each DM24 SPN+ FMI 31
                    var testResultSPNs = obdModuleInformation.getTestResultSPNs();
                    testResultSPNs.stream()
                                  .map(spn -> getCommunicationsModule().requestTestResults(getListener(),
                                                                                           moduleAddress,
                                                                                           247,
                                                                                           spn.getSpn(),
                                                                                           31))
                                  .flatMap(Collection::stream)
                                  .forEach(packets::add);
                }

                // 6.4.12.1.b. Create list of any ECU address+SPN+FMI combination with non-initialized test results,
                // noting the number of initialized test results for each SPN+FMI combination that has non-initialized
                // test results.
                var nonInitializedTests = packets.stream()
                                                 .map(DM30ScaledTestResultsPacket::getTestResults)
                                                 .flatMap(Collection::stream)
                                                 .filter(r -> !r.isInitialized())
                                                 .collect(Collectors.toList());

                if (!nonInitializedTests.isEmpty()) {
                    obdModuleInformation.setNonInitializedTests(nonInitializedTests);
                    getDataRepository().putObdModule(obdModuleInformation);
                }

                // 6.4.12.2.a. Fail if there is any difference in each ECU’s provided
                // test result labels (SPN and FMI combinations)
                // from the test results received in part 1 test 11, paragraph 6.1.12
                var currentTestResults = packets.stream()
                                                .map(DM30ScaledTestResultsPacket::getTestResults)
                                                .flatMap(Collection::stream)
                                                .map(SpnFmi::of)
                                                .collect(Collectors.toSet());

                var previousTestResults = obdModuleInformation.getScaledTestResults()
                                                              .stream()
                                                              .map(SpnFmi::of)
                                                              .collect(Collectors.toSet());

                if (!previousTestResults.equals(currentTestResults)) {
                    addFailure("6.4.12.2.a - " + Lookup.getAddressName(moduleAddress)
                            + " reported a difference in test result labels from the test results received in part 1");
                }

            }
        }

    }

}
