/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9.24 DM33: Emission Increasing Auxiliary Emission Control Device Active Time
 */
public class Part09Step24Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 24;
    private static final int TOTAL_STEPS = 0;

    Part09Step24Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step24Controller(Executor executor,
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
        // 6.9.24.1.a. DS DM33 [(send Request (PGN 59904) for PGN 41216 (SPNs 4124-4126))] to each OBD ECU.
        var dsResponses = getDataRepository().getObdModuleAddresses()
                                             .stream()
                                             .map(address -> getDiagnosticMessageModule().requestDM33(getListener(),
                                                                                                      address))
                                             .collect(Collectors.toList());

        var dsPackets = filterRequestResultPackets(dsResponses);
        dsPackets.forEach(packet -> {
            var previousDM33Timers = getDataRepository().getObdModule(packet.getSourceAddress())
                                                        .get(DM33EmissionIncreasingAECDActiveTime.class)
                                                        .getEiAecdEngineHoursTimers();
            // 6.9.24.2.a. Fail if any ECU reports a different number EI-AECD than was reported in part 2.
            var packetTimers = packet.getEiAecdEngineHoursTimers();
            if (packetTimers.size() != previousDM33Timers.size()) {
                addFailure("6.9.24.2.a - ECU " + packet.getModuleName() + " reported a different number EI-AECD here ("
                        + packetTimers.size() + ") and reported (" + previousDM33Timers.size() + ") in part 2");
            }
            // 6.9.24.2.b. Compare to list of ECU address + EI-AECD number + actual time (for Timer 1 and/or Timer 2)
            // for any with non- zero timer values created earlier in step 6.9.7.1 and fail if any timer value is less
            // than the value it was earlier in this part.
            for (EngineHoursTimer previousTimer : previousDM33Timers) {
                if (previousTimer.getEiAecdTimer1() > 0 || previousTimer.getEiAecdTimer2() > 0) {
                    int timeId = previousTimer.getEiAecdNumber();
                    var currentTimer = packet.getTimer(timeId);
                    if (currentTimer != null) {
                        if (currentTimer.getEiAecdTimer1() < previousTimer.getEiAecdTimer1()) {
                            addFailure("6.9.24.2.b - ECU " + packet.getModuleName()
                                    + " reported timer 1 value less than previously observed in 6.9.7.1");
                        }
                        if (currentTimer.getEiAecdTimer2() < previousTimer.getEiAecdTimer2()) {
                            addFailure("6.9.24.2.b - ECU " + packet.getModuleName()
                                    + " reported timer 2 value less than previously observed in 6.9.7.1");
                        }

                    }
                }
            }
        });

        // 6.9.24.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM33 message.
        checkForNACKsDS(filterRequestResultPackets(dsResponses), filterRequestResultAcks(dsResponses), "6.9.24.2.c.");
    }

}
