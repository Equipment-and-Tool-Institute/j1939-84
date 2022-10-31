/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;

/**
 * @author Marianne Schaefer (marianne.schaefer@gmail.com)
 *         <p>
 *         The validation class for Section A.6 Criteria for Readiness 1
 *         Evaluation
 */
public class SectionA6Validator {

    private final DataRepository dataRepository;

    private final TableA6Validator tableA6Validator;

    private final int partNumber;

    private final int stepNumber;

    public SectionA6Validator(DataRepository dataRepository, int partNumber, int stepNumber) {
        this(dataRepository, new TableA6Validator(dataRepository, partNumber, stepNumber), partNumber, stepNumber);
    }

    protected SectionA6Validator(DataRepository dataRepository,
                                 TableA6Validator tableA6Validator,
                                 int partNumber,
                                 int stepNumber) {
        this.dataRepository = dataRepository;
        this.tableA6Validator = tableA6Validator;
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
    }

    public void verify(ResultsListener listener,
                       String section,
                       RequestResult<DM5DiagnosticReadinessPacket> response,
                       boolean engineHasRun) {

        // A6.1. The response from each responding device shall be evaluated
        // separately using a through d below:

        // A6.1.a. Fail if no response from an OBD ECU (ECUs that indicate 0x13, 0x14, 0x22, or 0x23 for OBD compliance)
        List<Integer> addresses = new ArrayList<>(dataRepository.getObdModuleAddresses());
        response.toPacketStream().map(ParsedPacket::getSourceAddress).forEach(addresses::remove);
        addresses.stream()
                 .distinct()
                 .sorted()
                 .map(Lookup::getAddressName)
                 .map(moduleName -> section + " (A6.1.a) - OBD ECU " + moduleName
                         + " did not provide a response to Global query")
                 .forEach(msg -> {
                     listener.addOutcome(partNumber, stepNumber, FAIL, msg);
                 });

        var obdPackets = response.toPacketStream()
                                 .filter(p -> dataRepository.isObdModule(p.getSourceAddress()))
                                 .collect(Collectors.toList());

        // A6.1.b. Fail if any response does not report supported and complete for
        // comprehensive components support and status
        // (SPN 1221, byte 4, bit 3 = 1 and bit 7 = 0),
        // except when all the bits in SPNs 1221, 1222, and 1223 are sent as 0
        // as defined in SAE J1939-73 paragraph 5.7.5.
        obdPackets.stream()
                  .filter(p -> !isZeros(p))
                  .filter(p -> {
                      return p.getMonitoredSystems()
                              .stream()
                              .filter(s -> s.getId() == CompositeSystem.COMPREHENSIVE_COMPONENT)
                              .findFirst()
                              .stream()
                              .noneMatch(system -> system.getStatus().isEnabled() && system.getStatus().isComplete());
                  })
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      listener.addOutcome(partNumber,
                                          stepNumber,
                                          FAIL,
                                          section + " (A6.1.b) - " + moduleName
                                                  + " did not report supported and complete for comprehensive components support and status");
                  });

        // A6.1.c. Fail if any response does not report 0 = ‘complete/not supported’ for the status bit
        // for every unsupported monitors
        // (i.e., any of the support bits in SPN 1221, byte 4 bits 1-3,
        // 1222 byte 5 bits 1-8, or 1222 byte 6 bits 1-5 that report 0
        // also report 0 in the corresponding status bit in SPN 1221 and 1223).
        for (DM5DiagnosticReadinessPacket packet : obdPackets) {
            packet.getMonitoredSystems()
                  .stream()
                  .filter(s -> !s.getStatus().isEnabled())
                  .filter(s -> !s.getStatus().isComplete())
                  .map(MonitoredSystem::getName)
                  .map(String::trim)
                  .forEach(monitorName -> {
                      listener.addOutcome(partNumber,
                                          stepNumber,
                                          FAIL,
                                          section + " (A6.1.c) - " + packet.getModuleName()
                                                  + " did not 'complete/not supported' for the unsupported monitor "
                                                  + monitorName);
                  });
        }

        // A6.1.d. Fail if any response does not report 0 for reserved bits
        // (SPN 1221 byte 4 bits 4 and 8,
        // SPN 1222 byte 6 bits 6-8, and
        // SPN 1223 byte 8 bits 6-8).
        obdPackets.stream()
                  .filter(p -> {
                      var spn1221Ok = (p.getPacket().get(3) & 0x88) == 0;
                      var spn1222Ok = (p.getPacket().get(5) & 0xE0) == 0;
                      var spn1223Ok = (p.getPacket().get(7) & 0xE0) == 0;
                      return !(spn1221Ok && spn1222Ok && spn1223Ok);
                  })
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      listener.addOutcome(partNumber,
                                          stepNumber,
                                          FAIL,
                                          section + " (A6.1.d) - " + moduleName
                                                  + " did not report 0 for reserved bits");
                  });

        // A6.2. All responses received from all responding OBD devices shall
        // be combined with appropriate ‘AND/OR’ logic to create a composite
        // vehicle readiness response: [Do not use responses from non-OBD
        // devices to create a composite vehicle readiness response.]

        // A6.2.a. If one or more responses indicates 1 = supported in the support bit for a monitor,
        // A6.2.a.i. Then the composite vehicle readiness shall indicate 1 = supported for that support bit/monitor,
        // A6.2.a.ii. Else it shall indicate 0 = unsupported for that support bit/monitor;
        // A6.2.b. If one or more responses indicates the status bit for a supported monitor is 1 = not complete,
        // A6.2.b.i. Then the composite vehicle readiness shall indicate 1 = not complete for that status bit/monitor,
        // A6.2..b.ii. Else it shall indicate 0 = complete for that status bit/monitor).
        var compositeSystems = CommunicationsModule.getCompositeSystems(obdPackets, true);

        // A6.2.c. Fail if composite vehicle readiness does not meet any of the criteria in Table A-6.
        tableA6Validator.verify(listener, compositeSystems, section + " (A6.2.c)", engineHasRun);

        // A6.2.d. Warn if any individual required monitor, except Continuous Component Monitoring (CCM)
        // is supported by more than one OBD ECU.
        List<CompositeSystem> supportedSystems = obdPackets.stream()
                                                           .flatMap(p -> p.getMonitoredSystems().stream())
                                                           .filter(s -> s.getId() != CompositeSystem.COMPREHENSIVE_COMPONENT)
                                                           .filter(s -> s.getStatus().isEnabled())
                                                           .map(MonitoredSystem::getId)
                                                           .collect(Collectors.toList());

        for (CompositeSystem system : CompositeSystem.values()) {
            int freq = Collections.frequency(supportedSystems, system);
            if (freq > 1) {
                String warnMessage = section + " (A6.2.d) - " + system.getName().trim()
                        + " is supported by more than one OBD ECU";
                listener.addOutcome(partNumber, stepNumber, WARN, warnMessage);
            }
        }

        // A6.3. All responses received from non-OBD ECU shall be evaluated using the criteria below:
        var nonObdPackets = response.getPackets()
                                    .stream()
                                    .filter(packet -> !packet.isObd())
                                    .collect(Collectors.toList());

        // A6.3.a. Warn if any response from non-OBD ECU received.
        nonObdPackets.stream()
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         String warnMessage = section + " (A6.3.a) - Non-OBD ECU " + moduleName + " responded";
                         listener.addOutcome(partNumber, stepNumber, WARN, warnMessage);
                     });

        // A6.3.b. Warn if all the monitor status and support bits in any reply
        // from a non-OBD ECU are not all binary zeros or all binary ones.
        nonObdPackets.stream()
                     .filter(packet -> {
                         byte[] bytes = packet.getPacket().getBytes();

                         var allZeros = ((bytes[3] & 0x77) == 0) && (bytes[4] == 0) && ((bytes[5] & 0xE0) == 0)
                                 && (bytes[6] == 0) && ((bytes[7] & 0xE0) == 0);

                         var allOnes = ((bytes[3] & 0x77) == 0x77) && (bytes[4] == (byte) 0xFF)
                                 && ((bytes[5] & 0xE0) == 0xE0) && (bytes[6] == (byte) 0xFF)
                                 && ((bytes[7] & 0xE0) == 0xE0);
                         return !allZeros && !allOnes;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         String msg = "All the monitor status and support bits from " + moduleName
                                 + " are not all binary zeros or all binary ones";
                         listener.addOutcome(partNumber, stepNumber, WARN, msg);
                     });
    }

    private boolean isZeros(DM5DiagnosticReadinessPacket packet) {
        return Arrays.stream(packet.getPacket().getData(3, 7)).sum() == 0;
    }

}
