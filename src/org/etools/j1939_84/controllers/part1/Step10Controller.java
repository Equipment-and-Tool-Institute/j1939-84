/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31ScaledTestResults;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
 *
 */
public class Step10Controller extends StepController {

    private final DataRepository dataRepository;
    private final DiagnosticReadinessModule diagnosticReadinessModule;
    private final DTCModule dtcModule;
    private final OBDTestsModule obdTestsModule;

    Step10Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new DTCModule(), new PartResultFactory(),
                new DiagnosticReadinessModule(), new OBDTestsModule(), dataRepository);
    }

    protected Step10Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule, DTCModule dtcModule,
            PartResultFactory partResultFactory, DiagnosticReadinessModule diagnosticReadinessModule,
            OBDTestsModule obdTestsModule, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.dataRepository = dataRepository;
        this.dtcModule = dtcModule;
        this.obdTestsModule = obdTestsModule;
        this.diagnosticReadinessModule = diagnosticReadinessModule;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 10";
    }

    @Override
    public int getStepNumber() {
        return 10;
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        dtcModule.setJ1939(getJ1939());
        diagnosticReadinessModule.setJ1939(getJ1939());
        obdTestsModule.setJ1939(getJ1939());

        // Collect all OBD module address
        List<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses().stream().collect(Collectors.toList());

        // Collect our values for comparison after Diagnostic Data Clear/Reset
        // message
        // is sent
        List<DM28PermanentEmissionDTCPacket> previousDM28Packets = dtcModule.requestDM28(getListener()).getPackets()
                .stream()
                .filter(t -> !t.getDtcs().isEmpty())
                .collect(Collectors.toList());

        List<DM20MonitorPerformanceRatioPacket> previousDM20Packets = diagnosticReadinessModule
                .requestDM20(getListener(), true).getPackets();

        List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> previousDM33Packets = obdModuleAddresses
                .stream()
                .flatMap(address -> dtcModule.requestDM33(getListener(), address).getPackets().stream())
                .collect(Collectors.toList());

        // 6.1.10 DM11: Diagnostic Data Clear/Reset for Active DTCs
        List<AcknowledgmentPacket> globalDM11Packets = dtcModule.requestDM11(getListener(), obdModuleAddresses)
                .getAcks();

        // c. Allow 5 s to elapse before proceeding with test step 6.1.9.2.
        getDateTimeModule().pauseFor(5L * 1L * 1000L);

        // 6.1.10.2 Fail criteria:
        // a. Fail if NACK received from any HD OBD ECU.
        // from the dataRepo grab the obdModule addresses
        boolean nacked = globalDM11Packets.stream().anyMatch(packet -> packet.getResponse() == Response.NACK);
        if (nacked) {
            addWarning(1, 10, "6.1.10.3.a - The request for DM11 was ACK'ed");
        }

        // b. Fail if any diagnostic information in any ECU is not reset or
        // starts out
        // with unexpected values. Diagnostic information is defined in section
        // A.5,
        // Diagnostic
        // Information Definition.
        if (!verifyDiagnosticInformation(obdModuleAddresses,
                previousDM28Packets,
                previousDM20Packets,
                previousDM33Packets)) {
            StringBuilder failureMessage = new StringBuilder(
                    "6.1.10.3.b - Fail if any diagnostic information in any ECU is not ")
                            .append("reset or starts outwith unexpected values. ")
                            .append("Diagnostic information is defined in section A.5, ")
                            .append("Diagnostic Information Definition.")
                            .append(NL);
            addFailure(1, 10, failureMessage.toString());
        }

        // 6.1.10.3 Warn criteria:
        // a. Warn if ACK received from any HD OBD ECU.16
        boolean acked = globalDM11Packets.stream().anyMatch(packet -> packet.getResponse() == Response.ACK);
        if (acked) {
            addWarning(1, 10, "6.1.10.3.a - The request for DM11 was ACK'ed");
        }
    }

    private boolean verifyDiagnosticInformation(List<Integer> obdModuleAddresses,
            List<DM28PermanentEmissionDTCPacket> previousDM28Packets,
            List<DM20MonitorPerformanceRatioPacket> previousDM20Packets,
            List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> previousDM33Packets) {
        boolean passed = true;

        // a. DM6 pending shall report no DTCs and MIL off and not flashing
        List<DM6PendingEmissionDTCPacket> dm6Packets = dtcModule.requestDM6(getListener())
                .getPackets()
                .stream()
                .filter(t -> (!t.getDtcs().isEmpty()) ||
                        (t.getMalfunctionIndicatorLampStatus() != LampStatus.OFF &&
                                t.getMalfunctionIndicatorLampStatus() != LampStatus.FAST_FLASH &&
                                t.getMalfunctionIndicatorLampStatus() != LampStatus.SLOW_FLASH))
                .collect(Collectors.toList());
        if (!dm6Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm6Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", reported ")
                        .append(packet.getDtcs().size()).append(" DTCs.").append(NL).append("MIL status is : ")
                        .append(packet.getMalfunctionIndicatorLampStatus()).append(".").append(NL);
            });
            failureMessage.append("reported previously active DTC when verifying DM12");
            addFailure(1, 10, failureMessage.toString());
            passed = false;
        }

        // b. DM12 active shall report no DTCs and MIL off and not flashing
        List<DM12MILOnEmissionDTCPacket> dm12Packets = dtcModule.requestDM12(getListener())
                .getPackets()
                .stream()
                .filter(t -> (!t.getDtcs().isEmpty()) ||
                        (t.getMalfunctionIndicatorLampStatus() != LampStatus.OFF &&
                                t.getMalfunctionIndicatorLampStatus() != LampStatus.FAST_FLASH &&
                                t.getMalfunctionIndicatorLampStatus() != LampStatus.SLOW_FLASH))
                .collect(Collectors.toList());

        if (!dm12Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm12Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", reported ")
                        .append(packet.getDtcs().size()).append(" DTCs.").append(NL).append("MIL status is : ")
                        .append(packet.getMalfunctionIndicatorLampStatus()).append(".").append(NL);
            });
            failureMessage.append("reported previously active DTC when verifying DM12");
            addFailure(1, 10, failureMessage.toString());
            passed = false;
        }

        // c. DM23 previously active shall report no DTCs and MIL off and not
        // flashing
        List<DM23PreviouslyMILOnEmissionDTCPacket> dm23Packets = dtcModule.requestDM23(getListener()).getPackets()
                .stream()
                .filter(t -> (!t.getDtcs().isEmpty()) ||
                        (t.getMalfunctionIndicatorLampStatus() != LampStatus.OFF &&
                                t.getMalfunctionIndicatorLampStatus() != LampStatus.FAST_FLASH &&
                                t.getMalfunctionIndicatorLampStatus() != LampStatus.SLOW_FLASH))
                .collect(Collectors.toList());
        if (!dm23Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm23Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", reported ")
                        .append(packet.getDtcs().size()).append(" DTCs.").append(NL).append("MIL status is : ")
                        .append(packet.getMalfunctionIndicatorLampStatus()).append(".").append(NL);
            });
            failureMessage.append("reported previously active DTC when verifying DM23");
            addFailure(1, 10, failureMessage.toString());
            passed = false;
        }

        // d. DM29 shall report zero for number of pending, active, and
        // previously
        // active DTCs
        List<DM29DtcCounts> dm29Packets = dtcModule.requestDM29(getListener()).getPackets().stream()
                .filter(t -> t.getAllPendingDTCCount() != 0 ||
                        t.getEmissionRelatedMILOnDTCCount() != 0 ||
                        t.getEmissionRelatedPendingDTCCount() != 0 ||
                        t.getEmissionRelatedPermanentDTCCount() != 0 ||
                        t.getEmissionRelatedPreviouslyMILOnDTCCount() != 0)
                .collect(Collectors.toList());

        if (!dm29Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm29Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", ")
                        .append(packet.toString() + " ");
            });
            failureMessage.append("reported previously active DTC when verifying DM29");
            addFailure(1, 10, failureMessage.toString());
            passed = false;

        }

        // e. DM5 shall report zero for number of active and previously active
        // DTCs
        List<DM5DiagnosticReadinessPacket> dm5Packets = diagnosticReadinessModule.requestDM5(getListener(), true)
                .getPackets()
                .stream()
                .filter(t -> (t.getActiveCodeCount() != 0) ||
                        (t.getPreviouslyActiveCodeCount() != 0))
                .collect(Collectors.toList());
        if (!dm5Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm5Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress())
                        .append(", reported ")
                        .append(packet.getActiveCodeCount())
                        .append(" active DTCs and ")
                        .append(packet.getPreviouslyActiveCodeCount()).append(" previously acitve DTCs")
                        .append(NL);
            });
            failureMessage.append("when verifying DM5");
            addFailure(1, 10, failureMessage.toString());
            passed = false;
        }

        // 2. Freeze Frame information
        // a. DM25 expanded freeze frame shall report no data and DTC causing
        // freeze
        // frame with bytes 1-5 = 0 and bytes 6-8 = 255
        List<DM25ExpandedFreezeFrame> dm25Packets = dtcModule.requestDM25(getListener(), obdModuleAddresses)
                .getPackets();
        List<DM25ExpandedFreezeFrame> dm25PacketsWithData = dm25Packets.stream()
                .filter(t -> !t.getFreezeFrames().isEmpty())
                .collect(Collectors.toList());

        if (!dm25PacketsWithData.isEmpty()) {
            // Verify the no data & DTC didn't cause freeze frame
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm25PacketsWithData.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", has ")
                        .append(packet.getFreezeFrames().size() + " supported SPNs");
            });
            failureMessage.append("reported previously active DTC when verifying DM5");
            addFailure(1, 10, failureMessage.toString());
            passed = false;
        }

        // 3. MIL information
        // a. DM31 lamp status shall report no DTCs causing MIL on (if
        // supported. See
        // section 6 provisions before section 6.1).
        List<DM31ScaledTestResults> dm31Packets = dtcModule.requestDM31(getListener()).getPackets().stream()
                .filter(t -> !t.getDtcLampStatuses().isEmpty())
                .collect(Collectors.toList());
        if (!dm31Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm31Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress() + ", ")
                        .append(packet.getDtcLampStatuses().size() + " ");
            });
            failureMessage.append("reported previously active DTC when verifying DM31");
            addFailure(1, 10, failureMessage.toString());
            passed = false;

        }

        // b. DM21 diagnostic readiness 2 shall report 0 for distance with MIL
        // on and
        // minutes run with MIL on
        // 5.b. DM21 diagnostic readiness 2 shall report 0 for distance since
        // code clear
        // and minutes run since code clear
        List<DM21DiagnosticReadinessPacket> dm21Packets = dtcModule.requestDM21(getListener()).getPackets()
                .stream()
                .filter(packet -> packet.getKmWhileMILIsActivated() != 0 ||
                        packet.getMinutesWhileMILIsActivated() != 0 ||
                        packet.getKmSinceDTCsCleared() != 0 ||
                        packet.getMinutesSinceDTCsCleared() != 0)
                .collect(Collectors.toList());

        if (!dm21Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm21Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress())
                        .append(", reported ")
                        .append(packet.getKmSinceDTCsCleared())
                        .append("km(s) for distance with the MIL on.")
                        .append(NL)
                        .append("Reported minutes run with the MIL on is ")
                        .append(packet.getMinutesWhileMILIsActivated())
                        .append(" minutes while MIL is activated.")
                        .append(NL)
                        .append("Km since DTC code clear sent is ")
                        .append(packet.getKmSinceDTCsCleared())
                        .append("Minutes since the DTC code clear sent is ")
                        .append(packet.getMinutesSinceDTCsCleared())
                        .append("");
            });
            addFailure(1, 10, failureMessage.toString());
            passed = false;

        }

        // 4. Readiness status
        // a. DM5 shall report test not complete (1) for all supported monitors
        // except
        // comprehensive components.
        // 54 See 1971.1 section (h)(5.1.2)(A)(ii). 55 See 1971.1 section
        // (h)(4.4.2)(F)(iii). 56 See 1971.1 section (h)(5.2.2)(B).
        // Non-Business
        // SAE INTERNATIONAL J1939TM-84 Proposed Draft June 2019 Page 113 of 129

        // 5. Activity since code clear
        // a. DM26 diagnostic readiness 3 shall report 0 for number of warm-ups
        // since
        // code clear
        List<DM26TripDiagnosticReadinessPacket> dm26Packets = dtcModule.requestDM26(getListener()).getPackets()
                .stream()
                .filter(packet -> packet.getWarmUpsSinceClear() != 0)
                .collect(Collectors.toList());
        if (!dm26Packets.isEmpty()) {
            StringBuilder failureMessage = new StringBuilder("Modules with source address ");
            dm26Packets.forEach(packet -> {
                failureMessage.append(packet.getSourceAddress())
                        .append(", reported ")
                        .append(packet.getWarmUpsSinceClear())
                        .append(" warm-ups since code clear");
            });
            addFailure(1, 10, failureMessage.toString());
            passed = false;

        }

        // 6. Test results
        // a. DM7/DM30 Test Results shall report all test results with
        // initialized
        // results and limits (all 0x00 or 0xFB00 for results and 0xFFFF for
        // limits)
        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        obdModuleAddresses.forEach(address -> {
            for (SupportedSPN supportedSPN : dataRepository.getObdModule(address).getSupportedSpns()) {
                dm30Packets.addAll(
                        obdTestsModule.requestDM30Packets(getListener(), address, supportedSPN.getSpn())
                                .getPackets());
            }
        });
        dm30Packets.forEach(packet -> {
            StringBuilder failureMessage = new StringBuilder("DM30 Scaled Test Results for source address ");
            failureMessage.append(packet.getSourceAddress())
                    .append(" are : ")
                    .append(NL);
            for (ScaledTestResult testResult : packet.getTestResults()) {
                boolean failed = false;
                if (testResult.getTestMaximum() != 0xFFFF) {
                    failureMessage.append("TestMaximum failed and the value returned was : ")
                            .append(testResult.getScaledTestMaximum()).append(NL);
                    failed = true;
                }
                if (failed) {
                    addFailure(1, 10, failureMessage.toString());
                }
            }
        });

        // 7. Monitor Performance data
        // a. DM20 Monitor Performance Ratio data shall not be reset/shall stay
        // the same
        // as it was before code clear for all values including the number of
        // ignition
        // cycles, general denominators, monitor specific numerators, and
        // monitor
        // specific denominators.
        List<DM20MonitorPerformanceRatioPacket> dm20Packets = diagnosticReadinessModule.requestDM20(getListener(), true)
                .getPackets();
        dm20Packets.forEach(packet -> {
            boolean[] passedHere = { true };
            if (!previousDM20Packets.contains(packet)) {
                StringBuilder failureMessage = new StringBuilder(
                        "Pre/post (DM20) after a DTC all clear code (DM11) sent values differ : ");
                failureMessage.append(packet.toString());
                failureMessage.append(" is missing from the pre DM11 listing.")
                        .append(NL);
                addFailure(1, 10, failureMessage.toString());
                passedHere[0] = false;
            }
            // passed = passedHere[0];
        });

        // if (!previousDM20Packets.retainAll(dm20Packets)) {
        // StringBuilder failureMessage = new StringBuilder(
        // "Pre DTC all clear code sent retrieved the DM20 packet : ");
        // failureMessage.append(NL);
        // previousDM28Packets.forEach(packet -> {
        // failureMessage.append(packet.toString());
        // });
        // failureMessage.append(NL);
        // failureMessage.append("Post DTC all clear code sent retrieved the
        // DM20 packet
        // : ");
        // failureMessage.append(NL);
        // dm20Packets.forEach(packet -> {
        // failureMessage.append(packet.toString());
        // });
        // addFailure(1, 10, failureMessage.toString());
        // passed = false;
        //
        // }

        // 8. Permanent DTCs
        // a. DM28 Permanent DTCs shall not be erased/still report any permanent
        // DTC
        // that was present before code clear.
        List<DM28PermanentEmissionDTCPacket> dm28Packets = dtcModule.requestDM28(getListener()).getPackets()
                .stream()
                .filter(t -> t.getDtcs().size() != 0)
                .collect(Collectors.toList());

        if (!previousDM28Packets.retainAll(dm28Packets)) {
            StringBuilder failureMessage = new StringBuilder(
                    "Pre DTC all clear code sent retrieved the DM28 packet : ");
            failureMessage.append(NL);
            previousDM28Packets.forEach(packet -> {
                failureMessage.append(packet.toString());
            });
            failureMessage.append(NL);
            failureMessage.append("Post DTC all clear code sent retrieved the DM28 packet : ");
            failureMessage.append(NL);
            dm28Packets.forEach(packet -> {
                failureMessage.append(packet.toString());
            });
            addFailure(1, 10, failureMessage.toString());
            passed = false;

        }

        // 9. Engine runtime information
        // a. DM33 EI-AECD information shall not be reset/cleared for any
        // non-zero
        // values present before code clear.
        List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> dm33Packets = obdModuleAddresses.stream()
                .flatMap(address -> dtcModule.requestDM33(getListener(), address).getPackets().stream())
                .collect(Collectors.toList());
        if (!previousDM33Packets.retainAll(dm33Packets)) {
            StringBuilder failureMessage = new StringBuilder(
                    "Pre DTC all clear code sent retrieved the DM33 packet : ");
            failureMessage.append(NL);
            previousDM33Packets.forEach(packet -> {
                failureMessage.append(packet.toString());
            });
            failureMessage.append(NL);
            failureMessage.append("Post DTC all clear code sent retrieved the DM33 packet : ");
            failureMessage.append(NL);
            dm33Packets.forEach(packet -> {
                failureMessage.append(packet.toString());
            });
            addFailure(1, 10, failureMessage.toString());
            passed = false;

        }

        // b. Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle
        // time (PGN
        // 65244 (SPN 235)) shall not be reset/cleared for any non-zero values
        // present
        // before code clear
        // getVehicleInformationModule().

        return passed;
    }

}
