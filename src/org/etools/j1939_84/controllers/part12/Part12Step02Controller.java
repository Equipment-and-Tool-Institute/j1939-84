/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.12.2 DM26: Diagnostic Readiness 3
 */
public class Part12Step02Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part12Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step02Controller(Executor executor,
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
        // 6.12.2.1.a. DS DM26 [(send Request (PGN 59904) for PGN 64952 (SPNs 3303-3305)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM26(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterRequestResultPackets(dsResults);
        // 6.12.2.2.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported
        // by more than one OBD ECU.
        reportDuplicateCompositeSystems(packets, "6.12.2.2.a");

        packets.forEach(packet -> {
            save(packet);
            // 6.12.2.2.b. Info, if any supported monitor supported in DM5 by a given ECU is reported as (except
            // CCM) that was “0 = monitor complete this cycle or not supported” ” in SP 3303 bits 1-4 and SP 3305
            // [except comprehensive
            if (isDm5SupportedAndDm26Complete(packet)) {
                addInfo("6.12.2.2.b - DM5 message in 6.11.10.1.a from "
                        + Lookup.getAddressName(packet.getSourceAddress())
                        + " monitor reported supported and DM26 message reported complete or not supported");
            }
        });

        // 6.12.2.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM26 message
        checkForNACKsDS(packets, filterRequestResultAcks(dsResults), "6.12.2.2.c");
    }

    private boolean isDm5SupportedAndDm26Complete(DM26TripDiagnosticReadinessPacket dm26) {
        return Arrays.stream(CompositeSystem.values())
                     .anyMatch(sys -> sys != CompositeSystem.COMPREHENSIVE_COMPONENT
                             && isDm5Supported(sys, dm26.getSourceAddress()) && isDm26Completed(sys, dm26));
    }

    private boolean isDm5Supported(CompositeSystem systemId, int address) {
        var dm5 = get(DM5DiagnosticReadinessPacket.class, address, 11);
        var system = dm5 == null ? null
                : dm5.getMonitoredSystems()
                     .stream()
                     .filter(s -> s.getId() == systemId)
                     .findFirst()
                     .orElse(null);

        return system != null && system.getStatus().isEnabled();
    }

    private boolean isDm26Completed(CompositeSystem systemId, DM26TripDiagnosticReadinessPacket dm26) {
        var system = dm26.getMonitoredSystems()
                         .stream()
                         .filter(s -> s.getId() == systemId)
                         .findFirst()
                         .orElse(null);

        return system != null && system.getStatus().isComplete();
    }

}
