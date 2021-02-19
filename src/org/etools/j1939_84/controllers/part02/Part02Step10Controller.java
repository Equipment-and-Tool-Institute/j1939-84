/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/*
 * The controller for 6.2.10 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results
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
             new DiagnosticMessageModule(),
             DateTimeModule.getInstance());
    }

    Part02Step10Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           DataRepository dataRepository,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DateTimeModule dateTimeModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {

        //getDiagnosticMessageModule().setJ1939(getJ1939());

        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {

            int sourceAddress = obdModule.getSourceAddress();
            String moduleName = Lookup.getAddressName(sourceAddress);

            //6.2.10.1.a. DS DM7 to each OBD ECU with TID 247+ for each DM24 SPN +FMI 31 provided by OBD ECU’s DM24 response.
            List<ScaledTestResult> newTestResults = obdModule.getTestResultSPNs()
                    .stream()
                    .flatMap(spn -> getDiagnosticMessageModule().requestTestResults(getListener(),
                                                                                    sourceAddress,
                                                                                    247,
                                                                                    spn.getSpn(),
                                                                                    31).stream())
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
                addFailure("6.2.10.2.a - " + moduleName + " provided different test result labels from the test results received in part 1 test 12");
            }

            if (!newTestResults.isEmpty()) {
                //6.2.10.3.a. Warn if all test results show initialized (either 0xFB00/0xFFFF/0xFFFF
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
        }

    }

}
