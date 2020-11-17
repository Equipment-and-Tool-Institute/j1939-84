/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The controller for 6.1.14 DM26: Diagnostic readiness 3
 */

public class Step14Controller extends StepController {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 14;

    private static final int TOTAL_STEPS = 1;

    private final DataRepository dataRepository;
    private final DiagnosticReadinessModule diagnosticReadinessModule;

    private final DTCModule dtcModule;

    Step14Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new VehicleInformationModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), new DTCModule(), dataRepository);
    }

    Step14Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DiagnosticReadinessModule diagnosticReadinessModule, DTCModule dtcModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, partResultFactory,
                PART_NUMBER, STEP_NUMBER, TOTAL_STEPS);
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {

        diagnosticReadinessModule.setJ1939(getJ1939());
        dtcModule.setJ1939(getJ1939());

        // 6.1.14.1 Actions:
        // a. Global DM26 (send Request (PGN 59904) for PGN 64952 (SPNs
        // 3301-3305)).
        // i. Create list by ECU address of all data and current status for
        // use later in the test.
        RequestResult<DM26TripDiagnosticReadinessPacket> globalResponse = dtcModule.requestDM26(getListener());
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        if (!globalResponse.getPackets().isEmpty()) {
            // b. Display monitor readiness composite value in log for OBD ECU
            // replies only.
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM26:");
            List<CompositeMonitoredSystem> dm26Systems = DiagnosticReadinessModule
                    .getCompositeSystems(globalResponse.getPackets().stream().filter(p -> {
                        return obdModuleAddresses.contains(p.getSourceAddress());
                    }).collect(Collectors.toList()), false);
            getListener().onResult(dm26Systems.stream().sorted().map(system -> system.toString())
                    .collect(Collectors.toList()));

            // 6.1.14.2 Fail criteria:
            DiagnosticReadinessModule.getCompositeSystems(dataRepository.getObdModules().stream()
                    .flatMap(module -> module.getMonitoredSystems().stream())
                    .sorted()
                    .collect(Collectors.toList()), true).stream().forEach(system -> {
                        // a. Fail if any response for any monitor supported in
                        // DM5 by a given ECU is reported as '0=monitor complete
                        // this cycle or not supported' in SPN 3303 bits 1-4 and
                        // SPN 3305 [except comprehensive components monitor
                        // (CCM)].
                        if (system.getStatus().isEnabled() && dm26Systems.stream()
                                .anyMatch(s -> (s.getId() == system.getId()) && (!s.getStatus().isEnabled()))) {
                            addFailure(
                                    "6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response");
                        } else if (!system.getStatus().isEnabled() && dm26Systems.stream()
                                .anyMatch(s -> (s.getId() == system.getId()) && (s.getStatus().isEnabled()))) {
                            // b. Fail if any response for each monitor not
                            // supported in DM5 by a given ECU is not also
                            // reported in DM26 as '0=monitor complete this
                            // cycle or not supported' in SPN 3303 bits 5-7 and
                            // '0=monitor disabled for rest of this cycle or not
                            // supported' in SPN 3303 bits 1-2 and SPN 3304.20
                            addFailure(
                                    "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                                            + NL + system.toString());
                            if (system.getId() == CompositeSystem.COMPREHENSIVE_COMPONENT
                                    && !system.getStatus().isEnabled() && dm26Systems.stream()
                                            .anyMatch(s -> s.getId() == system.getId() && s.getStatus().isEnabled())) {
                                // c. Fail if any response from an ECU
                                // indicating
                                // support for CCM monitor in DM5 reports
                                // '0=monitor
                                // disabled for rest of this cycle or not
                                // supported'
                                // in SPN 3303 bit 3.
                                addFailure(
                                        "6.1.14.2.b Fail if any response from an ECU indicating support for CCM monitor in DM5 is report as not supported by DM26 response");
                            }
                        }
                    });

            // d. Fail if any response indicates number of warm-ups since
            // code clear (WU-SCC) (SPN 3302) is not zero.
            List<DM26TripDiagnosticReadinessPacket> warmUpsFailedPackets = globalResponse.getPackets().stream()
                    .filter(packet -> packet.getWarmUpsSinceClear() != 0).collect(Collectors.toList());
            if (!warmUpsFailedPackets.isEmpty()) {
                String[] failureMessage = {
                        "6.1.14.2.d Fail if any response indicates number of warm-ups since code clear is not zero" + NL
                };
                warmUpsFailedPackets.forEach(packet -> {
                    failureMessage[0] += packet.toString() + NL;
                });
                addFailure(
                        failureMessage[0]);
            }

            // e. Fail if any response indicates time since engine start
            // (SPN 3301) is not zero.
            globalResponse.getPackets().stream().forEach(packet -> {
                if (packet.getTimeSinceEngineStart() != 0) {
                    StringBuilder failure = new StringBuilder(
                            "6.1.14.2.e Fail if any response indicates time since engine start is not zero" + NL);
                    failure.append(packet.toString() + NL);
                    addFailure(failure.toString());
                }
            });

            // 6.1.14.3 Warn criteria:
            // a. Warn if any individual required monitor, except Continuous
            // Component Monitoring (CCM) is supported by more than one OBD ECU.
            // Get the list of duplicate composite systems
            List<CompositeSystem> compositeSystems = globalResponse.getPackets().stream()
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
                        "6.1.14.3.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU");
                duplicateCompositeSystems.stream().sorted().forEach(system -> {
                    warning.append(NL + system.getName() + " has reporting from more than one OBD ECU");
                });
                addWarning(
                        warning.toString());
            }

            // 6.1.14.4 Actions:
            // a. DS DM26 to each OBD ECU.
            List<DM26TripDiagnosticReadinessPacket> destinationSpecificPackets = new ArrayList<>();
            dataRepository.getObdModuleAddresses().forEach(address -> {
                destinationSpecificPackets.addAll(dtcModule.requestDM26(getListener(), address).getPackets());
            });
            if (destinationSpecificPackets.isEmpty()) {
                addWarning("6.1.14.4.a Destination Specific DM5 requests to OBD modules did not return any responses");
            }

            // 6.1.14.5 Fail criteria:
            // a. Fail if any difference compared to data received during
            // global request.
            List<DM26TripDiagnosticReadinessPacket> differentPackets = globalResponse.getPackets().stream()
                    .filter(packet -> {
                        return !destinationSpecificPackets.contains(packet);
                    }).collect(Collectors.toList());
            if (!differentPackets.isEmpty()) {
                addFailure("6.1.14.5.a Fail if any difference compared to data received during global request");
            }

            // b. Fail if NACK not received from OBD ECUs that did not respond
            // to global query.
            List<DM26TripDiagnosticReadinessPacket> nacksRemovedPackets = differentPackets.stream().filter(packet -> {
                return globalResponse.getAcks().stream()
                        .anyMatch(ack -> ack.getSourceAddress() != packet.getSourceAddress());
            }).collect(Collectors.toList());
            if (!nacksRemovedPackets.isEmpty()) {
                addFailure("6.1.14.5.b Fail if NACK not received from OBD ECUs that did not respond to global query");
            }
        } else {
            // 6.1.14.2.f. Fail if no OBD ECU provides DM26.
            addFailure("6.1.14.2.f. Fail if no OBD ECU provides DM26");
        }

    }
}
