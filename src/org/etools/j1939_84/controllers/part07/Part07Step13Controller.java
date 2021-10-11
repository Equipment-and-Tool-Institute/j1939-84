/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.7.13 DM20: Monitor Performance Ratio
 */
public class Part07Step13Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    Part07Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step13Controller(Executor executor,
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
        // 6.7.13.1.a. DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPN 3048)]) to ECU(s) that responded in part 5
        // with DM20 data.
        // 6.7.13.2.a. Fail if ignition cycle counter (SPN 3048) for any ECU has incremented by other than 3 cycles from
        // part 5.
        getDataRepository().getObdModuleAddresses()
                           .stream()
                           .filter(a -> getIgnCycles(a) != -1)
                           .map(a -> getCommunicationsModule().requestDM20(getListener(), a))
                           .flatMap(BusResult::toPacketStream)
                           .peek(this::save)
                           .filter(p -> p.getIgnitionCycles() != getIgnCycles(p.getSourceAddress()) + 3)
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.7.13.2.a - Ignition cycle counter for " + moduleName
                                       + " has incremented by other than 3 cycles from part 5");
                           });
    }

    private int getIgnCycles(int address) {
        var dm20 = get(DM20MonitorPerformanceRatioPacket.class, address, 5);
        return dm20 == null ? -1 : dm20.getIgnitionCycles();
    }

}
