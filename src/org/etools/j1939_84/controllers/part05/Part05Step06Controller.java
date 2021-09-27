/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.5.6 DM20: Monitor Performance Ratio
 */
public class Part05Step06Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part05Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part05Step06Controller(Executor executor,
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
        // 6.5.6.1.a. DS DM20 {(send Request (PGN 59904) for PGN 49664 (SPN 3048)]) to OBD ECU(s) that provided DM20
        // data in part 1.
        // 6.5.6.1.b. Store each ignition cycle counter value (SPN 3048) for future use.
        getDataRepository().getObdModules()
                           .stream()
                           .filter(m -> m.get(DM20MonitorPerformanceRatioPacket.class, 1) != null)
                           .map(OBDModuleInformation::getSourceAddress)
                           .map(a -> getCommunicationsModule().requestDM20(getListener(), a))
                           .flatMap(BusResult::toPacketStream)
                           .forEach(this::save);
    }

}
