/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.8.11 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part08Step11Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part08Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step11Controller(Executor executor,
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
        // 6.8.11.1.a. DS DM7 with TID 247 + each DM24 SPN + FMI 31.
        // 6.8.11.1.b. Create list of any ECU address+SPN+FMI with non-initialized values.
        getDataRepository().getObdModules().forEach(obdModule -> {
            var nonInitializedTests = obdModule.getTestResultSPNs()
                                               .stream()
                                               .map(SupportedSPN::getSpn)
                                               .map(s -> requestTest(obdModule.getSourceAddress(), s))
                                               .flatMap(Collection::stream)
                                               .map(DM30ScaledTestResultsPacket::getTestResults)
                                               .flatMap(Collection::stream)
                                               .filter(r -> !r.isInitialized())
                                               .collect(Collectors.toList());
            obdModule.setNonInitializedTests(nonInitializedTests);
            getDataRepository().putObdModule(obdModule);
        });
    }

    private List<DM30ScaledTestResultsPacket> requestTest(int address, int spn) {
        return getCommunicationsModule().requestTestResults(getListener(), address, 247, spn, 31);
    }

}
