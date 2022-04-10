/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.etools.j1939tools.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.2.8 DM26: Diagnostic readiness 3
 */
public class Part02Step08Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part02Step08Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step08Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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

        // 6.2.8.1.a. DS DM26 (send Request (PGN 59904) for PGN 64952 (SPNs 3301-3305)) to each OBD ECU.
        List<DM26TripDiagnosticReadinessPacket> dsPackets = new ArrayList<>();

        getDataRepository().getObdModules().forEach(obdModuleInformation -> {
            int address = obdModuleInformation.getSourceAddress();
            String moduleName = Lookup.getAddressName(address);

            var result = getCommunicationsModule().requestDM26(getListener(), address);

            var resultPackets = result.getPackets();
            if (resultPackets != null) {
                dsPackets.addAll(resultPackets);

                // 6.2.8.2.a Fail if any difference in any ECU monitor support bits this cycle compared to
                // responses in part 1 after DM11.
                resultPackets.stream()
                             .filter(this::supportBitsChanged)
                             .findFirst()
                             .ifPresent(p -> addFailure("6.2.8.2.a - Difference from " + moduleName
                                     + " monitor support bits this cycle compared to responses in part 1 after DM11"));

                // 6.2.8.2.b Fail if any ECU reports number of warm-ups SCC (SPN 3302) greater than zero.
                resultPackets.stream()
                             .filter(p -> p.getWarmUpsSinceClear() > 0)
                             .findFirst()
                             .ifPresent(p -> addFailure("6.2.8.2.b - " + moduleName
                                     + " indicates number of warm-ups since code clear greater than zero"));

                // 6.2.8.2.c Fail if NACK not received from OBD ECUs that did not provide a DM26 response.
                if (resultPackets.isEmpty() && result.getAcks().stream().noneMatch(p -> p.getResponse() == NACK)) {
                    addFailure("6.2.8.2.c - " + moduleName
                            + " did not provide a NACK and did not provide a DM26 response");
                }

                // 6.2.8.1.a.i. Record time since engine start (SPN 3301) from each ECU and timestamp of when message
                // was received.
                // This is accomplished by keeping around the packets received.
            }
        });

        // 6.2.8.1.b. Display monitor readiness composite value in log for OBD ECU replies only.
        List<CompositeMonitoredSystem> compositeSystems = getCompositeSystems(dsPackets, false);
        if (!compositeSystems.isEmpty()) {
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM26:");
            getListener().onResult(compositeSystems.stream()
                                                   .sorted()
                                                   .map(MonitoredSystem::toString)
                                                   .collect(Collectors.toList()));
            getListener().onResult("");
        }

        // 6.2.8.3.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported
        // by more than one OBD ECU.
        reportDuplicateCompositeSystems(dsPackets, "6.2.8.3.a");

        // 6.2.8.4.a Global DM26.
        var globalPackets = getCommunicationsModule().requestDM26(getListener()).getPackets();

        // 6.2.8.4.b Record time since engine start (SPN 3301) from each ECU and timestamp of when message was received.
        // This is accomplished by keeping around the packets received.

        // 6.2.8.5.a Fail if any difference compared to data received from DS request when taking into account
        // additional time elapsed by differences in timestamps of responses received from DS requests
        // and global request [by ECU]. i.e., T2 – T1 <= SPN 3301 response data value <= T2 – T1 + 1 s.
        for (int address : getDataRepository().getObdModuleAddresses()) {
            String moduleName = Lookup.getAddressName(address);

            var globalOptional = globalPackets.stream().filter(p -> p.getSourceAddress() == address).findFirst();
            var dsOptional = dsPackets.stream().filter(p -> p.getSourceAddress() == address).findFirst();
            if (globalOptional.isPresent() && dsOptional.isPresent()) {
                var globalPacket = globalOptional.get();
                var globalReceivedTime = globalPacket.getPacket().getTimestamp();
                var globalWarmupTime = globalPacket.getTimeSinceEngineStart();

                var dsPacket = dsOptional.get();
                var dsReceivedTime = dsPacket.getPacket().getTimestamp();
                var dsWarmupTime = dsPacket.getTimeSinceEngineStart();

                long deltaReceivedSeconds = ChronoUnit.SECONDS.between(dsReceivedTime, globalReceivedTime);
                long deltaWarmupTime = Double.valueOf(globalWarmupTime - dsWarmupTime).longValue();

                long difference = Math.abs(deltaWarmupTime - deltaReceivedSeconds);
                if (difference > 1) {
                    addFailure("6.2.8.5.a - Difference in data between DS and global responses from " + moduleName);
                }
            } else if (globalOptional.isEmpty() && dsOptional.isEmpty()) {
                addInfo("6.2.8.5.a - No responses received from " + moduleName);
            } else if (globalOptional.isEmpty()) {
                addInfo("6.2.8.5.a - Global response was not received from " + moduleName);
            } else {
                addInfo("6.2.8.5.a - DS response was not received from " + moduleName);
            }
        }

    }

    private boolean supportBitsChanged(DM26TripDiagnosticReadinessPacket dm26) {
        return Arrays.stream(CompositeSystem.values())
                     .anyMatch(sys -> isSupported(sys, dm26) != isPrevSupported(sys, dm26.getSourceAddress()));
    }

    private boolean isPrevSupported(CompositeSystem systemId, int address) {
        var dm26 = get(DM26TripDiagnosticReadinessPacket.class, address, 1);
        var system = dm26 == null ? null
                : dm26.getMonitoredSystems()
                      .stream()
                      .filter(s -> s.getId() == systemId)
                      .findFirst()
                      .orElse(null);

        return system != null && system.getStatus().isEnabled();
    }

    private boolean isSupported(CompositeSystem systemId, DM26TripDiagnosticReadinessPacket dm26) {
        var system = dm26.getMonitoredSystems()
                         .stream()
                         .filter(s -> s.getId() == systemId)
                         .findFirst()
                         .orElse(null);

        return system != null && system.getStatus().isEnabled();
    }
}
