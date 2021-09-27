/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static net.solidDesign.j1939.modules.CommunicationsModule.getCompositeSystems;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.packets.CompositeSystem;
import net.solidDesign.j1939.packets.DM26TripDiagnosticReadinessPacket;
import net.solidDesign.j1939.packets.DM5DiagnosticReadinessPacket;
import net.solidDesign.j1939.packets.MonitoredSystem;
import net.solidDesign.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.12.3 DM5: Diagnostic Readiness 1
 */
public class Part12Step03Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part12Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step03Controller(Executor executor,
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
        // 6.12.3.1.a. DS DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1221-1223)]) to each OBD ECU.
        var packets = getDataRepository().getObdModuleAddresses()
                                         .stream()
                                         .map(a -> getCommunicationsModule().requestDM5(getListener(), a))
                                         .map(BusResult::requestResult)
                                         .map(RequestResult::getPackets)
                                         .flatMap(Collection::stream)
                                         .collect(Collectors.toList());

        packets.forEach(this::save);

        // 6.12.3.1.b. Display monitor readiness composite value in log.
        getListener().onResult("");
        getListener().onResult("Vehicle Composite of DM5:");
        getCompositeSystems(packets, true).forEach(s -> getListener().onResult(s.toString()));

        // 6.12.3.2.a. Fail if any supported monitor (except CCM) that was “0 = complete” in part 11 is now reporting “1
        // = not complete.”.
        packets.forEach(this::reportNotCompleteSystems);

        // 6.12.3.3.a. Warn if DM5 reports fewer completed monitors than DM26 in step 6.12.2.1.
        packets.stream()
               .filter(p -> {
                   return countCompleteMonitors(p.getMonitoredSystems()) < getDM26CompleteMonitors(p.getSourceAddress());
               })
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.12.3.3.a - " + moduleName
                           + " DM5 reported fewer complete monitors than DM26 in step 6.12.2.1");
               });
    }

    private void reportNotCompleteSystems(DM5DiagnosticReadinessPacket currentDM5) {
        int address = currentDM5.getSourceAddress();
        String moduleName = currentDM5.getModuleName();

        var supportedSystems = currentDM5.getMonitoredSystems()
                                         .stream()
                                         .filter(s -> s.getStatus().isEnabled())
                                         .map(MonitoredSystem::getId)
                                         .filter(id -> id != CompositeSystem.COMPREHENSIVE_COMPONENT)
                                         .collect(Collectors.toList());

        var previouslyCompleteSupportedSystems = get(DM5DiagnosticReadinessPacket.class, address, 11)
                                                                                                    .getMonitoredSystems()
                                                                                                    .stream()
                                                                                                    .filter(s -> s.getStatus()
                                                                                                                  .isComplete())
                                                                                                    .map(MonitoredSystem::getId)
                                                                                                    .filter(supportedSystems::contains)
                                                                                                    .collect(Collectors.toList());

        currentDM5.getMonitoredSystems()
                  .stream()
                  .filter(s -> !s.getStatus().isComplete())
                  .map(MonitoredSystem::getId)
                  .filter(previouslyCompleteSupportedSystems::contains)
                  .map(CompositeSystem::getName)
                  .map(String::trim)
                  .sorted()
                  .forEach(system -> {
                      addFailure("6.12.3.2.a - " + moduleName + " reported " + system
                              + " as 'complete' in part 11 and is now reporting 'not complete'");
                  });
    }

    private long getDM26CompleteMonitors(int address) {
        var dm26 = get(DM26TripDiagnosticReadinessPacket.class, address, 12);
        if (dm26 == null) {
            return 0;
        }

        return countCompleteMonitors(dm26.getMonitoredSystems());
    }

    private static long countCompleteMonitors(List<MonitoredSystem> monitoredSystems) {
        return monitoredSystems.stream().filter(s -> s.getStatus().isComplete()).count();
    }

}
