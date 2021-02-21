/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.Collection;
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
 * 6.4.14 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part04Step14Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 14;
    private static final int TOTAL_STEPS = 0;

    Part04Step14Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step14Controller(Executor executor,
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
        // 6.4.14.1.a. DS DM7 with TID 250 and each specific SPN+FMI that had non-initialized test results on list
        // created in test 6.4.14.1.
        for (OBDModuleInformation obdModuleInformation : getDataRepository().getObdModules()) {
            int moduleAddress = obdModuleInformation.getSourceAddress();
            String moduleName = obdModuleInformation.getModuleName();

            for (ScaledTestResult str : obdModuleInformation.getNonInitializedTests()) {
                int spn = str.getSpn();
                int fmi = str.getFmi();

                // 6.4.14.2.a. Fail if any [Scaled Test Result is] now reporting initialized values.
                getDiagnosticMessageModule().requestTestResults(getListener(), moduleAddress, 250, spn, fmi)
                                            .stream()
                                            .map(DM30ScaledTestResultsPacket::getTestResults)
                                            .flatMap(Collection::stream)
                                            .filter(ScaledTestResult::isInitialized)
                                            .forEach(s -> addFailure("6.4.14.2.a - " + moduleName
                                                    + " is now reporting an initialize test for SPN = " + spn
                                                    + ", FMI = " + fmi));
            }
        }

    }

}
