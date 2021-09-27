/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

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
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.8.14 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part08Step14Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 14;
    private static final int TOTAL_STEPS = 0;

    Part08Step14Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step14Controller(Executor executor,
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
        // 6.8.14.1.a. DS DM7 with TID 250 and specific SPN+FMI for each combination with non-initialized test results
        // from list created earlier in this part.
        // 6.8.14.2.a. Fail if any test results now have initialized values.
        for (OBDModuleInformation moduleInformation : getDataRepository().getObdModules()) {
            moduleInformation.getNonInitializedTests()
                             .stream()
                             .map(str -> requestTestResults(moduleInformation, str))
                             .flatMap(Collection::stream)
                             .map(DM30ScaledTestResultsPacket::getTestResults)
                             .flatMap(Collection::stream)
                             .filter(ScaledTestResult::isInitialized)
                             .forEach(r -> {
                                 addFailure("6.8.14.2.a - " + moduleInformation.getModuleName()
                                         + " reported test result for SPN = " + r.getSpn() + ", FMI = " + r.getFmi()
                                         + " is now initialized");
                             });
        }

    }

    private List<DM30ScaledTestResultsPacket> requestTestResults(OBDModuleInformation moduleInformation,
                                                                 ScaledTestResult str) {
        return getCommunicationsModule().requestTestResults(getListener(),
                                                            moduleInformation.getSourceAddress(),
                                                            250,
                                                            str.getSpn(),
                                                            str.getFmi());
    }

}
