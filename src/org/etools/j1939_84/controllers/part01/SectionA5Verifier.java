/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INCOMPLETE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class SectionA5Verifier {
    private final DataRepository dataRepository;
    private final DiagnosticMessageModule diagnosticMessageModule;
    private final VehicleInformationModule vehicleInformationModule;
    private final int partNumber;
    private final int stepNumber;

    public SectionA5Verifier(int partNumber, int stepNumber) {
        this(DataRepository.getInstance(),
             new DiagnosticMessageModule(),
             new VehicleInformationModule(),
             partNumber,
             stepNumber);
    }

    protected SectionA5Verifier(DataRepository dataRepository,
                                DiagnosticMessageModule diagnosticMessageModule,
                                VehicleInformationModule vehicleInformationModule,
                                int partNumber,
                                int stepNumber) {
        this.dataRepository = dataRepository;
        this.diagnosticMessageModule = diagnosticMessageModule;
        this.vehicleInformationModule = vehicleInformationModule;
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
    }

    private static boolean isInvalid(int value) {
        return (value != 0xFFFF && value != 0xFB00 && value != 0x0000);
    }

    public void setJ1939(J1939 j1939) {
        diagnosticMessageModule.setJ1939(j1939);
        vehicleInformationModule.setJ1939(j1939);
    }

    public void verifyDataNotErased(ResultsListener listener, String section) {
        if (true) {
            listener.addOutcome(partNumber,
                                stepNumber,
                                INCOMPLETE,
                                section + " - hasn't finished implementing Section A.5 verifyDataNotErased");
            return;
        }

        // 1.a. DM6 pending shall report no DTCs and MIL off and not flashing
        verifyDTCsNotErased(listener, section, DM6PendingEmissionDTCPacket.class);
        verifyDTCsNotErased(listener, section, DM12MILOnEmissionDTCPacket.class);
        verifyDTCsNotErased(listener, section, DM23PreviouslyMILOnEmissionDTCPacket.class);
        verifyDM29NotErased(listener, section);
        verifyDM5NotErased(listener, section);
        verifyDM25NotErased(listener, section);
        verifyDM31NotErased(listener, section);
    }

    public void verifyDataNotMixedErased(ResultsListener listener, String section) {
        listener.addOutcome(partNumber,
                            stepNumber,
                            INCOMPLETE,
                            section + " - hasn't finished implementing Section A.5 verifyDataNotMixedErased");
    }

    public void verifyDataNotPartialErased(ResultsListener listener, String section) {
        listener.addOutcome(partNumber,
                            stepNumber,
                            INCOMPLETE,
                            section + " - hasn't finished implementing Section A.5 verifyDataNotPartialErased");
    }

    public void verifyDataErased(ResultsListener listener, String section) {
        if (true) {
            listener.addOutcome(partNumber,
                                stepNumber,
                                INCOMPLETE,
                                section + " - hasn't finished implementing Section A.5 verifyDataErased");
            return;
        }

        verifyDTCsErased(listener, section, DM6PendingEmissionDTCPacket.class);
        verifyDTCsErased(listener, section, DM12MILOnEmissionDTCPacket.class);
        verifyDTCsErased(listener, section, DM23PreviouslyMILOnEmissionDTCPacket.class);
        verifyDM29Erased(listener, section);
        verifyDM5Erased(listener, section);
        verifyDM25Erased(listener, section);
        verifyDM31Erased(listener, section);
    }

    public void verify(List<DM28PermanentEmissionDTCPacket> previousDM28Packets,
                       List<DM20MonitorPerformanceRatioPacket> previousDM20Packets,
                       List<DM33EmissionIncreasingAECDActiveTime> previousDM33Packets,
                       List<EngineHoursPacket> previousEngineHoursPackets,
                       ResultsListener listener,
                       String section) {

        verifyDM21(listener);
        verifyDM26(listener);
        verifyDM7DM30(listener);
        verifyDM20(previousDM20Packets, listener);
        verifyDM28(previousDM28Packets, listener);
        verifyDM33(previousDM33Packets, listener);
        verifyEngineHours(previousEngineHoursPackets, listener);
    }

    public boolean verifyDM20(List<DM20MonitorPerformanceRatioPacket> previousDM20Packets, ResultsListener listener) {
        // 7. Monitor Performance data
        // a. DM20 Monitor Performance Ratio data shall not be reset/shall stay
        // the same as it was before code clear for all values including the
        // number of ignition cycles, general denominators, monitor specific
        // numerators, and monitor specific denominators.
        List<DM20MonitorPerformanceRatioPacket> dm20Packets = diagnosticMessageModule.requestDM20(listener)
                                                                                     .getPackets();
        if (!dm20Packets.equals(previousDM20Packets)) {
            StringBuilder failMessage = new StringBuilder(
                                                          "Section A.5 verification failed during DM20 check done at table step 7.a");
            failMessage.append(NL)
                       .append("Previous Monitor Performance Ratio (DM20):")
                       .append(NL);
            previousDM20Packets.forEach(p -> failMessage.append(p.toString()).append(NL));
            failMessage.append("Post Monitor Performance Ratio (DM20):")
                       .append(NL);
            dm20Packets.forEach(p -> failMessage.append(p.toString()).append(NL));
            listener.onProgress(failMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 7.a DM20 Verification");
        }
        return true;
    }

    public boolean verifyDM21(ResultsListener listener) {
        // b. DM21 diagnostic readiness 2 shall report 0 for distance with MIL
        // on and minutes run with MIL on
        // 5.b. DM21 diagnostic readiness 2 shall report 0 for distance since
        // code clear and minutes run since code clear
        List<DM21DiagnosticReadinessPacket> dm21Packets = diagnosticMessageModule.requestDM21(listener)
                                                                                 .getPackets()
                                                                                 .stream()
                                                                                 .filter(packet -> packet.getKmWhileMILIsActivated() != 0
                                                                                         ||
                                                                                         packet.getMinutesWhileMILIsActivated() != 0
                                                                                         ||
                                                                                         packet.getKmSinceDTCsCleared() != 0
                                                                                         ||
                                                                                         packet.getMinutesSinceDTCsCleared() != 0)
                                                                                 .collect(Collectors.toList());

        if (!dm21Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                                                             "Section A.5 verification failed during DM21 check done at table step 3.b & 5.b");
            failureMessage.append(NL).append("Modules with source address ");
            dm21Packets.forEach(packet -> failureMessage.append(packet.getSourceAddress())
                                                        .append(", reported :")
                                                        .append(NL)
                                                        .append(packet.getKmWhileMILIsActivated())
                                                        .append(" km(s) for distance with the MIL on")
                                                        .append(NL)
                                                        .append(packet.getMinutesWhileMILIsActivated())
                                                        .append(" minute(s) run with the MIL on")
                                                        .append(NL)
                                                        .append(packet.getMinutesWhileMILIsActivated())
                                                        .append(" minute(s) while MIL is activated")
                                                        .append(NL)
                                                        .append(packet.getKmSinceDTCsCleared())
                                                        .append(" km(s) since DTC code clear sent")
                                                        .append(NL)
                                                        .append(packet.getMinutesSinceDTCsCleared())
                                                        .append(" minute(s) since the DTC code clear sent"));
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 3.b & 5.b DM21 Verification");
        }
        return true;
    }

    public void verifyDM25Erased(ResultsListener listener, String section) {
        // 2.a. DM25 expanded freeze frame shall report no data and DTC causing
        // freeze frame with bytes 1-5 = 0 and bytes 6-8 = 255
        dataRepository.getObdModuleAddresses()
                      .stream()
                      .map(address -> diagnosticMessageModule.requestDM25(listener, address))
                      .map(BusResult::getPacket)
                      .flatMap(Optional::stream)
                      .map(e -> e.left)
                      .flatMap(Optional::stream)
                      .filter(packet -> {
                          byte[] bytes = packet.getPacket().getBytes();
                          return bytes[0] != 0x00
                                  || bytes[1] != 0x00
                                  || bytes[2] != 0x00
                                  || bytes[3] != 0x00
                                  || bytes[4] != 0x00
                                  || bytes[5] != (byte) 0xFF
                                  || bytes[6] != (byte) 0xFF
                                  || bytes[7] != (byte) 0xFF;
                      })
                      .forEach(p -> {
                          addNotErasedFailure(listener, section, p);
                      });
    }

    public void verifyDM25NotErased(ResultsListener listener, String section) {
        dataRepository.getObdModuleAddresses()
                      .stream()
                      .map(address -> diagnosticMessageModule.requestDM25(listener, address))
                      .map(BusResult::getPacket)
                      .flatMap(Optional::stream)
                      .map(e -> e.left)
                      .flatMap(Optional::stream)
                      .filter(p -> !p.equals(getRepoPacket(p)))
                      .forEach(p -> {
                          addNotErasedFailure(listener, section, p);
                      });
    }

    public boolean verifyDM26(ResultsListener listener) {
        // 5. Activity since code clear
        // a. DM26 diagnostic readiness 3 shall report 0 for number of warm-ups
        // since code clear
        List<DM26TripDiagnosticReadinessPacket> dm26Packets = diagnosticMessageModule.requestDM26(listener)
                                                                                     .getPackets()
                                                                                     .stream()
                                                                                     .filter(Objects::nonNull)
                                                                                     .filter(packet -> packet.getWarmUpsSinceClear() != 0)
                                                                                     .collect(Collectors.toList());
        if (!dm26Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                                                             "Section A.5 verification failed during DM26 check done at table step 5.a");
            failureMessage.append(NL).append("Modules with source address ");
            dm26Packets.forEach(packet -> failureMessage.append(packet.getSourceAddress())
                                                        .append(", reported ")
                                                        .append(packet.getWarmUpsSinceClear())
                                                        .append(" warm-ups since code clear"));
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 5.a DM26 Verification");
        }
        return true;
    }

    public boolean verifyDM28(List<DM28PermanentEmissionDTCPacket> previousDM28Packets, ResultsListener listener) {
        // 8. Permanent DTCs
        // a. DM28 Permanent DTCs shall not be erased/still report any permanent
        // DTC that was present before code clear.
        List<DM28PermanentEmissionDTCPacket> dm28Packets = diagnosticMessageModule.requestDM28(listener)
                                                                                  .getPackets()
                                                                                  .stream()
                                                                                  .filter(t -> t.getDtcs().size() != 0)
                                                                                  .collect(Collectors.toList());
        // Since we only care about packets that have dtc, let make sure both
        // both lists accurately reflect that.
        if (!previousDM28Packets.stream()
                                .filter(packet -> packet.getDtcs().size() != 0)
                                .collect(Collectors.toList())
                                .equals(dm28Packets)) {
            StringBuilder failMessage = new StringBuilder(
                                                          "Section A.5 verification failed during DM28 check done at table step 8.a");
            failMessage.append(NL)
                       .append(
                               "Pre DTC all clear code sent retrieved the DM28 packet :")
                       .append(NL);
            previousDM28Packets.forEach(packet -> failMessage.append(packet.toString()));
            failMessage.append(NL);
            failMessage.append("Post DTC all clear code sent retrieved the DM28 packet :");
            failMessage.append(NL);
            dm28Packets.forEach(packet -> failMessage.append(packet.toString()));
            listener.onProgress(failMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 8.a DM28 Verification");
        }
        return true;
    }

    private void verifyDM29Erased(ResultsListener listener, String section) {
        // d. DM29 shall report zero for number of pending, active, and previously active DTCs
        diagnosticMessageModule.requestDM29(listener)
                               .getPackets()
                               .stream()
                               .filter(p -> p.getEmissionRelatedMILOnDTCCount() != 0
                                       || p.getEmissionRelatedPendingDTCCount() != 0
                                       || p.getEmissionRelatedPreviouslyMILOnDTCCount() != 0)
                               .forEach(p -> {
                                   addNotErasedFailure(listener, section, p);
                               });
    }

    private void verifyDM29NotErased(ResultsListener listener, String section) {
        diagnosticMessageModule.requestDM29(listener)
                               .getPackets()
                               .stream()
                               .filter(p -> !p.equals(getRepoPacket(p)))
                               .forEach(p -> {
                                   addErasedFailure(listener, section, p);
                               });
    }

    public void verifyDM31Erased(ResultsListener listener, String section) {
        // 3.a. DM31 lamp status shall report no DTCs causing MIL on
        diagnosticMessageModule.requestDM31(listener)
                               .getPackets()
                               .stream()
                               .filter(p -> {
                                   return p.getDtcLampStatuses()
                                           .stream()
                                           .map(DTCLampStatus::getMalfunctionIndicatorLampStatus)
                                           .anyMatch(mil -> mil != OFF && mil != NOT_SUPPORTED);
                               })
                               .forEach(p -> {
                                   addNotErasedFailure(listener, section, p);
                               });
    }

    public void verifyDM31NotErased(ResultsListener listener, String section) {
        diagnosticMessageModule.requestDM31(listener)
                               .getPackets()
                               .stream()
                               .filter(p -> !p.equals(getRepoPacket(p)))
                               .forEach(p -> {
                                   addErasedFailure(listener, section, p);
                               });
    }

    public boolean verifyDM33(List<DM33EmissionIncreasingAECDActiveTime> previousDM33Packets,
                              ResultsListener listener) {
        // 9. Engine runtime information
        // a. DM33 EI-AECD information shall not be reset/cleared for any
        // non-zero values present before code clear.
        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>();
        dataRepository.getObdModuleAddresses()
                      .forEach(address -> dm33Packets.addAll(new ArrayList<>(diagnosticMessageModule.requestDM33(listener,
                                                                                                                 address)
                                                                                                    .getPackets())));

        previousDM33Packets.sort(Comparator.comparingInt(ParsedPacket::getSourceAddress));
        dm33Packets.sort(Comparator.comparingInt(ParsedPacket::getSourceAddress));

        if (!previousDM33Packets.equals(dm33Packets)) {
            StringBuilder failMessage = new StringBuilder(
                                                          "Section A.5 verification failed during DM33 check done at table step 9.a");
            failMessage.append(NL)
                       .append("Pre DTC all clear code sent retrieved the DM33 packet :");
            previousDM33Packets.forEach(packet -> failMessage.append(NL).append("   ").append(packet.toString()));
            failMessage.append(NL)
                       .append("Post DTC all clear code sent retrieved the DM33 packet :");
            dm33Packets.forEach(packet -> failMessage.append(NL).append("   ").append(packet.toString()));
            listener.onProgress(failMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 9.a DM33 Verification");
        }
        return true;
    }

    private void verifyDM5Erased(ResultsListener listener, String section) {
        // e. DM5 shall report zero for number of active and previously active DTCs.
        diagnosticMessageModule.requestDM5(listener)
                               .getPackets()
                               .stream()
                               .filter(p -> (p.getActiveCodeCount() != 0)
                                       || (p.getPreviouslyActiveCodeCount() != 0))
                               .forEach(p -> {
                                   addNotErasedFailure(listener, section, p);
                               });
        // TODO Check monitors
    }

    private void verifyDM5NotErased(ResultsListener listener, String section) {
        diagnosticMessageModule.requestDM5(listener)
                               .getPackets()
                               .stream()
                               .filter(p -> !p.equals(getRepoPacket(p)))
                               .forEach(p -> {
                                   addNotErasedFailure(listener, section, p);
                               });
    }

    public boolean verifyDM7DM30(ResultsListener listener) {
        // 6. Test results
        // a. DM7/DM30 Test Results shall report all test results with
        // initialized results and limits (all 0x00 or 0xFB00 for results and
        // 0xFFFF for limits)
        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        dataRepository.getObdModuleAddresses().forEach(address -> {
            for (SupportedSPN supportedSPN : dataRepository.getObdModule(address).getTestResultSPNs()) {
                dm30Packets.addAll(
                                   diagnosticMessageModule.requestTestResults(listener,
                                                                              address,
                                                                              247,
                                                                              supportedSPN.getSpn(),
                                                                              31)
                                                          .stream()
                                                          .filter(packet -> packet.getTestResults().size() != 0)
                                                          .filter(p -> {
                                                              for (ScaledTestResult result : p.getTestResults()) {
                                                                  if (isInvalid(result.getTestMaximum())
                                                                          || isInvalid(result.getTestValue())
                                                                          || isInvalid(result.getTestMinimum())) {
                                                                      return true;
                                                                  }
                                                              }
                                                              return false;
                                                          })
                                                          .collect(Collectors.toList()));
            }
        });
        if (!dm30Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                                                             "Section A.5 verification failed during DM7/DM30 check done at table step 6.a");
            failureMessage.append(NL).append("DM30 Scaled Test Results for");
            dm30Packets.forEach(packet -> {
                failureMessage.append(NL)
                              .append("source address ")
                              .append(packet.getSourceAddress())
                              .append(" are : [")
                              .append(NL);
                for (ScaledTestResult testResult : packet.getTestResults()) {
                    int testMaximum = testResult.getTestMaximum();
                    if (isInvalid(testMaximum)) {
                        failureMessage.append("  TestMaximum failed and the value returned was : ")
                                      .append(testMaximum)
                                      .append(NL);
                    }
                    int testValue = testResult.getTestValue();
                    if (isInvalid(testValue)) {
                        failureMessage.append("  TestResult failed and the value returned was : ")
                                      .append(testResult.getTestValue())
                                      .append(NL);
                    }
                    int testMinimum = testResult.getTestMinimum();
                    if (isInvalid(testMinimum)) {
                        failureMessage.append("  TestMinimum failed and the value returned was : ")
                                      .append(testResult.getTestMinimum())
                                      .append(NL);
                    }
                    failureMessage.append("]");
                }
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 6.a DM7/DM30 Verification");
        }
        return true;
    }

    public boolean verifyEngineHours(List<EngineHoursPacket> previousEngineHoursPackets, ResultsListener listener) {
        // b. Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle
        // time (PGN 65244 (SPN 235)) shall not be reset/cleared for any
        // non-zero values present before code clear
        List<EngineHoursPacket> engineHourPackets = new ArrayList<>(vehicleInformationModule.requestEngineHours(listener)
                                                                                            .getPackets());
        if (!engineHourPackets.equals(previousEngineHoursPackets)) {
            StringBuilder failMessage = new StringBuilder(
                                                          "Section A.5 verification failed Cumulative engine runtime (PGN 65253 (SPN 247))");
            failMessage.append(NL)
                       .append(" and engine idletime (PGN 65244 (SPN 235)) shall not be reset/cleared for any")
                       .append(NL)
                       .append(" non-zero values present before code clear check done at table step 9.b")
                       .append(NL)
                       .append("Previous packet(s) was/were:")
                       .append(NL);
            previousEngineHoursPackets.forEach(packet -> failMessage.append("   ")
                                                                    .append(packet.toString())
                                                                    .append(NL));
            if (previousEngineHoursPackets.isEmpty()) {
                failMessage.append("   EMPTY")
                           .append(NL);
            }
            failMessage.append("Current packet(s) was/were:")
                       .append(NL);
            engineHourPackets.forEach(packet -> failMessage.append("   ")
                                                           .append(packet.toString())
                                                           .append(NL));
            if (engineHourPackets.isEmpty()) {
                failMessage.append("   EMPTY")
                           .append(NL);
            }
            listener.onProgress(failMessage.toString());
            return false;
        } else {
            listener.onProgress(
                                "PASS: Section A.5 Step 9.b Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle time (PGN 65244 (SPN 235)) Verification");
        }
        return true;
    }

    private void addNotErasedFailure(ResultsListener listener, String section, GenericPacket packet) {
        addFailure(listener, section + " - " + packet.getModuleName() + " did not erase " + packet.getName() + " data");
    }

    private void addErasedFailure(ResultsListener listener, String section, GenericPacket packet) {
        addFailure(listener, section + " - " + packet.getModuleName() + " erased " + packet.getName() + " data");
    }

    private void addFailure(ResultsListener listener, String message) {
        listener.addOutcome(partNumber, stepNumber, FAIL, message);
    }

    private void verifyDTCsErased(ResultsListener listener,
                                  String section,
                                  Class<? extends DiagnosticTroubleCodePacket> clazz) {
        requestDTCs(listener, clazz)
                                    .getPackets()
                                    .stream()
                                    .filter(p -> !p.getDtcs().isEmpty() || p.getMalfunctionIndicatorLampStatus() != OFF)
                                    .forEach(p -> {
                                        addNotErasedFailure(listener, section, p);
                                    });
    }

    private void verifyDTCsNotErased(ResultsListener listener,
                                     String section,
                                     Class<? extends DiagnosticTroubleCodePacket> clazz) {
        requestDTCs(listener, clazz)
                                    .getPackets()
                                    .stream()
                                    .filter(p -> {
                                        int address = p.getSourceAddress();
                                        boolean dtcCleared = p.getDtcs().isEmpty()
                                                && !getDTCs(clazz, address).isEmpty();
                                        boolean milCleared = p.getMalfunctionIndicatorLampStatus() == OFF
                                                && getMIL(clazz, address) != OFF;
                                        return dtcCleared || milCleared;
                                    })
                                    .forEach(p -> {
                                        addErasedFailure(listener, section, p);
                                    });
    }

    private RequestResult<? extends DiagnosticTroubleCodePacket>
            requestDTCs(ResultsListener listener, Class<? extends DiagnosticTroubleCodePacket> clazz) {
        if (clazz == DM6PendingEmissionDTCPacket.class) {
            return diagnosticMessageModule.requestDM6(listener);
        }
        return RequestResult.empty();
    }

    private List<DiagnosticTroubleCode> getDTCs(Class<? extends DiagnosticTroubleCodePacket> clazz, int address) {
        OBDModuleInformation obdModuleInformation = dataRepository.getObdModule(address);
        if (obdModuleInformation != null) {
            var packet = obdModuleInformation.getLatest(clazz);
            if (packet != null) {
                return packet.getDtcs();
            }
        }
        return List.of();
    }

    private LampStatus getMIL(Class<? extends DiagnosticTroubleCodePacket> clazz, int address) {
        OBDModuleInformation obdModuleInformation = dataRepository.getObdModule(address);
        if (obdModuleInformation != null) {
            var packet = obdModuleInformation.getLatest(clazz);
            if (packet != null) {
                return packet.getMalfunctionIndicatorLampStatus();
            }
        }
        return null;
    }

    private GenericPacket getRepoPacket(GenericPacket packet) {
        OBDModuleInformation info = dataRepository.getObdModule(packet.getSourceAddress());
        return info == null ? null : info.getLatest(packet.getClass());
    }

}
