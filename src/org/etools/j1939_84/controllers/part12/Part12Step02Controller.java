/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static net.soliddesign.j1939tools.j1939.packets.CompositeSystem.COMPREHENSIVE_COMPONENT;
import static net.soliddesign.j1939tools.j1939.packets.CompositeSystem.MISFIRE;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.soliddesign.j1939tools.j1939.packets.CompositeSystem;
import net.soliddesign.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import net.soliddesign.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import net.soliddesign.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
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
        packets.forEach(this::save);

        // 6.12.2.2.a. Fail if any supported monitor (except CCM and Misfire) that was “0 = complete this cycle”
        // in part 11 is not reporting “1 = not complete this cycle.”.
        packets.forEach(this::reportNotCompleteSystems);

        // 6.12.2.2.b. Fail if NACK not received from OBD ECUs that did not provide a DM26 message
        checkForNACKsDS(packets, filterRequestResultAcks(dsResults), "6.12.2.2.b");
    }

    private void reportNotCompleteSystems(DM26TripDiagnosticReadinessPacket currentDM26) {
        int address = currentDM26.getSourceAddress();
        String moduleName = currentDM26.getModuleName();

        List<CompositeSystem> previouslyCompleteSupportedSystems = getPreviouslyCompleteSupportedSystems(address);

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

    private List<CompositeSystem> getPreviouslyCompleteSupportedSystems(int address) {
        var supportedSystems = getSupportedSystems(address);
        return get(DM26TripDiagnosticReadinessPacket.class, address, 11)
                                                                        .getMonitoredSystems()
                                                                        .stream()
                                                                        .filter(s -> s.getStatus().isComplete())
                                                                        .map(MonitoredSystem::getId)
                                                                        .filter(id -> id != COMPREHENSIVE_COMPONENT)
                                                                        .filter(id -> id != MISFIRE)
                                                                        .filter(supportedSystems::contains)
                                                                        .collect(Collectors.toList());
    }

    private List<CompositeSystem> getSupportedSystems(int address) {
        var dm5 = get(DM5DiagnosticReadinessPacket.class, address, 1);
        if (dm5 == null) {
            return List.of();
        }

        return dm5.getMonitoredSystems()
                  .stream()
                  .filter(s -> s.getStatus().isEnabled())
                  .map(MonitoredSystem::getId)
                  .filter(id -> id != COMPREHENSIVE_COMPONENT)
                  .filter(id -> id != MISFIRE)
                  .collect(Collectors.toList());
    }

}
