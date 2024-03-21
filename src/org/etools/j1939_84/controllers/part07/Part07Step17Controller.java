/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.17 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part07Step17Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 17;
    private static final int TOTAL_STEPS = 0;

    Part07Step17Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step17Controller(Executor executor,
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
        // 6.7.17.1.a. DS DM7 with TID 250 and each specific SPN+FMI that had non-initialized test results on list
        // created in step 6.7.15.1.b.
        for (OBDModuleInformation obdModuleInformation : getDataRepository().getObdModules()) {
            int moduleAddress = obdModuleInformation.getSourceAddress();
            String moduleName = obdModuleInformation.getModuleName();

            for (Map.Entry<ScaledTestResult, Integer> e : obdModuleInformation.getNonInitializedTests().entrySet()) {
                ScaledTestResult str = e.getKey();
                int spn = str.getSpn();
                int fmi = str.getFmi();
                long initializedCount = e.getValue();

                // 6.7.17.2.a. Fail if any non-initialized tests reports now report initialized values.
                // Use this to help verify no diagnostic information was cleared with DM3 request.
                getCommunicationsModule().requestTestResults(getListener(), moduleAddress, 250, spn, fmi).forEach(p -> {
                    p.getTestResults().stream().filter(ScaledTestResult::isInitialized).forEach(s -> {
                        if (initializedCount != p.getTestResults().stream().filter(pstr -> pstr.equals(s) && pstr.isInitialized()).count()) {
                            addFailure("6.7.17.2.a - " + moduleName
                                               + " is now reporting an initialize test for SPN = " + spn
                                               + ", FMI = " + fmi);
                        }
                    });
                });
            }
        }
    }

}
