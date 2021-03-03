/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9.6 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part09Step06Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part09Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step06Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
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
        // 6.9.6.1.a DS DM7 with TID 247 and specific SPN+FMI 31 for each SPN found to have non-initialized test results
        // from list in step 6.8.11.1.
        // 6.9.6.2.a Fail if any test result is now initialized
        for (OBDModuleInformation moduleInformation : getDataRepository().getObdModules()) {
            moduleInformation.getNonInitializedTests()
                             .stream()
                             .map(str -> requestTestResults(moduleInformation, str))
                             .flatMap(Collection::stream)
                             .map(DM30ScaledTestResultsPacket::getTestResults)
                             .flatMap(Collection::stream)
                             .filter(ScaledTestResult::isInitialized)
                             .forEach(r -> {
                                 addFailure("6.9.6.2.a - " + moduleInformation.getModuleName()
                                         + " reported test result for SPN = " + r.getSpn() + ", FMI = " + r.getFmi()
                                         + " is now initialized");
                             });
        }

    }

    private List<DM30ScaledTestResultsPacket> requestTestResults(OBDModuleInformation moduleInformation,
                                                                 ScaledTestResult str) {
        return getDiagnosticMessageModule().requestTestResults(getListener(),
                                                               moduleInformation.getSourceAddress(),
                                                               247,
                                                               str.getSpn(),
                                                               str.getFmi());
    }

}
