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
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
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

        List<DM26TripDiagnosticReadinessPacket> dsPackets = new ArrayList<>();

        // 6.2.8.1.a. DS DM26 (send Request (PGN 59904) for PGN 64952 (SPNs 3301-3305)) to each OBD ECU.
        // 6.2.8.1.a.i. Record time since engine start (SPN 3301) from each ECU and timestamp of when message
        // was received.
        // This is accomplished by keeping around the packets received.
        getDataRepository().getObdModules().forEach(obdModuleInformation -> {
            int address = obdModuleInformation.getSourceAddress();
            String moduleName = Lookup.getAddressName(address);

            var result = getCommunicationsModule().requestDM26(getListener(), address).getEither();

            if (result != null) {
                result.forEach(packet -> {
                    if (packet.left.isPresent()) {
                        var dm26 = packet.left.get();
                        dsPackets.add(dm26);

                        // 6.2.8.2.a Fail if any response for each monitor not supported in DM5 by a given ECU from
                        // 6.2.2.3 is also reported in DM26 as “1=monitor not complete this monitoring cycle” in
                        // SP 3303 bits 5-7.
                        // Unsupported monitors will never need to run and are ‘de facto’ complete.
                        if (isDm5NotSupportedAndDm26NotCompleted(dm26)) {
                            addFailure("6.2.8.2.a - DM5 message in 6.2.2.3 from " + moduleName
                                    + " monitor reported not supported and DM26 message reported not complete");
                        }

                        // 6.2.8.2.b Fail if any response for each monitor not supported in DM5 by a given ECU is also
                        // reported in DM26 as “0=monitor enabled for this monitoring cycle” in SP 3303 bits 1 and 2 and
                        // SP 3304.
                        // Unsupported monitors will never run and cannot be enabled.
                        if (isDm5NotSupportedAndDm26Enabled(dm26)) {
                            addFailure("6.2.8.2.b - DM5 message in 6.2.2.3 from " + moduleName
                                    + " monitor reported not supported and DM26 message reported enable");
                        }

                        // 6.2.8.2.c Fail if any response from an ECU indicating support for CCM monitor in DM5 reports
                        // “0=monitor disabled for rest of this cycle or not supported” in SP 3303 bit 3.
                        if (isCCMMonitorAndDm26Disabled(dm26)) {
                            addFailure("6.2.8.2.c - DM5 message in 6.2.2.3 from " + moduleName
                                    + " monitor reported CCM supported and DM26 message reported disabled");
                        }

                        // 6.2.8.2.d Fail if any ECU reports number of warm-ups SCC (SP 3302) greater than zero.
                        if (dm26.getWarmUpsSinceClear() > 0) {
                            addFailure("6.2.8.2.d - " + moduleName
                                    + " indicates number of warm-ups since code clear greater than zero");
                        }

                        // 6.2.8.3.b Info, if any response for any monitor supported in DM5 by a given ECU is reported
                        // as “0=monitor complete this cycle or not supported” in SP 3303 bits 1-4 and SP 3305 [except
                        // comprehensive components monitor (CCM)]
                        if (isDm5SupportedAndDm26Complete(dm26)) {
                            addInfo("6.2.8.3.b - DM5 message in 6.2.2.3 from " + moduleName
                                    + " monitor reported supported and DM26 message reported complete or not supported");
                        }

                    }
                });
            }
        });

        // 6.2.8.1.b. Display monitor readiness composite value in log for OBD ECU replies only.
        List<CompositeMonitoredSystem> compositeSystems = getCompositeSystems(dsPackets, false);
        if (!compositeSystems.isEmpty()) {
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM26:");
            getListener().onResult(compositeSystems.stream()
                                                   .map(MonitoredSystem::toString)
                                                   .collect(Collectors.toList()));
            getListener().onResult("");
        }

        // 6.2.8.3.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported
        // by more than one OBD ECU.
        reportDuplicateCompositeSystems(dsPackets, "6.2.8.3.a");

        // 6.2.8.4.a Global DM26.
        var globalResult = getCommunicationsModule().requestDM26(getListener());

        // 6.2.8.4.b Record time since engine start (SPN 3301) from each ECU and timestamp of when message was received.
        // This is accomplished by keeping around the packets received.

        for (int address : getDataRepository().getObdModuleAddresses()) {
            String moduleName = Lookup.getAddressName(address);
            var globalPacket = globalResult.getPackets()
                                           .stream()
                                           .filter(packet -> packet.getSourceAddress() == address)
                                           .findFirst();
            var globalAck = globalResult.getAcks()
                                        .stream()
                                        .filter(packet -> packet.getSourceAddress() == address)
                                        .findFirst();

            // 6.2.8.5.a Fail if any difference compared to data received from DS request when taking into account
            // additional time elapsed by differences in timestamps of responses received from DS requests
            // and global request [by ECU]. i.e., T2 – T1 <= SPN 3301 response data value <= T2 – T1 + 1 s.
            var dsOptional = dsPackets.stream().filter(p -> p.getSourceAddress() == address).findFirst();
            if (globalPacket.isPresent() && dsOptional.isPresent()) {
                var globalReceivedTime = globalPacket.get().getPacket().getTimestamp();
                var globalWarmupTime = globalPacket.get().getTimeSinceEngineStart();

                var dsPacket = dsOptional.get();
                var dsReceivedTime = dsPacket.getPacket().getTimestamp();
                var dsWarmupTime = dsPacket.getTimeSinceEngineStart();

                long deltaReceivedSeconds = ChronoUnit.SECONDS.between(dsReceivedTime, globalReceivedTime);
                long deltaWarmupTime = Double.valueOf(globalWarmupTime - dsWarmupTime).longValue();

                long difference = Math.abs(deltaWarmupTime - deltaReceivedSeconds);
                if (difference > 1) {
                    addFailure("6.2.8.5.a - Difference in data between DS and global responses from " + moduleName);
                }
            } else if (globalAck.isPresent() && dsOptional.isPresent()) {
                // 6.2.8.5.b Fail if NACK not received from OBD ECUs that did not respond to global query.
                if (globalAck.get().getResponse() != NACK) {
                    addFailure("6.2.8.5.b - Response received to global query from " + moduleName
                            + " is not a NACK");
                }
            } else if (globalPacket.isEmpty() && dsOptional.isEmpty()) {
                addInfo("6.2.8.5.a - No responses received from " + moduleName);
            } else if (globalPacket.isEmpty()) {
                addInfo("6.2.8.5.a - Global response was not received from " + moduleName);
            } else {
                addInfo("6.2.8.5.a - DS response was not received from " + moduleName);
            }
        }
    }

    private boolean isDm5Supported(CompositeSystem systemId, int address) {
        var dm5 = get(DM5DiagnosticReadinessPacket.class, address, 2);
        var system = dm5 == null ? null
                : dm5.getMonitoredSystems()
                     .stream()
                     .filter(s -> s.getId() == systemId)
                     .findFirst()
                     .orElse(null);

        return system != null && system.getStatus().isEnabled();
    }

    private boolean isCCMMonitorAndDm26Disabled(DM26TripDiagnosticReadinessPacket dm26) {
        return Arrays.stream(CompositeSystem.values())
                     .anyMatch(sys -> sys == CompositeSystem.COMPREHENSIVE_COMPONENT
                             && isDm5Supported(sys, dm26.getSourceAddress())
                             && !isDm26Enabled(sys, dm26));
    }

    private boolean isDm5NotSupportedAndDm26Enabled(DM26TripDiagnosticReadinessPacket dm26) {
        return Arrays.stream(CompositeSystem.values())
                     .anyMatch(sys -> !isDm5Supported(sys, dm26.getSourceAddress()) && isDm26Enabled(sys, dm26));
    }

    private boolean isDm5NotSupportedAndDm26NotCompleted(DM26TripDiagnosticReadinessPacket dm26) {
        return Arrays.stream(CompositeSystem.values())
                     .anyMatch(sys -> !isDm5Supported(sys, dm26.getSourceAddress()) && !isDm26Completed(sys, dm26));
    }

    private boolean isDm5SupportedAndDm26Complete(DM26TripDiagnosticReadinessPacket dm26) {
        return Arrays.stream(CompositeSystem.values())
                     .anyMatch(sys -> sys != CompositeSystem.COMPREHENSIVE_COMPONENT
                             && isDm5Supported(sys, dm26.getSourceAddress()) && isDm26Completed(sys, dm26));
    }

    private boolean isDm26Completed(CompositeSystem systemId, DM26TripDiagnosticReadinessPacket dm26) {
        var system = dm26.getMonitoredSystems()
                         .stream()
                         .filter(s -> s.getId() == systemId)
                         .findFirst()
                         .orElse(null);

        return system != null && system.getStatus().isComplete();
    }

    private boolean isDm26Enabled(CompositeSystem systemId, DM26TripDiagnosticReadinessPacket dm26) {
        var system = dm26.getMonitoredSystems()
                         .stream()
                         .filter(s -> s.getId() == systemId)
                         .findFirst()
                         .orElse(null);

        return system != null && system.getStatus().isEnabled();
    }
}
