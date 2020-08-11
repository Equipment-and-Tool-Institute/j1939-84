/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class SectionA6Verifier {

    private final DataRepository dataRepository;
    private final DiagnosticReadinessModule diagnosticReadinessModule;
    private final DTCModule dtcModule;

    private final OBDTestsModule obdTestsModule;

    private final VehicleInformationModule vehicleInformationModule;

    public SectionA6Verifier(DataRepository dataRepository) {
        this(dataRepository, new DiagnosticReadinessModule(), new DTCModule(), new OBDTestsModule(),
                new VehicleInformationModule());
    }

    protected SectionA6Verifier(DataRepository dataRepository,
            DiagnosticReadinessModule diagnosticReadinessModule,
            DTCModule dtcModule,
            OBDTestsModule obdTestsModule,
            VehicleInformationModule vehicleInformationModule) {
        this.dataRepository = dataRepository;
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.dtcModule = dtcModule;
        this.obdTestsModule = obdTestsModule;
        this.vehicleInformationModule = vehicleInformationModule;
    }

    /**
     * Helper method to evaluate the composite systems according to Secton A6
     * Step 2 a
     *
     * @param message
     * @param packet
     */
    // TODO write a finder method so you can identify Monitored Systems by name
    // TODO Translate Table A6 into (not) Enabled & (not) Complete for each
    // Monitor
    // TODO This method below needs to consider the Fuel Type and if Code Clear
    // has been performed as well as Engine Model Year
    // TODO Evaluate each MonitoredSystem using the translated table based upon
    // the name of each monitor
    private void evaluateCompositeSystems(List<CompositeMonitoredSystem> compositeSystems) {
        // a. If one or more responses indicates 1 = supported in the
        // support bit for a monitor,
        compositeSystems.forEach(system -> {
            // i. Then the composite vehicle readiness shall indicate 1 =
            // supported for that support bit/monitor
            if (!system.getStatus().isEnabled()) {
                // message.append(
                // "Step 2.a - If one or more responses indicates 1 = supported
                // in the support bit for a monitor")
                // .append(NL)
                // .append("i. Then the composite vehicle readiness shall
                // indicate 1 = supported for that support bit/monitor")
                // .append(NL)
                // .append(packet.toString());
            } else if (system.getSourceAddress() == 0) {
                // ii. Else it shall indicate 0 = unsupported for that
                // support bit/monitor;

            }
        });
    }

    public void setJ1939(J1939 j1939) {
        diagnosticReadinessModule.setJ1939(j1939);
        dtcModule.setJ1939(j1939);
        obdTestsModule.setJ1939(j1939);
        vehicleInformationModule.setJ1939(j1939);
    }

    public boolean verify(ResultsListener listener) {

        boolean[] passed = { true };

        RequestResult<DM5DiagnosticReadinessPacket> response = diagnosticReadinessModule.requestDM5(listener, true);

        // 1. The response from each responding device shall be evaluated
        // separately using a through d below:
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        StringBuilder message = new StringBuilder("Section A.6 verification");
        obdModuleAddresses.forEach(address -> {
            // a. Fail if no response from an OBD ECU (ECUs that indicate 0x13,
            // 0x14, 0x22, or 0x23 for OBD compliance).
            if (!response.getPackets().stream().anyMatch(packet -> packet.getSourceAddress() == address &&
                    packet.isObd())) {
                message.append(
                        " failed at step 1.a - Fail if no response from an OBD ECU (ECUs that indicate 0x13, 0x14, 0x22, or 0x23 for OBD compliance)")
                        .append(NL)
                        .append("Source address " + address + " did not return a response");

            }
        });
        // b. Fail if any response does not report supported and
        // complete for comprehensive components support and status (SPN
        // 1221, byte 4, bit 3 = 1 and bit 7 = 0), except when all the
        // bits in SPNs 1221, 1222, and 1223 are sent as 0 as defined in
        // SAE J1939-73 paragraph 5.7.5.
        response.getPackets().forEach(packet -> {
            // b. Fail if any response does not report supported and
            // complete for comprehensive components support and status (SPN
            // 1221, byte 4, bit 3 = 1 and bit 7 = 0), except when all the
            // bits in SPNs 1221, 1222, and 1223 are sent as 0 as defined in
            // SAE J1939-73 paragraph 5.7.5.
            boolean isComplete = packet.getMonitoredSystems().stream()
                    .filter(monitoredSys -> monitoredSys.getName() == CompositeSystem.COMPREHENSIVE_COMPONENT.getName())
                    .anyMatch(system -> system.getStatus().isEnabled() && system.getStatus().isComplete());
            int[] bytes = packet.getPacket().getData(3, 7);
            int sum = 0;
            for (int val : bytes) {
                sum += val;
            }

            if (!isComplete && sum != 0) {
                message.append(" failed at step 1.b - Fail if any response does not report supported and")
                        .append(NL)
                        .append("complete for comprehensive components support and status (SPN")
                        .append(NL)
                        .append("1221, byte 4, bit 3 = 1 and bit 7 = 0), except when all the")
                        .append(NL)
                        .append("bits in SPNs 1221, 1222, and 1223 are sent as 0 as defined in")
                        .append(NL)
                        .append("SAE J1939-73 paragraph 5.7.5");
            }
            // c. Fail if any response does not report 0 = ‘complete/not
            // supported’ for the status bit for every unsupported monitors
            // (i.e., any of the support bits in SPN 1221, byte 4 bits 1-3,
            // 1222 byte 5 bits 1-8, or 1222 byte 6 bits 1-5 that report 0
            // also report 0 in the corresponding status bit in SPN 1221 and
            // 1223).
            if (!packet.getMonitoredSystems().stream()
                    .anyMatch(system -> !system.getStatus().isEnabled() && !system.getStatus().isComplete())) {
                message.append(" failed at step 1.c - Fail if any response does not report 0 = ‘complete/not")
                        .append(NL)
                        .append("supported’ for the status bit for every unsupported monitors")
                        .append(NL)
                        .append("(i.e., any of the support bits in SPN 1221, byte 4 bits 1-3,")
                        .append(NL)
                        .append("1222 byte 5 bits 1-8, or 1222 byte 6 bits 1-5 that report 0")
                        .append(NL)
                        .append("also report 0 in the corresponding status bit in SPN 1221 and")
                        .append(NL)
                        .append("1223");
            }
            // d. Fail if any response does not report 0 for reserved bits
            // (SPN 1221 byte 4 bits 4 and 8, SPN 1222 byte 6 bits 68, and
            // SPN1223 byte 8 bits 6-8).
            if ((packet.getPacket().getBytes()[1] & 0x88) != 0) {
                message.append(" failed at step 1.d - Fail if any response does not report 0 for reserved bits")
                        .append(NL)
                        .append("(SPN 1221 byte 4 bits 4 and 8, SPN 1222 byte 6 bits 68, and")
                        .append(NL)
                        .append("SPN1223 byte 8 bits 6-8)");
            }

        });
        // 2. All responses received from all responding OBD devices shall
        // be combined with appropriate ‘AND/OR’ logic to create a composite
        // vehicle readiness response: [Do not use responses from non-OBD
        // devices to create a composite vehicle readiness response.]
        Collection<MonitoredSystem> monitoredSystems = response.getPackets().stream()
                .filter(packet -> !packet.isObd())
                .flatMap(p -> p.getMonitoredSystems().stream())
                .collect(Collectors.toSet());

        List<CompositeMonitoredSystem> compositeSystems = DiagnosticReadinessModule
                .getCompositeSystems(monitoredSystems, true);

        evaluateCompositeSystems(compositeSystems);

        // b. If one or more responses indicates the status bit for a
        // supported monitor is 1 = not complete,

        // i. Then the composite vehicle readiness shall indicate 1 = not
        // complete for that status bit/monitor

        // ii. Else it shall indicate 0 = complete for that status
        // bit/monitor).

        // c. Fail if composite vehicle readiness does not meet any of the
        // criteria in Table A-6.

        // d. Warn if any individual required monitor, except Continuous
        // Component Monitoring (CCM) is supported by more than one OBD ECU.

        // 3. All responses received from non-OBD ECU shall be evaluated
        // using the criteria below:

        // a. Warn if any response from non-OBD ECU received.

        // b. Warn if all the monitor status and support bits in any reply
        // from a non-OBD ECU are not all binary zeros or all binary ones.

        listener.onProgress(message.toString());

        return passed[0];
    }
}