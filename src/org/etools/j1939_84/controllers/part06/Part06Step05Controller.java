/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.6.5 DM20: Monitor Performance Ratio
 */
public class Part06Step05Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part06Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part06Step05Controller(Executor executor,
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
        // 6.6.5.1.a DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPN 3048)]) to OBD ECU(s) that responded
        // in part 5 (test 6.5.[6]) with DM20 data.
        // 6.6.5.2.a Fail if any ignition cycle counter (SPN 3048) from same ECU as was stored in part 5 has
        // incremented by a value other than 2.
        getDataRepository().getObdModuleAddresses()
                           .stream()
                           .filter(this::providedDM20InPart5)
                           .map(a -> getCommunicationsModule().requestDM20(getListener(), a))
                           .flatMap(BusResult::toPacketStream)
                           .filter(p -> p.getIgnitionCycles() != getPart5IgnCycles(p.getSourceAddress()) + 2)
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.6.5.2.a - Ignition cycle counter (SPN 3048) from "
                                       + moduleName +
                                       " has not incremented by two compared to the value recorded in part 5");
                           });
    }

    private boolean providedDM20InPart5(int address) {
        return getPart5DM20(address) != null;
    }

    private int getPart5IgnCycles(int address) {
        var dm20 = getPart5DM20(address);
        return dm20 == null ? -1 : dm20.getIgnitionCycles();
    }

    private DM20MonitorPerformanceRatioPacket getPart5DM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 5);
    }

}
