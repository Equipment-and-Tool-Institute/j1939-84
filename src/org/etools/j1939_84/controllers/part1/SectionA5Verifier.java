/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class SectionA5Verifier {
    private static boolean isValid(int value) {
        return (value == 0xFFFF || value == 0xFB00 || value == 0x0000);
    }

    private final DataRepository dataRepository;
    private final DiagnosticReadinessModule diagnosticReadinessModule;
    private final DTCModule dtcModule;
    private final OBDTestsModule obdTestsModule;

    private final VehicleInformationModule vehicleInformationModule;

    public SectionA5Verifier(DataRepository dataRepository) {
        this(dataRepository, new DiagnosticReadinessModule(), new DTCModule(), new OBDTestsModule(),
                new VehicleInformationModule());
    }

    protected SectionA5Verifier(DataRepository dataRepository,
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

    public void setJ1939(J1939 j1939) {
        diagnosticReadinessModule.setJ1939(j1939);
        dtcModule.setJ1939(j1939);
        obdTestsModule.setJ1939(j1939);
        vehicleInformationModule.setJ1939(j1939);
    }

    public boolean verify(List<DM28PermanentEmissionDTCPacket> previousDM28Packets,
            List<DM20MonitorPerformanceRatioPacket> previousDM20Packets,
            List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> previousDM33Packets,
            List<EngineHoursPacket> previousEngineHoursPackets,
            ResultsListener listener) {

        boolean passed = true;

        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses().stream()
                .collect(Collectors.toList());

        if (!verifyDM6(listener)) {
            passed = false;
        }

        if (!verifyDM12(listener)) {
            passed = false;
        }

        if (!verifyDM23(listener)) {
            passed = false;
        }

        if (!verifyDM29(listener)) {
            passed = false;
        }

        if (!verifyDM25(listener, obdModuleAddresses)) {
            passed = false;
        }

        if (!verifyDM31(listener)) {
            passed = false;
        }

        if (!verifyDM21(listener)) {
            passed = false;
        }

        if (!verifyDM5(listener)) {
            passed = false;
        }

        if (!verifyDM26(listener)) {
            passed = false;
        }

        if (!verifyDM7DM30(listener, obdModuleAddresses)) {
            passed = false;
        }

        if (!verifyDM20(previousDM20Packets, listener)) {
            passed = false;
        }

        if (!verifyDM28(previousDM28Packets, listener)) {
            passed = false;
        }

        if (!verifyDM33(previousDM33Packets, listener, obdModuleAddresses)) {
            passed = false;
        }

        if (!verifyEngineHours(previousEngineHoursPackets, listener)) {
            passed = false;
        }
        return passed;
    }

    /**
     * @param listener
     *
     * @return boolean representing passed state
     */
    public boolean verifyDM12(ResultsListener listener) {
        // b. DM12 active shall report no DTCs and MIL off and not flashing
        List<DM12MILOnEmissionDTCPacket> dm12Packets = dtcModule.requestDM12(listener, true)
                .getPackets()
                .stream()
                .map(p -> p)
                .filter(t -> (!t.getDtcs().isEmpty()) ||
                        (t.getMalfunctionIndicatorLampStatus() != LampStatus.OFF))
                .collect(Collectors.toList());

        if (!dm12Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM12 check done at table step 1.b");
            failureMessage.append(NL).append("Modules with source address ");
            dm12Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", reported ")
                        .append(packet.getDtcs().size()).append(" DTCs.").append(NL).append("MIL status is : ")
                        .append(packet.getMalfunctionIndicatorLampStatus()).append(".");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 1.b DM12 Verification");
        }
        return true;
    }

    /**
     * @param previousDM20Packets
     * @param listener
     */
    public boolean verifyDM20(List<DM20MonitorPerformanceRatioPacket> previousDM20Packets, ResultsListener listener) {
        // 7. Monitor Performance data
        // a. DM20 Monitor Performance Ratio data shall not be reset/shall stay
        // the same as it was before code clear for all values including the
        // number of ignition cycles, general denominators, monitor specific
        // numerators, and monitor specific denominators.
        List<DM20MonitorPerformanceRatioPacket> dm20Packets = diagnosticReadinessModule
                .requestDM20(listener, true)
                .getPackets()
                .stream()
                .map(p -> p)
                .collect(Collectors.toList());
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

    /**
     * @param listener
     */
    public boolean verifyDM21(ResultsListener listener) {
        // b. DM21 diagnostic readiness 2 shall report 0 for distance with MIL
        // on and minutes run with MIL on
        // 5.b. DM21 diagnostic readiness 2 shall report 0 for distance since
        // code clear and minutes run since code clear
        List<DM21DiagnosticReadinessPacket> dm21Packets = dtcModule.requestDM21(listener).getPackets()
                .stream()
                .map(p -> p)
                .filter(packet -> packet.getKmWhileMILIsActivated() != 0 ||
                        packet.getMinutesWhileMILIsActivated() != 0 ||
                        packet.getKmSinceDTCsCleared() != 0 ||
                        packet.getMinutesSinceDTCsCleared() != 0)
                .collect(Collectors.toList());

        if (!dm21Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM21 check done at table step 3.b & 5.b");
            failureMessage.append(NL).append("Modules with source address ");
            dm21Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress())
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
                        .append(" minute(s) since the DTC code clear sent");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 3.b & 5.b DM21 Verification");
        }
        return true;
    }

    /**
     * @param listener
     */
    public boolean verifyDM23(ResultsListener listener) {
        // c. DM23 previously active shall report no DTCs and MIL off and not
        // flashing
        List<DM23PreviouslyMILOnEmissionDTCPacket> dm23Packets = dtcModule.requestDM23(listener, true).getPackets()
                .stream()
                .filter(t -> !t.getDtcs().isEmpty() ||
                        t.getMalfunctionIndicatorLampStatus() != LampStatus.OFF)
                .collect(Collectors.toList());
        if (!dm23Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM23 check done at table step 1.c");
            dm23Packets.forEach(packet -> {
                failureMessage.append(NL).append("Module with source address ")
                        .append(packet.getSourceAddress() + ", reported ")
                        .append(packet.getDtcs().size()).append(" DTCs.").append(NL).append("MIL status is : ")
                        .append(packet.getMalfunctionIndicatorLampStatus()).append(".");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 1.c DM23 Verification");
        }
        return true;
    }

    /**
     * @param listener
     * @param obdModuleAddresses
     */
    public boolean verifyDM25(ResultsListener listener, List<Integer> obdModuleAddresses) {
        // 2. Freeze Frame information
        // a. DM25 expanded freeze frame shall report no data and DTC causing
        // freeze frame with bytes 1-5 = 0 and bytes 6-8 = 255 (**Remember array
        // are zero based**)
        List<DM25ExpandedFreezeFrame> dm25Packets = obdModuleAddresses.stream()
                // convert address to DM25
                .flatMap(address -> dtcModule.requestDM25(listener, address).getPacket().stream())
                // ignore DM25 NACKs
                .flatMap(e -> e.left.stream())
                // filter invalid DM25 out
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
                .collect(Collectors.toList());

        if (!dm25Packets.isEmpty()) {
            // Verify the no data & DTC didn't cause freeze frame
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM25 check done at table step 2.a");
            dm25Packets.forEach(packet -> {
                failureMessage.append(NL).append("Module with source address " + packet.getSourceAddress() + ", has ")
                        .append(packet.getFreezeFrames().size() + " supported SPNs");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 2.a DM25 Verification");
        }
        return true;
    }

    /**
     * @param listener
     */
    public boolean verifyDM26(ResultsListener listener) {
        // 5. Activity since code clear
        // a. DM26 diagnostic readiness 3 shall report 0 for number of warm-ups
        // since code clear
        List<DM26TripDiagnosticReadinessPacket> dm26Packets = dtcModule.requestDM26(listener).getPackets()
                .stream()
                .filter(packet -> packet instanceof DM26TripDiagnosticReadinessPacket)
                .map(p -> p)
                .filter(packet -> packet.getWarmUpsSinceClear() != 0)
                .collect(Collectors.toList());
        if (!dm26Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM26 check done at table step 5.a");
            failureMessage.append(NL).append("Modules with source address ");
            dm26Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress())
                        .append(", reported ")
                        .append(packet.getWarmUpsSinceClear())
                        .append(" warm-ups since code clear");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 5.a DM26 Verification");
        }
        return true;
    }

    /**
     * @param previousDM28Packets
     * @param listener
     */
    public boolean verifyDM28(List<DM28PermanentEmissionDTCPacket> previousDM28Packets, ResultsListener listener) {
        // 8. Permanent DTCs
        // a. DM28 Permanent DTCs shall not be erased/still report any permanent
        // DTC that was present before code clear.
        List<DM28PermanentEmissionDTCPacket> dm28Packets = dtcModule.requestDM28(listener, true).getPackets()
                .stream()
                .map(p -> p)
                .filter(t -> t.getDtcs().size() != 0)
                .collect(Collectors.toList());
        // Since we only care about packets that have dtc, let make sure both
        // both lists accurately reflect that.
        if (!previousDM28Packets.stream()
                .filter(packet -> packet.getDtcs().size() != 0).collect(Collectors.toList()).equals(dm28Packets)) {
            StringBuilder failMessage = new StringBuilder(
                    "Section A.5 verification failed during DM28 check done at table step 8.a");
            failMessage.append(NL).append(
                    "Pre DTC all clear code sent retrieved the DM28 packet :").append(NL);
            previousDM28Packets.forEach(packet -> {
                failMessage.append(packet.toString());
            });
            failMessage.append(NL);
            failMessage.append("Post DTC all clear code sent retrieved the DM28 packet :");
            failMessage.append(NL);
            dm28Packets.forEach(packet -> {
                failMessage.append(packet.toString());
            });
            listener.onProgress(failMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 8.a DM28 Verification");
        }
        return true;
    }

    /**
     * @param listener
     */
    public boolean verifyDM29(ResultsListener listener) {
        // d. DM29 shall report zero for number of pending, active, and
        // previously active DTCs
        List<DM29DtcCounts> dm29Packets = dtcModule.requestDM29(listener).getPackets().stream()
                .filter(packet -> packet instanceof DM29DtcCounts)
                .map(p -> p)
                .filter(t -> t.getAllPendingDTCCount() != 0 ||
                        t.getEmissionRelatedMILOnDTCCount() != 0 ||
                        t.getEmissionRelatedPendingDTCCount() != 0 ||
                        t.getEmissionRelatedPermanentDTCCount() != 0 ||
                        t.getEmissionRelatedPreviouslyMILOnDTCCount() != 0)
                .collect(Collectors.toList());

        if (!dm29Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM29 check done at table step 1.d");
            failureMessage.append(NL).append("Modules with source address ");
            dm29Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", ")
                        .append(packet.toString() + " ");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 1.d DM29 Verification");
        }
        return true;
    }

    /**
     * @param listener
     */
    public boolean verifyDM31(ResultsListener listener) {
        // 3. MIL information
        // a. DM31 lamp status shall report no DTCs causing MIL on (if
        // supported, see section 6 provisions before section 6.1).
        List<DM31DtcToLampAssociation> dm31Packets = dtcModule.requestDM31(listener).getPackets().stream()
                .filter(packet -> packet instanceof DM31DtcToLampAssociation)
                .filter(t -> !t.getDtcLampStatuses().isEmpty())
                .collect(Collectors.toList());
        if (!dm31Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM31 check done at table step 3.a");
            failureMessage.append(NL).append("Modules with source address ");
            dm31Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", is reporting ")
                        .append(packet.getDtcLampStatuses().size() + " with DTC lamp status(es) causing MIL on.");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 3.a DM31 Verification");
        }
        return true;
    }

    /**
     * @param previousDM33Packets
     * @param listener
     * @param obdModuleAddresses
     */
    public boolean verifyDM33(List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> previousDM33Packets,
            ResultsListener listener, List<Integer> obdModuleAddresses) {
        // 9. Engine runtime information
        // a. DM33 EI-AECD information shall not be reset/cleared for any
        // non-zero values present before code clear.
        List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> dm33Packets = new ArrayList<>();
        obdModuleAddresses.forEach(address -> {
            dm33Packets.addAll(dtcModule.requestDM33(listener, address).getPackets()
                    .stream()
                    .map(p -> p)
                    .collect(Collectors.toList()));
        });

        Collections.sort(previousDM33Packets,
                (packet1, packet2) -> packet1.getSourceAddress() - packet2.getSourceAddress());
        Collections.sort(dm33Packets, (packet1, packet2) -> packet1.getSourceAddress() - packet2.getSourceAddress());

        if (!previousDM33Packets.equals(dm33Packets)) {
            StringBuilder failMessage = new StringBuilder(
                    "Section A.5 verification failed during DM33 check done at table step 9.a");
            failMessage.append(NL)
                    .append("Pre DTC all clear code sent retrieved the DM33 packet :");
            previousDM33Packets.forEach(packet -> {
                failMessage.append(NL).append("   ").append(packet.toString());
            });
            failMessage.append(NL)
                    .append("Post DTC all clear code sent retrieved the DM33 packet :");
            dm33Packets.forEach(packet -> {
                failMessage.append(NL).append("   ").append(packet.toString());
            });
            listener.onProgress(failMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 9.a DM33 Verification");
        }
        return true;
    }

    /**
     * @param listener
     */
    public boolean verifyDM5(ResultsListener listener) {
        // e. DM5 shall report zero for number of active and previously active
        // DTCs
        boolean passTest = true;
        List<DM5DiagnosticReadinessPacket> dm5Packets = diagnosticReadinessModule.requestDM5(listener, true)
                .getPackets()
                .stream()
                .filter(packet -> packet instanceof DM5DiagnosticReadinessPacket)
                .map(p -> p)
                .filter(t -> (t.getActiveCodeCount() != 0) ||
                        (t.getPreviouslyActiveCodeCount() != 0))
                .collect(Collectors.toList());
        if (!dm5Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM5 check done at table step 1.e");
            failureMessage.append(NL).append("Modules with source address ");
            dm5Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress())
                        .append(", reported ")
                        .append(packet.getActiveCodeCount())
                        .append(" active DTCs and ")
                        .append(packet.getPreviouslyActiveCodeCount()).append(" previously acitve DTCs");
            });
            listener.onProgress(failureMessage.toString());
            passTest = false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 1.e DM5 Verification");
        }

        // 4. Readiness status
        // a. DM5 shall report test not complete (1) for all supported monitors
        // except comprehensive components.
        List<MonitoredSystem> monitoredSystems = new ArrayList<>();
        StringBuilder failedMessage = new StringBuilder(
                "Section A.5 verification failed during DM5 check done at table step 4.a");
        failedMessage.append(NL).append("Module address ");

        dm5Packets.forEach(packet -> {
            failedMessage.append(packet.getSourceAddress());
            failedMessage.append(" :");
            failedMessage.append(NL);
            for (MonitoredSystem system : packet.getContinuouslyMonitoredSystems()) {
                if (system.getStatus().isEnabled() && system.getStatus().isComplete()) {
                    monitoredSystems.addAll(packet.getContinuouslyMonitoredSystems());
                    failedMessage.append(packet.toString());
                }
            }
        });
        if (!monitoredSystems.isEmpty()) {
            listener.onProgress(failedMessage.toString());
            passTest = false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 4.a DM5 Verification");
        }
        return passTest;
    }

    /**
     * @param listener
     */
    public boolean verifyDM6(ResultsListener listener) {
        // 1. Emission-related DTCs
        // a. DM6 pending shall report no DTCs and MIL off and not flashing
        List<DM6PendingEmissionDTCPacket> dm6Packets = dtcModule.requestDM6(listener)
                .getPackets()
                .stream()
                .map(p -> p)
                .filter(t -> (!t.getDtcs().isEmpty()) ||
                        (t.getMalfunctionIndicatorLampStatus() != LampStatus.OFF))
                .collect(Collectors.toList());
        if (!dm6Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed at DM6 check done at table step 1.a");
            failureMessage.append(NL).append("Modules with source address ");
            dm6Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", reported ")
                        .append(packet.getDtcs().size()).append(" DTCs.").append(NL).append("MIL status is : ")
                        .append(packet.getMalfunctionIndicatorLampStatus()).append(".");
            });
            listener.onProgress(failureMessage.toString());
            return false;
        } else {
            listener.onProgress("PASS: Section A.5 Step 1.a DM6 Verfication");
        }
        return true;
    }

    /**
     * @param listener
     * @param obdModuleAddresses
     */
    public boolean verifyDM7DM30(ResultsListener listener, List<Integer> obdModuleAddresses) {
        // 6. Test results
        // a. DM7/DM30 Test Results shall report all test results with
        // initialized results and limits (all 0x00 or 0xFB00 for results and
        // 0xFFFF for limits)
        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        obdModuleAddresses.forEach(address -> {
            for (SupportedSPN supportedSPN : dataRepository.getObdModule(address).getTestResultSpns()) {
                dm30Packets.addAll(
                        obdTestsModule.requestDM30Packets(listener, address, supportedSPN.getSpn())
                                .getPackets()
                                .stream()
                                .filter(packet -> packet.getTestResults().size() != 0)
                                .map(p -> p)
                                .filter(p -> {
                                    for (ScaledTestResult result : p.getTestResults()) {
                                        if (!isValid(result.getTestMaximum()) || !isValid(result.getTestValue())
                                                || !isValid(result.getTestMinimum())) {
                                            return true;
                                        }
                                    }
                                    return false;
                                })
                                .collect(Collectors.toList()));
            }
        });
        if (!dm30Packets.isEmpty()) {
            boolean[] failed = { false };
            StringBuilder failureMessage = new StringBuilder(
                    "Section A.5 verification failed during DM7/DM30 check done at table step 6.a");
            failureMessage.append(NL).append("DM30 Scaled Test Results for");
            dm30Packets.forEach(packet -> {
                failureMessage.append(NL).append("source address ").append(packet.getSourceAddress())
                        .append(" are : [")
                        .append(NL);
                for (ScaledTestResult testResult : packet.getTestResults()) {
                    int testMaximum = testResult.getTestMaximum();
                    if (!isValid(testMaximum)) {
                        failureMessage.append("  TestMaximum failed and the value returned was : ")
                                .append(testMaximum).append(NL);
                        failed[0] = true;
                    }
                    int testValue = testResult.getTestValue();
                    if (!isValid(testValue)) {
                        failureMessage.append("  TestResult failed and the value returned was : ")
                                .append(testResult.getTestValue()).append(NL);
                        failed[0] = true;
                    }
                    int testMinimum = testResult.getTestMinimum();
                    if (!isValid(testMinimum)) {
                        failureMessage.append("  TestMinimum failed and the value returned was : ")
                                .append(testResult.getTestMinimum()).append(NL);
                        failed[0] = true;
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

    /**
     * @param previousEngineHoursPackets
     * @param listener
     */
    public boolean verifyEngineHours(List<EngineHoursPacket> previousEngineHoursPackets, ResultsListener listener) {
        // b. Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle
        // time (PGN 65244 (SPN 235)) shall not be reset/cleared for any
        // non-zero values present before code clear
        List<EngineHoursPacket> engineHourPackets = vehicleInformationModule.requestEngineHours(listener)
                .getPackets()
                .stream()
                .map(p -> p)
                .collect(Collectors.toList());
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
            previousEngineHoursPackets.forEach(packet -> {
                failMessage.append("   ").append(packet.toString())
                        .append(NL);
            });
            if (previousEngineHoursPackets.isEmpty()) {
                failMessage.append("   EMPTY")
                        .append(NL);
            }
            failMessage.append("Current packet(s) was/were:")
                    .append(NL);
            engineHourPackets.forEach(packet -> {
                failMessage.append("   ").append(packet.toString())
                        .append(NL);
            });
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
}