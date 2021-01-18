/*
 * Copyright (c) 2020. Electronic Tools Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;
import static org.etools.j1939_84.modules.DiagnosticReadinessModule.getCompositeSystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.part1.SectionA6Validator;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Part02Step02Controller extends StepController {
    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    private final DiagnosticReadinessModule diagnosticReadinessModule;

    private final SectionA6Validator sectionA6Validator;

    private final DataRepository dataRepository;

    Part02Step02Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticReadinessModule(),
             DateTimeModule.getInstance(),
             new SectionA6Validator(dataRepository),
             dataRepository);
    }

    Part02Step02Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticReadinessModule diagnosticReadinessModule,
                           DateTimeModule dateTimeModule,
                           SectionA6Validator sectionA6Validator,
                           DataRepository dataRepository) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.sectionA6Validator = sectionA6Validator;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
        diagnosticReadinessModule.setJ1939(getJ1939());
        // 6.2.2.1.a. Global DM5 (send Request (PGN 59904) for PGN 65230 (SPNs 1218-1223)).
        RequestResult<DM5DiagnosticReadinessPacket> globalDM5Result = diagnosticReadinessModule.requestDM5(getListener(),
                                                                                                           true);
        List<DM5DiagnosticReadinessPacket> globalDM5Packets = globalDM5Result.getPackets();
        List<DM5DiagnosticReadinessPacket> obdGlobalPackets = globalDM5Packets
                .stream()
                .filter(DM5DiagnosticReadinessPacket::isObd)
                .collect(Collectors.toList());
        if (!obdGlobalPackets.isEmpty()) {
            // 6.2.2.1.b. Display monitor readiness composite value in log for OBD ECU replies only.
            List<CompositeMonitoredSystem> systems = getCompositeSystems(obdGlobalPackets, true);
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM5:");
            getListener().onResult(systems.stream()
                                           .sorted()
                                           .map(MonitoredSystem::toString)
                                           .collect(Collectors.toList()));
            getListener().onResult("");
        } else {
            addFailure("6.2.2.1.a - Global DM5 request did not receive any response packets");
        }
        //6.2.2.2.a. Fail/warn per the section A.6 Criteria for Readiness 1 Evaluation.27
        sectionA6Validator.verify(getListener(), getPartNumber(), getStepNumber(), globalDM5Result);

        //6.2.2.2.b. Fail if any OBD ECU reports active/previously active fault DTC count not = 0/0.
        obdGlobalPackets.forEach(packet -> {
            byte activeCount = packet.getActiveCodeCount();
            byte prevCount = packet.getPreviouslyActiveCodeCount();
            if (activeCount != 0 || prevCount != 0) {
                addFailure("6.2.2.2.b - An OBD ECU reported active/previously active fault DTCs count not = 0/0"
                                   + NL + "  Reported active fault count = " + activeCount + NL
                                   + "  Reported previously active fault count = " + prevCount);
            }
        });

        //6.2.2.2.c. Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU.
        List<CompositeSystem> compositeSystems = globalDM5Packets.stream()
                .flatMap(packet -> packet.getMonitoredSystems().stream()
                        .filter(system -> system.getId() != CompositeSystem.COMPREHENSIVE_COMPONENT)
                        .filter(system -> system.getStatus().isEnabled()))
                .map(MonitoredSystem::getId)
                .collect(Collectors.toList());

        // reduce list to a single copy of each duplicate
        Set<CompositeSystem> duplicateCompositeSystems = compositeSystems.stream()
                .filter(system -> Collections.frequency(compositeSystems, system) > 1)
                .collect(Collectors.toSet());
        if (duplicateCompositeSystems.size() > 0) {
            StringBuilder warning = new StringBuilder(
                    "6.2.2.2.c - Listed below are the individually required monitors, except Continuous Component Monitoring (CCM)"
                            + NL + "  that have been reported as supported by more than one OBD ECU:");
            duplicateCompositeSystems.stream().sorted().forEach(system -> warning.append(NL)
                    .append("    ")
                    .append(system.getName().trim()));
            addWarning(warning.toString());
        }

        //6.2.2.3.a. DS DM5 to each OBD ECU.
        List<DM5DiagnosticReadinessPacket> destinationSpecificPackets = new ArrayList<>();
        dataRepository.getObdModuleAddresses()
                .forEach(address -> {
                    BusResult<DM5DiagnosticReadinessPacket> busResult = diagnosticReadinessModule
                            .requestDM5(getListener(), true, address);
                    busResult.getPacket()
                            .ifPresentOrElse(packet -> {
                                                 // No requirements around the destination specific acks
                                                 // so, the acks are not needed
                                                 packet.left.ifPresent(destinationSpecificPackets::add);
                                             },
                                             () -> addWarning(getPartNumber(),
                                                              getStepNumber(),
                                                              "6.2.2.3.a - OBD module " + getAddressName(
                                                                      address)
                                                                      + " did not return a response to a destination specific DM5 request"));
                });

        // 6.2.2.4.a. Fail if any difference compared to data received during global request.
        for (DM5DiagnosticReadinessPacket responsePacket : globalDM5Packets) {
            DM5DiagnosticReadinessPacket dsPacketResponse = null;
            for (DM5DiagnosticReadinessPacket dsPacket : destinationSpecificPackets) {
                if (dsPacket.getSourceAddress() == responsePacket.getSourceAddress()) {
                    dsPacketResponse = dsPacket;
                    break;
                }
            }
            if (dsPacketResponse != null && !dsPacketResponse.equals(responsePacket)) {
                addFailure("6.2.2.4.a - Difference compared to data received during global request");
                break;
            }
        }
    }
}
