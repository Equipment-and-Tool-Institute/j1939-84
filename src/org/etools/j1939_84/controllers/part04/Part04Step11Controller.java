/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.11 DM20: Monitor Performance Ratio
 */
public class Part04Step11Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part04Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step11Controller(Executor executor,
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

        // 6.4.11.1.a DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPN 3048)]) to ECU(s) that responded in
        // part 1 with DM20 data.
        // 6.4.11.2.a Fail if ignition cycle counter (SPN 3048) for any ECU has not incremented by one compared
        // to value recorded at end of part 3.
        getDataRepository().getObdModuleAddresses()
                           .stream()
                           .filter(this::providedPart1DM20)
                           .map(a -> getDiagnosticMessageModule().requestDM20(getListener(), a))
                           .flatMap(BusResult::toPacketStream)
                           .peek(this::save)
                           .filter(p -> p.getIgnitionCycles() != getPart3IgnCycles(p.getSourceAddress()) + 1)
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.4.11.2.a - Ignition cycle counter (SPN 3048) from "
                                       + moduleName +
                                       " has not incremented by one compared to the value recorded at the end of part 3");
                           });
    }

    private boolean providedPart1DM20(int address) {
        return get(DM20MonitorPerformanceRatioPacket.class, address, 1) != null;
    }

    private int getPart3IgnCycles(int address) {
        var dm20 = get(DM20MonitorPerformanceRatioPacket.class, address, 3);
        return dm20 == null ? -1 : dm20.getIgnitionCycles();
    }

}
