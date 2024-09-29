/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.15 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part07Step15Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part07Step15Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step15Controller(Executor executor,
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
            var testResults = new ArrayList<ScaledTestResult>();

            // 6.7.15.1.a DS DM7 with TID 246 + SPN 5846 + FMI 31.
            var allTestResults = queryForAllTestsResults(obdModuleInformation);
            if (!allTestResults.isEmpty()) {
                testResults.addAll(allTestResults);
            } else {
                // 6.7.15.1.a.i. If TID 246 method not supported, use DS DM7 with TID 247 + each DM24 SPN+ FMI 31.
                testResults.addAll(queryForSupportedTestResults(obdModuleInformation));
            }

            // 6.7.15.1.b. Create list of any ECU address+SPN+FMI combination with non-initialized test results.
            Map<ScaledTestResult, Integer> nonInit = new HashMap<ScaledTestResult, Integer>();
            testResults.stream().filter(tr -> !tr.isInitialized()).forEach(tr -> {
                nonInit.put(tr, (int)testResults.stream().filter(tr2 -> tr.equals(tr2) && tr2.isInitialized()).count());
            });
            obdModuleInformation.setNonInitializedTests(nonInit);
            obdModuleInformation.setInitializedTests(testResults.stream().filter(t -> t.isInitialized()).toList());

            // 6.7.15.2.a. Fail if any difference in the ECU address+SPN+FMI combinations that report test results
            // compared to list created in part 1.
            if (!testResultsSame(testResults, obdModuleInformation.getScaledTestResults())) {
                addFailure("6.7.15.2.a - Difference in tests results reported from "
                        + obdModuleInformation.getModuleName() + " compared to list created in part 1");
            }
        }
    }

    private List<ScaledTestResult> queryForAllTestsResults(OBDModuleInformation obdModuleInformation) {
        return parseTestResults(getAllTestResults(obdModuleInformation.getSourceAddress()));
    }

    private BusResult<DM30ScaledTestResultsPacket> getAllTestResults(int address) {
        return getCommunicationsModule().requestTestResult(getListener(), address, 246, 5846, 31);
    }

    private static boolean testResultsSame(Collection<ScaledTestResult> testResults1,
                                           Collection<ScaledTestResult> testResults2) {
        var results1 = testResults1.stream()
                                   .map(r -> r.getSpn() + ":" + r.getFmi())
                                   .sorted()
                                   .collect(Collectors.joining(","));
        var results2 = testResults2.stream()
                                   .map(r -> r.getSpn() + ":" + r.getFmi())
                                   .sorted()
                                   .collect(Collectors.joining(","));
        return results1.equals(results2);
    }

    private static List<ScaledTestResult> parseTestResults(BusResult<DM30ScaledTestResultsPacket> result) {
        return result.getPacket()
                     .flatMap(r -> r.left)
                     .map(DM30ScaledTestResultsPacket::getTestResults)
                     .stream()
                     .flatMap(Collection::stream)
                     .collect(Collectors.toList());
    }

    private List<ScaledTestResult> queryForSupportedTestResults(OBDModuleInformation obdModuleInformation) {
        var testResults = new ArrayList<ScaledTestResult>();

        // 6.7.15.1.a.i. If TID 246 method not supported, use DS DM7 with TID 247 + each DM24 SPN+ FMI 31.
        for (SupportedSPN supportedSPN : obdModuleInformation.getTestResultSPNs()) {
            int spn = supportedSPN.getSpn();

            var singleTestResponse = getTestResults(obdModuleInformation.getSourceAddress(), spn);

            var singleTestResult = parseTestResults(singleTestResponse);
            if (!singleTestResult.isEmpty()) {
                testResults.addAll(singleTestResult);
            } else {
                // 6.7.15.2.b. Fail if NACK received from OBD ECUs that did not support an SPN listed in its
                // DM24 response
                if (isNACKed(singleTestResponse)) {
                    addFailure("6.7.15.2.b - NACK received from " + obdModuleInformation.getModuleName()
                            + " which did not support an SPN (" + spn + ") listed in its DM24 response");
                }
            }
        }
        return testResults;
    }

    private BusResult<DM30ScaledTestResultsPacket> getTestResults(int address, int spn) {
        return getCommunicationsModule().requestTestResult(getListener(), address, 247, spn, 31);
    }

    private static boolean isNACKed(BusResult<DM30ScaledTestResultsPacket> singleTestResponse) {
        return singleTestResponse.getPacket()
                                 .flatMap(r -> r.right)
                                 .map(AcknowledgmentPacket::getResponse)
                                 .filter(r -> r == NACK)
                                 .stream()
                                 .findFirst()
                                 .isPresent();
    }

}
