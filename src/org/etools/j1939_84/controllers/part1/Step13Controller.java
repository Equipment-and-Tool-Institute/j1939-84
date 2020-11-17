/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The controller for 6.1.13 DM5: Diagnostic Readiness 1: Monitor
 *         Readiness
 */

public class Step13Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;

    private final DiagnosticReadinessModule diagnosticReadinessModule;
    private final SectionA6Validator sectionA6Validator;

    Step13Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new VehicleInformationModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), dataRepository, new SectionA6Validator(dataRepository));
    }

    Step13Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DiagnosticReadinessModule diagnosticReadinessModule, DataRepository dataRepository,
            SectionA6Validator sectionaA6Validator) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, partResultFactory,
                PART_NUMBER, STEP_NUMBER, TOTAL_STEPS);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dataRepository = dataRepository;
        sectionA6Validator = sectionaA6Validator;
    }

    /**
     * @param packets
     * @return
     */
    private StringBuilder buildDisplayMonitorReadinessLog(List<DM5DiagnosticReadinessPacket> packets) {

        StringBuilder displayReportBuilder = new StringBuilder("Global DM5 Vehicle Composite");
        packets.forEach(packet -> {
            displayReportBuilder
                    .append(NL + "  DM5 Response from " + Lookup.getAddressName(packet.getSourceAddress()));
            packet.getMonitoredSystems().stream().sorted().forEach(system -> {
                displayReportBuilder
                        .append(NL + String.format("%-35s", "    " + system.getName()))
                        .append(system.getStatus().isEnabled() ? String.format("%15s", "supported, ")
                                : String.format("%15s", "not supported, "))
                        .append(system.getStatus().isComplete() ? String.format("%15s", "completed")
                                : String.format("%15s", "not completed"));
            });
        });
        return displayReportBuilder;
    }

    @Override
    protected void run() throws Throwable {

        diagnosticReadinessModule.setJ1939(getJ1939());
        sectionA6Validator.setJ1939(getJ1939());

        // 6.1.13.1 Actions:
        // a. Global DM5 (send Request (PGN 59904) for PGN 65230 (SPNs
        // 1218-1223)).
        RequestResult<DM5DiagnosticReadinessPacket> response = diagnosticReadinessModule.requestDM5(getListener(),
                true);
        List<DM5DiagnosticReadinessPacket> obdGlobalPackets = response.getPackets().stream()
                .filter(packet -> !packet.isObd())
                .collect(Collectors.toList());
        if (!obdGlobalPackets.isEmpty()) {
            // got responses from the global request so log success
            addPass("6.1.13.1.a");
            // b. Display monitor readiness composite value in log for OBD ECU
            // replies only.
            StringBuilder displayReportBuilder = buildDisplayMonitorReadinessLog(obdGlobalPackets);
            getListener().onResult(displayReportBuilder.toString());
        } else {
            addFailure("6.1.13.1.a Global DM5 request did not receive any respone packets");
        }

        // 6.1.13.2 Fail/warn criteria:
        // a. Fail/warn per section A.6, Criteria for Readiness 1 Evaluation
        if (sectionA6Validator.verify(getListener(), getPartNumber(), getStepNumber())) {
            addPass("6.1.13.2.a");
        } else {
            addFailure("6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation");
        }
        // b. Fail if any OBD ECU reports active/previously active fault
        // DTCs count not = 0/0.
        boolean[] passed2b = { true };
        obdGlobalPackets.forEach(pack -> {
            if (pack.getActiveCodeCount() != 0 || pack.getPreviouslyActiveCodeCount() != 0) {
                addFailure(
                        "6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0"
                                + NL + "  Reported active fault count = " + pack.getActiveCodeCount() + NL
                                + "  Reported previously active fault count = " + pack.getPreviouslyActiveCodeCount());
                passed2b[0] = false;
            }

        });
        if (passed2b[0]) {
            addPass("6.1.13.2.b");
        }

        // c. Fail if no OBD ECU provides DM5 with readiness bits showing
        // monitor support.
        List<DM5DiagnosticReadinessPacket> dm5PacketsShowingMonitorSupport = obdGlobalPackets.stream().filter(
                packet -> {
                    return packet.getMonitoredSystems().stream().filter(system -> system.getStatus().isEnabled())
                            .count() > 0;
                })
                .collect(Collectors.toList());
        if (dm5PacketsShowingMonitorSupport.isEmpty()) {
            addFailure("6.1.13.2.c Fail if no OBD ECU provides DM5 with readiness bits showing monitor support");
        } else {
            addPass("6.1.13.2.c");
        }

        // d. Warn if any individual required monitor, except Continuous
        // Component Monitoring (CCM) is supported by more than one OBD ECU.
        // Get the list of duplicate composite systems
        List<CompositeSystem> compositeSystems = obdGlobalPackets.stream()
                .flatMap(packet -> packet.getMonitoredSystems().stream()
                        .filter(system -> system.getId() != CompositeSystem.COMPREHENSIVE_COMPONENT)
                        .filter(system -> system.getStatus().isEnabled()))
                .map(system -> system.getId())
                .collect(Collectors.toList());

        // reduce list to a single copy of each duplicate
        Set<CompositeSystem> duplicateCompositeSystems = compositeSystems.stream()
                .filter(system -> Collections.frequency(compositeSystems, system) > 1)
                .collect(Collectors.toSet());
        if (duplicateCompositeSystems.size() > 0) {
            StringBuilder warning = new StringBuilder(
                    "6.1.13.2.d Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU");
            duplicateCompositeSystems.stream().sorted().forEach(system -> {
                warning.append(NL + system.getName() + " has reporting from more than one OBD ECU");
            });
            addWarning(
                    warning.toString());
        } else {
            addPass("6.1.13.2.d");
        }

        // 6.1.13.3 Actions2:
        // a. DS DM5 to each OBD ECU.
        List<DM5DiagnosticReadinessPacket> destinationSpecificPackets = new ArrayList<>();
        dataRepository.getObdModuleAddresses().forEach(address -> {
            diagnosticReadinessModule.requestDM5(getListener(), true, address)
                    .getPacket()
                    .ifPresentOrElse((packet) -> {
                        // No requirements around the destination specific acks
                        // so, the acks are not needed
                        if (packet.left.isPresent()) {
                            destinationSpecificPackets.add(packet.left.get());
                        }
                    }, () -> {
                        addWarning("6.1.13.3 OBD module " + getAddressName(address)
                                + " did not return a response to a destination specific request");
                    });
        });
        if (destinationSpecificPackets.isEmpty()) {
            addWarning("6.1.13.3.a Destination Specific DM5 requests to OBD modules did not return any responses");
        } else {
            addPass("6.1.13.3.a");
        }

        // 6.1.13.4 Fail criteria:
        // a. Fail if any difference compared to data received during global
        // request.
        List<DM5DiagnosticReadinessPacket> differentPackets = obdGlobalPackets.stream().filter(aObject -> {
            return !destinationSpecificPackets.contains(aObject);
        }).collect(Collectors.toList());
        if (differentPackets.isEmpty()) {
            addPass("6.1.13.4.a");
        } else {
            addFailure("6.1.13.4.a Fail if any difference compared to data received during global request");
        }
        // b. Fail if NACK not received from OBD ECUs that did not respond to
        // global query.
        List<DM5DiagnosticReadinessPacket> nacksRemovedPackets = differentPackets.stream().filter(packet -> {
            return response.getAcks().stream().anyMatch(ack -> ack.getSourceAddress() != packet.getSourceAddress());
        }).collect(Collectors.toList());
        if (nacksRemovedPackets.isEmpty()) {
            addPass("6.1.13.4.b");
        } else {
            addFailure("6.1.13.4.b Fail if NACK not received from OBD ECUs that did not respond to global query");
        }
    }
}
