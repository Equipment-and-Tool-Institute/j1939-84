/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.model.Outcome.FAIL;

import java.util.Collection;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.IdleOperationPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class SectionA5MessageVerifier {

    private final DataRepository dataRepository;
    private final DiagnosticMessageModule diagMsgModule;
    private final VehicleInformationModule vehInfoModule;
    private final int partNumber;
    private final int stepNumber;

    SectionA5MessageVerifier(int partNumber, int stepNumber) {
        this(DataRepository.getInstance(),
             new DiagnosticMessageModule(),
             new VehicleInformationModule(),
             partNumber,
             stepNumber);
    }

    protected SectionA5MessageVerifier(DataRepository dataRepository,
                                       DiagnosticMessageModule diagMsgModule,
                                       VehicleInformationModule vehInfoModule,
                                       int partNumber,
                                       int stepNumber) {
        this.dataRepository = dataRepository;
        this.diagMsgModule = diagMsgModule;
        this.vehInfoModule = vehInfoModule;
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
    }

    public void setJ1939(J1939 j1939) {
        diagMsgModule.setJ1939(j1939);
        vehInfoModule.setJ1939(j1939);
    }

    boolean checkDM6(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 1.a. DM6 pending shall report no DTCs and MIL off and not flashing
        return diagMsgModule.requestDM6(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = !p.hasDTCs() && p.getMalfunctionIndicatorLampStatus() == OFF;
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM12(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 1.b. DM12 active shall report no DTCs and MIL off and not flashing
        return diagMsgModule.requestDM12(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = !p.hasDTCs() && p.getMalfunctionIndicatorLampStatus() == OFF;
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM23(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 1.c. DM23 previously active shall report no DTCs and MIL off and not flashing
        return diagMsgModule.requestDM23(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = !p.hasDTCs() && p.getMalfunctionIndicatorLampStatus() == OFF;
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM29(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 1.d. DM29 shall report zero for number of pending, active, and previously active DTCs
        return diagMsgModule.requestDM29(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = p.getEmissionRelatedPendingDTCCount() == 0
                                        && p.getEmissionRelatedMILOnDTCCount() == 0
                                        && p.getEmissionRelatedPreviouslyMILOnDTCCount() == 0;
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM5(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 1.e. DM5 shall report zero for number of active and previously active DTCs.
        // 4.a. DM5 shall report test not complete (1) for all supported monitors except comprehensive components.
        return diagMsgModule.requestDM5(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean allTestsIncomplete = p.getMonitoredSystems()
                                                              .stream()
                                                              .filter(s -> s.getId() != CompositeSystem.COMPREHENSIVE_COMPONENT)
                                                              .map(MonitoredSystem::getStatus)
                                                              .noneMatch(MonitoredSystemStatus::isComplete);

                                boolean noCodes = p.getActiveCodeCount() == 0 && p.getPreviouslyActiveCodeCount() == 0;

                                boolean isErased = noCodes && allTestsIncomplete;

                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM25(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 2.a. DM25 expanded freeze frame shall report no data and DTC causing freeze frame
        // with bytes 1-5 = 0 and bytes 6-8 = 255.
        return diagMsgModule.requestDM25(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = p.getFreezeFrames().isEmpty();
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM31(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 3.a. DM31 lamp status shall report no DTCs causing MIL on (if supported).
        return diagMsgModule.requestDM31(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = p.getDtcLampStatuses()
                                                    .stream()
                                                    .allMatch(s -> s.getMalfunctionIndicatorLampStatus() == OFF);
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM21(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 3.b. DM21 diagnostic readiness 2 shall report 0 for distance with MIL on and minutes run with MIL on.
        // 5.b. DM21 diagnostic readiness 2 shall report 0 for distance since code clear and minutes run since code
        // clear.
        return diagMsgModule.requestDM21(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = p.getKmWhileMILIsActivated() == 0
                                        && p.getMinutesWhileMILIsActivated() == 0
                                        && p.getKmSinceDTCsCleared() == 0
                                        && p.getMinutesSinceDTCsCleared() == 0;
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM26(ResultsListener listener, String section, int address, boolean asErased, boolean reportFailure) {
        // 5.a. DM26 diagnostic readiness 3 shall report 0 for number of warm-ups since code clear.
        return diagMsgModule.requestDM26(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = p.getWarmUpsSinceClear() == 0;
                                return asErased != isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, asErased, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkTestResults(ResultsListener listener,
                             String section,
                             int address,
                             boolean asErased,
                             boolean reportFailure) {
        // 6.a. DM7/DM30 Test Results shall report all test results with initialized results and limits
        // (all 0x00 or 0xFB00 for results and 0xFFFF for limits).
        var isErased = dataRepository.getObdModule(address)
                                     .getTestResultSPNs()
                                     .stream()
                                     .map(SupportedSPN::getSpn)
                                     .map(spn -> diagMsgModule.requestTestResult(listener, address, 247, spn, 31))
                                     .flatMap(BusResult::toPacketStream)
                                     .map(DM30ScaledTestResultsPacket::getTestResults)
                                     .flatMap(Collection::stream)
                                     .allMatch(ScaledTestResult::isInitialized);

        boolean failure = asErased != isErased;
        if (failure) {
            addFailure(listener, section, asErased, Lookup.getAddressName(address), "Test Results", reportFailure);
        }
        return !failure;
    }

    boolean checkDM20(ResultsListener listener, String section, int address, boolean reportFailure) {
        // 7.a. DM20 Monitor Performance Ratio data shall not be reset/shall stay the same as it was before code clear
        // for all values including the number of ignition cycles, general denominators, monitor specific numerators,
        // and monitor specific denominators.
        var repoPacket = getLatest(DM20MonitorPerformanceRatioPacket.class, address);
        if (repoPacket == null) {
            return true;
        }

        return diagMsgModule.requestDM20(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                return !p.equals(repoPacket);
                            })
                            .peek(p -> {
                                addFailure(listener, section, false, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM28(ResultsListener listener, String section, int address, boolean reportFailure) {
        // 8.a. DM28 permanent DTCs shall not be erased/still report any permanent DTC
        // that was present before code clear.
        var repoPacket = getLatest(DM28PermanentEmissionDTCPacket.class, address);
        if (repoPacket == null || !repoPacket.hasDTCs()) {
            return true;
        }

        return diagMsgModule.requestDM28(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                return !p.hasDTCs();
                            })
                            .peek(p -> {
                                addFailure(listener, section, false, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkDM33(ResultsListener listener, String section, int address, boolean reportFailure) {
        // 9.a. DM33 EI-AECD information shall not be reset/cleared for any non-zero values present before code clear.
        var repoPacket = getLatest(DM33EmissionIncreasingAECDActiveTime.class, address);
        if (repoPacket == null) {
            return true;
        }

        return diagMsgModule.requestDM33(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                boolean isErased = false;
                                for (EngineHoursTimer timer : repoPacket.getEiAecdEngineHoursTimers()) {
                                    var pTimer = p.getTimer(timer.getEiAecdNumber());
                                    if (timer.getEiAecdTimer1() != 0 && pTimer.getEiAecdTimer1() == 0) {
                                        isErased = true;
                                        break;
                                    }

                                    if (timer.getEiAecdTimer2() != 0 && pTimer.getEiAecdTimer2() == 0) {
                                        isErased = true;
                                        break;
                                    }
                                }
                                return isErased;
                            })
                            .peek(p -> {
                                addFailure(listener, section, false, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkEngineRunTime(ResultsListener listener, String section, int address, boolean reportFailure) {
        // 9.b. Cumulative engine runtime [PGN 65253 (SPN 247))] shall not be reset/cleared for any non-zero values
        // present before code clear.
        var packet = getLatest(EngineHoursPacket.class, address);
        if (packet == null || packet.getEngineHours() == 0) {
            return true;
        }

        return vehInfoModule.requestEngineHours(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                return p.getEngineHours() < packet.getEngineHours();
                            })
                            .peek(p -> {
                                addFailure(listener, section, false, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    boolean checkEngineIdleTime(ResultsListener listener, String section, int address, boolean reportFailure) {
        // 9.b. Cumulative engine idle time [(PGN 65244 (SPN 235)] shall not be reset/cleared for any non-zero values
        // present before code clear.
        var packet = getLatest(IdleOperationPacket.class, address);
        if (packet == null || packet.getEngineIdleHours() == 0) {
            return true;
        }

        return vehInfoModule.requestIdleOperation(listener, address)
                            .toPacketStream()
                            .filter(p -> {
                                return p.getEngineIdleHours() < packet.getEngineIdleHours();
                            })
                            .peek(p -> {
                                addFailure(listener, section, false, p, reportFailure);
                            })
                            .findAny()
                            .isEmpty();
    }

    private <T extends GenericPacket> T getLatest(Class<T> clazz, int address) {
        return dataRepository.getObdModule(address).getLatest(clazz);
    }

    private void addFailure(ResultsListener listener,
                            String section,
                            boolean asErased,
                            GenericPacket p,
                            boolean reportFailure) {
        addFailure(listener, section, asErased, p.getModuleName(), p.getName(), reportFailure);
    }

    private void addFailure(ResultsListener listener,
                            String section,
                            boolean asErased,
                            String moduleName,
                            String dataName,
                            boolean reportFailure) {
        if (reportFailure) {
            if (asErased) {
                addFailure(listener, section + " - " + moduleName + " did not erase " + dataName + " data");
            } else {
                addFailure(listener, section + " - " + moduleName + " erased " + dataName + " data");
            }
        }
    }

    private void addFailure(ResultsListener listener, String message) {
        listener.addOutcome(partNumber, stepNumber, FAIL, message);
    }

}
