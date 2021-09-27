/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.solidDesign.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.SpnFmi;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.12.8 DM7/ DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part12Step08Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part12Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step08Controller(Executor executor,
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
        // 6.12.8.1.a. DS DM7 with TID 250 and each SPN+FMI from list created in part 1.
        // 6.12.8.1.b. Record all values for any ECU address+SPN+FMI that has non-initialized values.
        for (OBDModuleInformation moduleInformation : getDataRepository().getObdModules()) {
            var nonInitializedTests = moduleInformation.getScaledTestResults()
                                                       .stream()
                                                       .map(SpnFmi::of)
                                                       .distinct()
                                                       .map(k -> requestTestResults(moduleInformation.getSourceAddress(),
                                                                                    k.spn,
                                                                                    k.fmi))
                                                       .flatMap(Collection::stream)
                                                       .map(DM30ScaledTestResultsPacket::getTestResults)
                                                       .flatMap(Collection::stream)
                                                       .filter(str -> !str.isInitialized())
                                                       .collect(Collectors.toList());
            moduleInformation.setNonInitializedTests(nonInitializedTests);
            getDataRepository().putObdModule(moduleInformation);
        }
    }

    private List<DM30ScaledTestResultsPacket> requestTestResults(int address, int spn, int fmi) {
        return getCommunicationsModule().requestTestResults(getListener(), address, 250, spn, fmi);
    }

}
