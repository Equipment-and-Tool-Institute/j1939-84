/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.1.13 DM5: Diagnostic Readiness 1: Monitor Readiness
 */

public class Step13Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;
    private final SectionA6Validator sectionA6Validator;

    Step13Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticReadinessModule(),
             dataRepository,
             new SectionA6Validator(dataRepository),
             DateTimeModule.getInstance());
    }

    Step13Controller(Executor executor,
                     EngineSpeedModule engineSpeedModule,
                     BannerModule bannerModule,
                     VehicleInformationModule vehicleInformationModule,
                     DiagnosticReadinessModule diagnosticReadinessModule,
                     DataRepository dataRepository,
                     SectionA6Validator sectionA6Validator,
                     DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
        this.sectionA6Validator = sectionA6Validator;
    }

    @Override
    protected void run() throws Throwable {

        diagnosticReadinessModule.setJ1939(getJ1939());

        // 6.1.13.1.a. Global DM5 (send Request (PGN 59904) for PGN 65230 (SPNs 1218-1223)).
        RequestResult<DM5DiagnosticReadinessPacket> response = diagnosticReadinessModule.requestDM5(getListener(),
                                                                                                    true);
        List<DM5DiagnosticReadinessPacket> obdGlobalPackets = response.getPackets().stream()
                .filter(DM5DiagnosticReadinessPacket::isObd)
                .collect(Collectors.toList());
        if (!obdGlobalPackets.isEmpty()) {
            // got responses from the global request so log success
            // 6.1.13.1.b. Display monitor readiness composite value in log for OBD ECU replies only.

            List<CompositeMonitoredSystem> systems = DiagnosticReadinessModule.getCompositeSystems(obdGlobalPackets,
                                                                                                   true);
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM5:");
            getListener().onResult(systems.stream()
                                           .sorted()
                                           .map(MonitoredSystem::toString)
                                           .collect(Collectors.toList()));
            getListener().onResult("");
        } else {
            addFailure("6.1.13.1.a - Global DM5 request did not receive any response packets");
        }

        // 6.1.13.2.a. Fail/warn per section A.6, Criteria for Readiness 1 Evaluation
        sectionA6Validator.verify(getListener(), getPartNumber(), getStepNumber(), response);

        // 6.1.13.2.b. Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0.
        obdGlobalPackets.forEach(packet -> {
            if (packet.getActiveCodeCount() != 0 || packet.getPreviouslyActiveCodeCount() != 0) {
                addFailure(
                        "6.1.13.2.b - An OBD ECU reported active/previously active fault DTCs count not = 0/0"
                                + NL + "  Reported active fault count = " + packet.getActiveCodeCount() + NL
                                + "  Reported previously active fault count = " + packet.getPreviouslyActiveCodeCount());
            }
        });

        // 6.1.13.2.c. Fail if no OBD ECU provides DM5 with readiness bits showing monitor support.
        List<DM5DiagnosticReadinessPacket> dm5PacketsShowingMonitorSupport = obdGlobalPackets.stream()
                .filter(packet -> packet.getMonitoredSystems()
                        .stream()
                        .anyMatch(system -> system.getStatus().isEnabled()))
                .collect(Collectors.toList());
        if (dm5PacketsShowingMonitorSupport.isEmpty()) {
            addFailure("6.1.13.2.c - No OBD ECU provided DM5 with readiness bits showing monitor support");
        }

        // 6.1.13.2.d. Warn if any individual required monitor, except Continuous
        // Component Monitoring (CCM) is supported by more than one OBD ECU.
        // Get the list of duplicate composite systems
        List<CompositeSystem> compositeSystems = obdGlobalPackets.stream()
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
                    "6.1.13.2.d - An individual required monitor is supported by more than one OBD ECU");
            duplicateCompositeSystems.stream().sorted().forEach(system ->
                                                                        warning.append(NL)
                                                                                .append(system.getName())
                                                                                .append(" has reporting from more than one OBD ECU"));
            addWarning(warning.toString());
        }

        List<Integer> obdAddresses = dataRepository.getObdModuleAddresses();
        // 6.1.13.3.a. DS DM5 to each OBD ECU.
        List<BusResult<DM5DiagnosticReadinessPacket>> destinationSpecificPackets = obdAddresses
                .stream()
                .map(address -> diagnosticReadinessModule.requestDM5(getListener(), true, address))
                .collect(Collectors.toList());

        // 6.1.13.4.a. Fail if any difference compared to data received during global request.
        List<DM5DiagnosticReadinessPacket> dsDM5s = filterPackets(destinationSpecificPackets);
        compareRequestPackets(obdGlobalPackets, dsDM5s, "6.1.13.4.a");

        // 6.1.13.4.a. Fail if any difference compared to data received during global request.
        List<AcknowledgmentPacket> dsAcks = filterAcks(destinationSpecificPackets);
        checkForNACKs(obdGlobalPackets, dsAcks, dataRepository.getObdModuleAddresses(), "6.1.13.4.b.");
    }
}
