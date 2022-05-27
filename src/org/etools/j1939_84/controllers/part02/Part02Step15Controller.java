/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939tools.j1939.packets.EngineHoursTimer;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.15 DM33: Emission increasing auxiliary emission control device active time
 */
public class Part02Step15Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part02Step15Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    Part02Step15Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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

    private static List<EngineHoursTimer> getTimers(int address, List<DM33EmissionIncreasingAECDActiveTime> packets) {
        return packets
                      .stream()
                      .filter(p -> p.getSourceAddress() == address)
                      .map(DM33EmissionIncreasingAECDActiveTime::getEiAecdEngineHoursTimers)
                      .findFirst()
                      .orElse(List.of());
    }

    private static EngineHoursTimer getTimer(List<EngineHoursTimer> timers, int timerNumber) {
        return timers
                     .stream()
                     .filter(t -> t.getEiAecdNumber() == timerNumber)
                     .findFirst()
                     .orElse(null);
    }

    @Override
    protected void run() throws Throwable {

        // 6.2.15.1.a. Global DM33 (send Request (PGN 59904) for PGN 41216 (SPNs 4124-4126)).
        var globalPackets = getCommunicationsModule().requestDM33(getListener()).getPackets();

        // 6.2.15.1.b. Create list of reported EI-AECD timers by ECU.
        globalPackets.forEach(this::save);

        // 6.2.15.2.a. Fail if no ECU responds.
        // [Engines using SI technology need not respond until the 2024 engine model year].
        if (globalPackets.isEmpty()) {
            if (!isSparkIgnition() || getEngineModelYear() >= 2024) {
                addFailure("6.2.15.2.a - No ECU responded to the global request");
            }
        } else {
            // 6.2.15.3.a. Warn if only response(s) = 0xFB (no EI-AECDs) for EI-AECD number (byte 1).
            for (var dm33 : globalPackets) {
                dm33.getEiAecdEngineHoursTimers()
                    .stream()
                    .findFirst()
                    .map(EngineHoursTimer::getEiAecdNumber)
                    .filter(n -> n == 0xFB)
                    .map(v -> dm33.getModuleName())
                    .ifPresent(moduleName -> addWarning("6.2.15.3.a - " + moduleName
                            + " responded 0xFB for EI-AECD number"));
            }
        }

        // 6.2.15.4.a. DS DM33 to each OBD ECU.
        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();
        var dsResponses = obdModuleAddresses
                                            .stream()
                                            .map(address -> getCommunicationsModule().requestDM33(getListener(),
                                                                                                  address))
                                            .collect(Collectors.toList());

        // 6.2.15.5.a. Fail if any difference is detected when response data is compared to data received
        // from global request, which is greater than 2 minutes more than the times reported
        // from the responses received from the global request in 6.2.15.2.
        for (int address : obdModuleAddresses) {
            String moduleName = Lookup.getAddressName(address);

            var globalTimers = getTimers(address, globalPackets);
            var dsTimers = getTimers(address, filterRequestResultPackets(dsResponses));

            for (int i = 0; i <= 250; i++) {
                var globalTimer = getTimer(globalTimers, i);
                var dsTimer = getTimer(dsTimers, i);
                if (globalTimer != null && dsTimer != null) {
                    long difference = dsTimer.getEiAecdTimer1() - globalTimer.getEiAecdTimer1();
                    if (difference > 2) {
                        addFailure("6.2.15.5.a - " + moduleName + " reported EiAECD Timer 1 from timer " + i
                                + " with a difference of " + difference + " which is greater than 2 minutes");
                    }

                    difference = dsTimer.getEiAecdTimer2() - globalTimer.getEiAecdTimer2();
                    if (difference > 2) {
                        addFailure("6.2.15.5.a - " + moduleName + " reported EiAECD Timer 2 from timer " + i
                                + " with a difference of " + difference + " which is greater than 2 minutes");
                    }
                } else if (globalTimer != null || dsTimer != null) {
                    addFailure("6.2.15.5.a - " + moduleName + " did not return timer " + i + " in both responses");
                }
            }
        }

        if (!isSparkIgnition() || getEngineModelYear() >= 2024) {
            // 6.2.15.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
            checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResponses), "6.2.15.5.b");
        }
    }

}
