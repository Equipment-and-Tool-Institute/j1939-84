/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.9.7 DM33: Emission Increasing Auxiliary Emission Control Device Active Time
 */
public class Part09Step07Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part09Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step07Controller(Executor executor,
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
        // 6.9.7.1.a Global DM33 [(send Request (PGN 59904) for PGN 41216 (SPNs 4124-4126)]).
        var packets = getCommunicationsModule().requestDM33(getListener()).getPackets();

        packets.forEach(this::save);

        // 6.9.7.2.a Fail if any ECU reports a different number of EI-AECD timers than was reported in part 2.
        // [Engines using SI technology need not respond until the 2024 engine model year]
        if (!isSparkIgnition() || getEngineModelYear() >= 2024) {
            packets.stream()
                   .filter(p -> isObdModule(p.getSourceAddress()))
                   .filter(p -> p.getEiAecdEngineHoursTimers().size() != getPrevDM33TimerCount(p.getSourceAddress()))
                   .map(ParsedPacket::getModuleName)
                   .forEach(moduleName -> {
                       addFailure("6.9.7.2.a - " + moduleName
                               + " reported a different number of EI-AECD timers than was reported in part 2");
                   });
        }

        // 6.9.7.1.b Create a list of ECU address + EI-AECD number + actual time (for Timer 1 and/or Timer 2) for any
        // with non-zero timer values.
        packets.forEach(this::save);
    }

    private int getPrevDM33TimerCount(int address) {
        var dm31 = get(DM33EmissionIncreasingAECDActiveTime.class, address, 2);
        return dm31 == null ? -1 : dm31.getEiAecdEngineHoursTimers().size();
    }

}
