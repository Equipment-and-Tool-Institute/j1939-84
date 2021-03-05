/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

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
             new DiagnosticMessageModule());
    }

    Part12Step02Controller(Executor executor,
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
        // 6.12.2.1.a. DS DM26 [(send Request (PGN 59904) for PGN 64952 (SPNs 3303-3305)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM26(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterRequestResultPackets(dsResults);

        // 6.12.2.2.a. Fail if any supported monitor (except CCM) that was “0 = complete this cycle” in part 11 is not
        // reporting “1 = not complete this cycle.”.
        packets.forEach(this::reportNotCompleteSystems);

        // 6.12.2.2.b. Fail if NACK not received from OBD ECUs that did not provide a DM26 message
        checkForNACKsDS(packets, filterRequestResultAcks(dsResults), "6.12.2.2.b");

        // Save packets for next step
        packets.forEach(this::save);
    }

    private void reportNotCompleteSystems(DM26TripDiagnosticReadinessPacket currentDM26) {
        int address = currentDM26.getSourceAddress();
        String moduleName = currentDM26.getModuleName();

        var supportedSystems = getSupportedSystems(address);

        var previouslyCompleteSupportedSystems = get(DM26TripDiagnosticReadinessPacket.class,
                                                     address)
                                                             .getMonitoredSystems()
                                                             .stream()
                                                             .filter(s -> s.getStatus().isComplete())
                                                             .map(MonitoredSystem::getId)
                                                             .filter(supportedSystems::contains)
                                                             .collect(Collectors.toList());

        currentDM26.getMonitoredSystems()
                   .stream()
                   .filter(s -> !s.getStatus().isComplete())
                   .map(MonitoredSystem::getId)
                   .filter(previouslyCompleteSupportedSystems::contains)
                   .map(CompositeSystem::getName)
                   .map(String::trim)
                   .sorted()
                   .forEach(system -> {
                       addFailure("6.12.2.2.a - " + moduleName + " reported " + system
                               + " as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
                   });
    }

    private List<CompositeSystem> getSupportedSystems(int address) {
        var dm5 = get(DM5DiagnosticReadinessPacket.class, address);
        if (dm5 == null) {
            return List.of();
        }

        return dm5.getMonitoredSystems()
                  .stream()
                  .filter(s -> s.getStatus().isEnabled())
                  .map(MonitoredSystem::getId)
                  .filter(id -> id != CompositeSystem.COMPREHENSIVE_COMPONENT)
                  .collect(Collectors.toList());
    }

}
