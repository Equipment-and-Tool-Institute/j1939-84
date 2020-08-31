/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.PASS;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;

/**
 *
 * @author Marianne Schaefer (marianne.schaefer@gmail.com)
 *
 *         The validation class for Section A.6 Criteria for Readiness 1
 *         Evaluation
 *
 */
public class SectionA6Validator {

    private final DataRepository dataRepository;
    private final DiagnosticReadinessModule diagnosticReadinessModule;

    private final String SECTION_NAME = "Section A6 Validator";

    private final TableA6Validator tableA6Validator;

    public SectionA6Validator(DataRepository dataRepository) {
        this(dataRepository, new DiagnosticReadinessModule(), new TableA6Validator(dataRepository));
    }

    protected SectionA6Validator(DataRepository dataRepository,
            DiagnosticReadinessModule diagnosticReadinessModule,
            TableA6Validator tableA6Validator) {
        this.dataRepository = dataRepository;
        this.diagnosticReadinessModule = diagnosticReadinessModule;
        this.tableA6Validator = tableA6Validator;
    }

    /**
     * @param partNumber
     * @param stepNumber
     * @param outCome
     * @param message
     * @param listener
     */
    private void addOutcome(int partNumber, int stepNumber, Outcome outCome, String message,
            ResultsListener listener) {
        listener.addOutcome(partNumber, stepNumber, outCome, message);
    }

    /**
     * Helper method to evaluate the composite systems according to Secton A6
     * Step 2 a
     *
     * @param message
     * @param packet
     */
    public void setJ1939(J1939 j1939) {
        diagnosticReadinessModule.setJ1939(j1939);
    }

    public boolean verify(ResultsListener listener, int partNumber, int stepNumber) {

        boolean[] passed = { true };

        RequestResult<DM5DiagnosticReadinessPacket> response = diagnosticReadinessModule.requestDM5(listener, true);

        // 1. The response from each responding device shall be evaluated
        // separately using a through d below:
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        List<Integer> obdModuleAddresses2 = List.copyOf(obdModuleAddresses);
        List<Integer> packs = response.getPackets().stream().filter(packet -> packet.isObd())
                .map(packet -> packet.getSourceAddress()).collect(Collectors.toList());
        packs.addAll(response.getAcks().stream().map(packet -> packet.getSourceAddress()).collect(Collectors.toList()));
        obdModuleAddresses.removeAll(packs);
        if (!obdModuleAddresses.isEmpty()) {
            // a. Fail if no response from an OBD ECU (ECUs that indicate
            // 0x13, 0x14, 0x22, or 0x23 for OBD compliance).
            StringBuilder message1a = new StringBuilder(SECTION_NAME + NL);
            message1a.append(
                    " Step 1.a - Fail if no response from an OBD ECU (ECUs that indicate 0x13, 0x14, 0x22, or 0x23 for OBD compliance)");
            obdModuleAddresses.forEach(address -> message1a
                    .append(NL + "   ECU with source address :  " + address + " did not return a response"));

            addOutcome(partNumber, stepNumber, FAIL, message1a.toString(), listener);
            passed[0] = false;
        } else {
            String message = SECTION_NAME + NL + "Step 1.a - Pass";
            addOutcome(partNumber, stepNumber, PASS, message, listener);
        }

        response.getPackets().forEach(packet -> {
            // b. Fail if any response does not report supported and
            // complete for comprehensive components support and status (SPN
            // 1221, byte 4, bit 3 = 1 and bit 7 = 0), except when all the
            // bits in SPNs 1221, 1222, and 1223 are sent as 0 as defined in
            // SAE J1939-73 paragraph 5.7.5.
            Set<MonitoredSystem> monitoredSystems = packet.getMonitoredSystems();
            boolean isComplete = monitoredSystems.stream()
                    .filter(monitoredSys -> monitoredSys.getId() == CompositeSystem.COMPREHENSIVE_COMPONENT)
                    .anyMatch(system -> !system.getStatus().isEnabled() || !system.getStatus().isComplete());
            int[] bytes = packet.getPacket().getData(3, 7);
            int sum = 0;
            for (int val : bytes) {
                sum += val;
            }

            if (!isComplete && sum != 0) {
                StringBuilder message = new StringBuilder(SECTION_NAME + NL);
                message.append("Step 1.b - Fail if any response does not report supported and")
                        .append(NL)
                        .append(" complete for comprehensive components support and status (SPN")
                        .append(NL)
                        .append(" 1221, byte 4, bit 3 = 1 and bit 7 = 0), except when all the")
                        .append(NL)
                        .append(" bits in SPNs 1221, 1222, and 1223 are sent as 0 as defined in")
                        .append(NL)
                        .append(" SAE J1939-73 paragraph 5.7.5");
                addOutcome(partNumber, stepNumber, FAIL, message.toString(), listener);
                passed[0] = false;
            } else {
                String message = SECTION_NAME + NL + "Step 1.b - Pass";
                addOutcome(partNumber, stepNumber, PASS, message, listener);
            }

            // c. Fail if any response does not report 0 = ‘complete/not
            // supported’ for the status bit for every unsupported monitors
            // (i.e., any of the support bits in SPN 1221, byte 4 bits 1-3,
            // 1222 byte 5 bits 1-8, or 1222 byte 6 bits 1-5 that report 0
            // also report 0 in the corresponding status bit in SPN 1221 and
            // 1223).
            if (monitoredSystems.stream()
                    .anyMatch(system -> !system.getStatus().isEnabled() && !system.getStatus().isComplete())) {
                StringBuilder message = new StringBuilder(SECTION_NAME + NL);
                message.append(" Step 1.c - Fail if any response does not report 0 = ‘complete/not")
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
                addOutcome(partNumber, stepNumber, FAIL, message.toString(), listener);
                passed[0] = false;
            } else {
                String message = SECTION_NAME + NL + "Step 1.c - Pass";
                addOutcome(partNumber, stepNumber, PASS, message, listener);
            }

            // d. Fail if any response does not report 0 for reserved bits
            // (SPN 1221 byte 4 bits 4 and 8, SPN 1222 byte 6 bits 68, and
            // SPN1223 byte 8 bits 6-8).
            if ((packet.getPacket().getBytes()[3] & 0x88) != 0) {
                StringBuilder message = new StringBuilder(SECTION_NAME + NL);
                message.append(" Step 1.d - Fail if any response does not report 0 for reserved bits")
                        .append(NL)
                        .append("(SPN 1221 byte 4 bits 4 and 8, SPN 1222 byte 6 bits 6-8, and")
                        .append(NL)
                        .append("SPN1223 byte 8 bits 6-8)");
                addOutcome(partNumber, stepNumber, FAIL, message.toString(), listener);
                passed[0] = false;
            } else {
                String message = SECTION_NAME + NL + "Step 1.d - Pass";
                addOutcome(partNumber, stepNumber, PASS, message, listener);
            }
            // 2. All responses received from all responding OBD devices shall
            // be combined with appropriate ‘AND/OR’ logic to create a composite
            // vehicle readiness response: [Do not use responses from non-OBD
            // devices to create a composite vehicle readiness response.]

            // c. Fail if composite vehicle readiness does not meet any of the
            // criteria in Table A-6.
            if (!tableA6Validator.verify(listener, packet, partNumber, stepNumber)) {
                String failureMessage = SECTION_NAME + NL;
                failureMessage += " Step 2.c - Fail if composite vehicle readiness does not meet any of the criteria in Table A-6";
                listener.addOutcome(partNumber, stepNumber, FAIL, failureMessage);
                passed[0] = false;
            } else {
                String message = SECTION_NAME + NL + "Step 2.c - Pass";
                addOutcome(partNumber, stepNumber, PASS, message, listener);
            }

        });

        // d. Warn if any individual required monitor, except Continuous
        // Component Monitoring (CCM) is supported by more than one OBD ECU.
        List<CompositeSystem> supportedSystems = response.getPackets().stream()
                .filter(p -> {
                    return obdModuleAddresses2.contains(p.getSourceAddress());
                })
                .flatMap(p -> p.getMonitoredSystems().stream())
                .filter(s -> s.getId() != CompositeSystem.COMPREHENSIVE_COMPONENT)
                .filter(s -> s.getStatus().isEnabled())
                .map(s -> s.getId())
                .collect(Collectors.toList());
        // Convert to a set so there's only one of each system
        Set<CompositeSystem> systemsSet = new HashSet<>(supportedSystems);

        if (supportedSystems.size() != systemsSet.size()) {
            // Since the sizes aren't the same, the list contains a duplicate
            String warnMessage = SECTION_NAME + NL
                    + " Step 2.d Warn if any individual required monitor, except Continuous"
                    + NL + "  Component Monitoring (CCM) is supported by more than one OBD ECU";
            addOutcome(partNumber, stepNumber, WARN, warnMessage, listener);
        } else {
            String message = SECTION_NAME + NL + "Step 2.d - Pass";
            addOutcome(partNumber, stepNumber, PASS, message, listener);
        }

        // 3. All responses received from non-OBD ECU shall be evaluated
        // using the criteria below:
        List<DM5DiagnosticReadinessPacket> nonObdPackets = response.getPackets().stream()
                .filter(packet -> !packet.isObd()).collect(Collectors.toList());

        // a. Warn if any response from non-OBD ECU received.
        if (!nonObdPackets.isEmpty()) {
            // b. Warn if all the monitor status and support bits in any reply
            // from a non-OBD ECU are not all binary zeros or all binary ones.
            nonObdPackets.forEach(packet -> {
                byte[] bytes = packet.getPacket().getBytes();
                StringBuilder byteCheckMessage = new StringBuilder(SECTION_NAME + NL);
                boolean warn = false;
                if ((bytes[3] & 0x77) != 0 &&
                        (bytes[3] & 0x77) != 0x77) {
                    byteCheckMessage.append(" Step 3.b [byte 4] failed all binary zeros or all binary ones check" + NL);
                    warn = true;
                }
                if (bytes[4] != 0x00 && bytes[4] != 0xFF) {
                    byteCheckMessage.append(" Step 3.b [byte 5] failed all binary zeros or all binary ones check" + NL);
                    warn = true;
                }
                if ((bytes[5] & 0xE0) != 0 &&
                        (bytes[5] & 0xE0) != 0xE0) {
                    byteCheckMessage.append(" Step 3.b [byte 6] failed all binary zeros or all binary ones check" + NL);
                    warn = true;
                }
                if (bytes[6] != 0x00 && bytes[6] != 0xFF) {
                    byteCheckMessage.append(" Step 3.b [byte 7] failed all binary zeros or all binary ones check" + NL);
                    warn = true;
                }
                if ((bytes[7] & 0xE0) != 0 &&
                        (bytes[7] & 0xE0) != 0xE0) {
                    byteCheckMessage.append(" Step 3.b [byte 8] failed all binary zeros or all binary ones check" + NL);
                    warn = true;
                }
                if (warn) {
                    addOutcome(partNumber, stepNumber, WARN, byteCheckMessage.toString(), listener);
                }
            });
        } else {
            String message = SECTION_NAME + NL + "Step 3.b - Pass";
            addOutcome(partNumber, stepNumber, PASS, message, listener);
        }
        return passed[0];
    }

}