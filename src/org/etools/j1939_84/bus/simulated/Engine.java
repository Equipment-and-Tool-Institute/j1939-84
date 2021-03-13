/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.simulated;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.COMPREHENSIVE_COMPONENT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EVAPORATIVE_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR_HEATER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.FUEL_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.MISFIRE;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NOX_CATALYST_ABSORBER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus.AreaStatus.OUTSIDE;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.bus.j1939.packets.ParsedPacket.to2Bytes;
import static org.etools.j1939_84.bus.j1939.packets.ParsedPacket.to4Bytes;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineSpeedPacket;
import org.etools.j1939_84.bus.j1939.packets.FreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.model.KeyState;
import org.etools.j1939_84.model.SpnFmi;

/**
 * Simulated Engine used for System Testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Engine implements AutoCloseable {
    private static final Charset A_UTF8 = StandardCharsets.UTF_8;
    private final static int ADDR = 0x00;
    private static final byte[] COMPONENT_ID = "INT*570261221315646M13*570HM2U3545277**".getBytes(A_UTF8);
    private static final byte[] DISTANCE = to4Bytes(256345L * 8); // km

    /*
     * Calibration ID must be 16 bytes
     */
    private static final byte[] ENGINE_CAL_ID1 = "PBT5MPR3        ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CVN1 = to4Bytes(0x40DCBF96L);

    private static final byte[] ENGINE_SPEED = to2Bytes(1400 * 8); // rpm
    private static final byte[] ENGINE_SPEED_ZERO = to2Bytes(0); // rpm

    private static final byte NA = (byte) 0xFF;
    private static final byte[] NA3 = new byte[] { NA, NA, NA };
    private static final byte[] NA4 = new byte[] { NA, NA, NA, NA };
    private static final byte[] NA8 = new byte[] { NA, NA, NA, NA, NA, NA, NA, NA };

    /*
     * VIN can be any length up to 200 bytes, but should end with *
     */
    private static final byte[] VIN = "3HAMKSTN0FL575012*".getBytes(A_UTF8);
    private KeyState keyState = KeyState.KEY_ON_ENGINE_OFF;

    private final Sim sim;

    private final List<DiagnosticTroubleCode> activeDTCs = new ArrayList<>();
    private final List<DiagnosticTroubleCode> previousDTCs = new ArrayList<>();
    private final List<DiagnosticTroubleCode> pendingDTCs = new ArrayList<>();
    private final List<DiagnosticTroubleCode> permanentDTCs = new ArrayList<>();

    private final Map<SpnFmi, ScaledTestResult> scaledTestResultMap = new HashMap<>();

    private int ignitionCycles = 0;
    private int obdConditions = 0;
    private int secondsSCC = 100000;
    private int secondsWithMIL = 0;
    private int warmUpsSCC = 132948;
    private int secondsRunning = 0;

    private DiagnosticTroubleCode nextFault;

    public Engine(Bus bus) throws BusException {
        sim = new Sim(bus);

        // xmsn rate is actually engine speed dependent
        sim.schedule(100,
                     100,
                     TimeUnit.MILLISECONDS,
                     () -> {
                         if (!isKeyOn()) {
                             // Rather then stopping the schedule, just send a different message
                             return Packet.create(0x1FFFF, ADDR, NA8);
                         } else {
                             return Packet.create(EngineSpeedPacket.PGN,
                                                  ADDR,
                                                  combine(NA3,
                                                          isEngineOn() ? ENGINE_SPEED : ENGINE_SPEED_ZERO,
                                                          NA3));
                         }
                     });

        sim.schedule(100, 100, TimeUnit.MILLISECONDS, () -> Packet.create(65248, ADDR, combine(NA4, DISTANCE)));

        sim.schedule(50,
                     50,
                     TimeUnit.MILLISECONDS,
                     () -> Packet.create(0x0C, 0xF00A, ADDR, false, combine(to4Bytes(0L), NA4)));

        sim.response(p -> isRequestFor(65259, p), () -> Packet.create(65259, ADDR, COMPONENT_ID));

        sim.response(p -> isRequestFor(0x1FFFF, p), p -> {
            setKeyState(KEY_ON_ENGINE_RUNNING);
            return Packet.create(0x1FFFF, ADDR, getKeyStateAsBytes());
        });

        sim.response(p -> isRequestFor(0x1FFFE, p), () -> {
            setKeyState(KeyState.KEY_ON_ENGINE_OFF);
            return Packet.create(0x1FFFE, ADDR, getKeyStateAsBytes());
        });

        sim.response(p -> isRequestFor(0x1FFFC, p), () -> {
            setKeyState(KeyState.KEY_OFF);
            return Packet.create(0x1FFFC, ADDR, getKeyStateAsBytes());
        });

        sim.response(p -> isRequestFor(0x1FFFA, p), () -> {
            nextFault = DiagnosticTroubleCode.create(0xFA, 0x0A, 0, 1);
            return Packet.create(0x1FFFA, ADDR, NA8);
        });

        sim.response(p -> isRequestFor(0x1FFFB, p), () -> {
            nextFault = DiagnosticTroubleCode.create(0xFB, 0x0B, 0, 1);
            return Packet.create(0x1FFFB, ADDR, NA8);
        });

        // 65278, Auxiliary Water Pump Pressure, AWPP, 1 s, 1, 73, Auxiliary Pump Pressure,4 9
        sim.response(p -> isRequestFor(65278, p), () -> Packet.create(65278, ADDR, NA8));

        sim.schedule(1, 1, SECONDS, () -> {
            if (isEngineOn()) {
                secondsRunning++;
                secondsSCC++;
                if (getMilStatus() == ON) {
                    secondsWithMIL++;
                }
            }
        });

        sim.response(p -> isRequestFor(EngineHoursPacket.PGN, p),
                     EngineHoursPacket.create(ADDR, secondsRunning)::getPacket);

        // Address Claim
        sim.response(p -> isRequestFor(0xEE00, p),
                     p -> Packet.create(0xEEFF, ADDR, 0x00, 0x00, 0x40, 0x05, 0x00, 0x00, 0x65, 0x14));

        sim.response(p -> isRequestFor(VehicleIdentificationPacket.PGN, p),
                     p -> Packet.create(VehicleIdentificationPacket.PGN, ADDR, VIN));

        // DM1
        sim.schedule(1,
                     1,
                     SECONDS,
                     () -> DM1ActiveDTCsPacket.create(ADDR,
                                                      getMilStatus(),
                                                      OFF,
                                                      OFF,
                                                      OFF,
                                                      activeDTCs.toArray(new DiagnosticTroubleCode[0]))
                                              .getPacket());

        // DM 2
        sim.response(p -> isRequestFor(DM2PreviouslyActiveDTC.PGN, p),
                     p -> DM2PreviouslyActiveDTC.create(ADDR,
                                                        getMilStatus(),
                                                        OFF,
                                                        OFF,
                                                        OFF,
                                                        previousDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                .getPacket());

        // DM5
        sim.response(p -> isRequestFor(DM5DiagnosticReadinessPacket.PGN, p),
                     p -> DM5DiagnosticReadinessPacket.create(ADDR,
                                                              activeDTCs.size(),
                                                              previousDTCs.size(),
                                                              0x14,
                                                              getEnabledSystems(),
                                                              getCompleteDM5Systems())
                                                      .getPacket());

        // DM6
        sim.response(p -> isRequestFor(DM6PendingEmissionDTCPacket.PGN, p),
                     p -> {
                         return DM6PendingEmissionDTCPacket.create(ADDR,
                                                                   getMilStatus(),
                                                                   OFF,
                                                                   OFF,
                                                                   OFF,
                                                                   pendingDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                           .getPacket();
                     });

        // DM11
        sim.response(p -> (p.getId(0xFFFF) == 0xEAFF) && p.get24(0) == DM11ClearActiveDTCsPacket.PGN,
                     p -> {
                         System.err.println("!!!!!CLEARING CODES!!!!!!");
                         activeDTCs.clear(); // DM1 & DM12
                         previousDTCs.clear(); // DM2 & DM23
                         secondsWithMIL = 0;
                         secondsSCC = 0;
                         warmUpsSCC = 0;
                         // pendingActiveDTCs.clear(); //reset by something else
                         // permanentActiveDTCs.clear(); // reset by 3 cycle and then by General Denom

                         // Clear DM25

                         return Packet.create(0, ADDR, NA8); // Don't return anything
                     });

        // DM12
        sim.response(p -> isRequestFor(DM12MILOnEmissionDTCPacket.PGN, p),
                     p -> DM12MILOnEmissionDTCPacket.create(ADDR,
                                                            getMilStatus(),
                                                            OFF,
                                                            OFF,
                                                            OFF,
                                                            activeDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                    .getPacket());

        // DM19
        sim.response(p -> isRequestFor(DM19CalibrationInformationPacket.PGN, p),
                     p -> Packet.create(DM19CalibrationInformationPacket.PGN | p.getSource(),
                                        ADDR,
                                        combine(ENGINE_CVN1, ENGINE_CAL_ID1)));

        // DM 20
        sim.response(p -> isRequestFor(DM20MonitorPerformanceRatioPacket.PGN, p),
                     p -> {
                         return DM20MonitorPerformanceRatioPacket.create(ADDR,
                                                                         p.getSource(),
                                                                         ignitionCycles,
                                                                         obdConditions,
                                                                         new PerformanceRatio(5322,
                                                                                              0,
                                                                                              obdConditions,
                                                                                              0),
                                                                         new PerformanceRatio(5318, 0, 0, 0),
                                                                         new PerformanceRatio(3058, 0, 0, 0),
                                                                         new PerformanceRatio(3064, 0, 0, 0),
                                                                         new PerformanceRatio(5321, 0, 0, 0),
                                                                         new PerformanceRatio(3055, 0, 0, 0),
                                                                         new PerformanceRatio(4792, 0, 0, 0))
                                                                 .getPacket();
                     });

        // DM21
        sim.response(p -> isRequestFor(DM21DiagnosticReadinessPacket.PGN, p),
                     p -> {
                         System.out.println("TimeSCC " + secondsSCC);
                         return DM21DiagnosticReadinessPacket.create(ADDR,
                                                                     p.getSource(),
                                                                     0,
                                                                     0,
                                                                     (int) getMinutesWithMil(),
                                                                     (int) getMinutesSCC())
                                                             .getPacket();
                     });

        // DM23
        sim.response(p -> isRequestFor(DM23PreviouslyMILOnEmissionDTCPacket.PGN, p),
                     p -> DM23PreviouslyMILOnEmissionDTCPacket.create(ADDR,
                                                                      getMilStatus(),
                                                                      OFF,
                                                                      OFF,
                                                                      OFF,
                                                                      previousDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                              .getPacket());

        // DM24 supported SPNs
        sim.response(p -> isRequestFor(DM24SPNSupportPacket.PGN, p),
                     p -> {
                         return DM24SPNSupportPacket.create(ADDR,
                                                            SupportedSPN.create(27, false, true, false, 1),
                                                            SupportedSPN.create(84, false, true, false, 1),
                                                            SupportedSPN.create(91, false, true, false, 1),
                                                            SupportedSPN.create(92, false, true, true, 1),
                                                            SupportedSPN.create(94, false, true, false, 1),
                                                            SupportedSPN.create(102, true, true, false, 1),
                                                            SupportedSPN.create(108, false, true, false, 1),
                                                            SupportedSPN.create(110, false, true, true, 1),
                                                            SupportedSPN.create(157, true, false, false, 1),
                                                            SupportedSPN.create(158, false, true, false, 1),
                                                            SupportedSPN.create(183, false, true, false, 1),
                                                            SupportedSPN.create(190, false, true, true, 2),
                                                            SupportedSPN.create(235, false, true, false, 1),
                                                            SupportedSPN.create(247, false, true, false, 1),
                                                            SupportedSPN.create(248, false, true, false, 1),
                                                            SupportedSPN.create(512, false, true, true, 1),
                                                            SupportedSPN.create(513, false, true, true, 1),
                                                            SupportedSPN.create(514, false, true, false, 1),
                                                            SupportedSPN.create(539, false, true, false, 1),
                                                            SupportedSPN.create(540, false, true, false, 1),
                                                            SupportedSPN.create(541, false, true, false, 1),
                                                            SupportedSPN.create(542, false, true, false, 1),
                                                            SupportedSPN.create(543, false, true, false, 1),
                                                            SupportedSPN.create(544, false, true, false, 1),
                                                            SupportedSPN.create(651, true, false, false, 1),
                                                            SupportedSPN.create(1323, true, false, false, 1),
                                                            SupportedSPN.create(1324, true, false, false, 1),
                                                            SupportedSPN.create(1325, true, false, false, 1),
                                                            SupportedSPN.create(1326, true, false, false, 1),
                                                            SupportedSPN.create(1413, false, true, false, 1),
                                                            SupportedSPN.create(2630, true, false, false, 1),
                                                            SupportedSPN.create(2791, false, true, false, 1),
                                                            SupportedSPN.create(2978, false, true, false, 1),
                                                            SupportedSPN.create(3031, false, true, false, 1),
                                                            SupportedSPN.create(3058, true, false, false, 1),
                                                            SupportedSPN.create(3226, true, true, false, 1),
                                                            SupportedSPN.create(3251, true, false, false, 1),
                                                            SupportedSPN.create(3301, false, false, true, 2),
                                                            SupportedSPN.create(3361, true, false, false, 1),
                                                            SupportedSPN.create(3516, false, true, false, 1),
                                                            SupportedSPN.create(3609, false, true, false, 1),
                                                            SupportedSPN.create(3700, false, true, false, 1),
                                                            SupportedSPN.create(3713, true, false, false, 1),
                                                            SupportedSPN.create(4364, true, false, false, 1),
                                                            SupportedSPN.create(4752, true, false, false, 1),
                                                            SupportedSPN.create(5018, true, false, false, 1),
                                                            SupportedSPN.create(5466, false, true, false, 1),
                                                            SupportedSPN.create(5827, false, true, false, 1),
                                                            SupportedSPN.create(5829, false, true, false, 1),
                                                            SupportedSPN.create(5837, false, true, false, 1),
                                                            SupportedSPN.create(6895, false, true, false, 1),
                                                            SupportedSPN.create(7333, false, true, false, 1))
                                                    .getPacket();
                     });

        // DM25
        sim.response(p -> isRequestFor(DM25ExpandedFreezeFrame.PGN, p),
                     p -> {
                         DiagnosticTroubleCode dtc = null;
                         if (!pendingDTCs.isEmpty()) {
                             dtc = pendingDTCs.get(0);
                         } else if (!activeDTCs.isEmpty()) {
                             dtc = activeDTCs.get(0);
                         }

                         if (dtc == null) {
                             return DM25ExpandedFreezeFrame.create(ADDR).getPacket();
                         } else {
                             var freezeFrame = new FreezeFrame(dtc, new int[8]);
                             return DM25ExpandedFreezeFrame.create(ADDR, freezeFrame).getPacket();
                         }
                     });

        // DM26
        sim.response(p -> isRequestFor(DM26TripDiagnosticReadinessPacket.PGN, p),
                     p -> DM26TripDiagnosticReadinessPacket.create(ADDR,
                                                                   secondsSCC,
                                                                   warmUpsSCC,
                                                                   getEnabledSystems(),
                                                                   getCompleteDM26Systems())
                                                           .getPacket());

        // DM27
        sim.response(p -> isRequestFor(DM27AllPendingDTCsPacket.PGN, p),
                     p -> DM27AllPendingDTCsPacket.create(ADDR,
                                                          getMilStatus(),
                                                          OFF,
                                                          OFF,
                                                          OFF,
                                                          pendingDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                  .getPacket());

        // DM28
        sim.response(p -> isRequestFor(DM28PermanentEmissionDTCPacket.PGN, p),
                     p -> DM28PermanentEmissionDTCPacket.create(ADDR,
                                                                getMilStatus(),
                                                                OFF,
                                                                OFF,
                                                                OFF,
                                                                permanentDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                        .getPacket());

        // DM29 response
        sim.response(p -> isRequestFor(DM29DtcCounts.PGN, p),
                     p -> DM29DtcCounts.create(ADDR,
                                               p.getSource(),
                                               pendingDTCs.size(),
                                               pendingDTCs.size(),
                                               activeDTCs.size(),
                                               previousDTCs.size(),
                                               permanentDTCs.size())
                                       .getPacket());

        // DM30 response for DM7 Request
        sim.response(Engine::isRequestForDM30, p -> {
            var results = new ArrayList<ScaledTestResult>();

            var dm7 = new DM7CommandTestsPacket(p);
            int testId = dm7.getTestId();
            int spn = dm7.getSpn();
            int fmi = dm7.getFmi();

            if (testId == 247 && fmi == 31) {
                if (spn == 157) {
                    results.add(ScaledTestResult.create(testId, spn, 18, 385, isEngineOn() ? secondsSCC : 0, 0, 0));
                    results.add(ScaledTestResult.create(testId, spn, 16, 385, 0, 0, 0));
                } else if (spn == 651) {
                    results.add(ScaledTestResult.create(testId, spn, 7, 385, 0, 0, 0));
                } else if (spn == 1323 || spn == 1324 || spn == 1325 || spn == 1326) {
                    results.add(ScaledTestResult.create(testId, spn, 31, 385, 0, 0, 0));
                } else if (spn == 3058) {
                    results.add(ScaledTestResult.create(testId, spn, 18, 385, 0, 0, 0));
                    results.add(ScaledTestResult.create(testId, spn, 16, 385, 0, 0, 0));
                } else if (spn == 4752) {
                    results.add(ScaledTestResult.create(testId, spn, 1, 385, 0, 0, 0));
                } else if (spn == 102) {
                    results.add(ScaledTestResult.create(testId, spn, 17, 385, 0, 0, 0));
                    results.add(ScaledTestResult.create(testId, spn, 16, 385, 0, 0, 0));
                } else if (spn == 2630) {
                    results.add(ScaledTestResult.create(testId, spn, 16, 385, 0, 0, 0));
                } else if (spn == 5018) {
                    results.add(ScaledTestResult.create(testId, spn, 18, 385, 0, 0, 0));
                } else if (spn == 4364) {
                    results.add(ScaledTestResult.create(testId, spn, 17, 385, 0, 0, 0));
                } else if (spn == 3361) {
                    results.add(ScaledTestResult.create(testId, spn, 7, 385, 0, 0, 0));
                } else if (spn == 3251) {
                    results.add(ScaledTestResult.create(testId, spn, 2, 385, 0, 0, 0));
                } else if (spn == 3713) {
                    results.add(ScaledTestResult.create(testId, spn, 31, 385, 0, 0, 0));
                } else if (spn == 3226) {
                    results.add(ScaledTestResult.create(testId, spn, 16, 385, 0, 0, 0));
                } else {
                    results.add(ScaledTestResult.create(testId, spn, fmi, 385, 0, 0, 0));
                }
            } else if (testId == 246 && spn == 5846 && fmi == 31) {
                results.addAll(scaledTestResultMap.values());
            } else if (testId == 250) {
                results.add(scaledTestResultMap.get(SpnFmi.of(spn, fmi)));
            }

            if (testId == 247) {
                // Save the test results for later
                for (ScaledTestResult result : results) {
                    scaledTestResultMap.put(SpnFmi.of(result), result);
                }
            }

            return DM30ScaledTestResultsPacket.create(ADDR, p.getSource(), results.toArray(new ScaledTestResult[0]))
                                              .getPacket();
        });

        // DM31 response
        sim.response(p -> isRequestFor(DM31DtcToLampAssociation.PGN, p),
                     p -> {
                         if (activeDTCs.isEmpty()) {
                             return DM31DtcToLampAssociation.create(ADDR, p.getSource()).getPacket();
                         } else {
                             var lampStatus = DTCLampStatus.create(activeDTCs.get(0), OFF, getMilStatus(), OFF, OFF);
                             return DM31DtcToLampAssociation.create(ADDR, p.getSource(), lampStatus).getPacket();
                         }
                     });

        // @formatter:off
        // DM33 response for DM33 Global Request for PGN 41216
        sim.response(p -> isRequestFor(DM33EmissionIncreasingAECDActiveTime.PGN, p),
                     p -> Packet
                             .create(DM33EmissionIncreasingAECDActiveTime.PGN | p.getSource(),
                                     ADDR,
                                     0x01, //Number
                                     0x00, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF, //Timer2

                                     0x04, //Number
                                     0x00, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF, //Timer2

                                     0x06, //Number
                                     0x00, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF, //Timer2

                                     0x0B, //Number
                                     0x00, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF, //Timer2

                                     0x0C, //Number
                                     0x00, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF, //Timer2

                                     0x0D, //Number
                                     0x00, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF, //Timer2

                                     0x31, //Number
                                     0x01, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF, //Timer2

                                     0x38,  //Number
                                     0x0D, 0x00, 0x00, 0x00, //Timer1
                                     0xFF, 0xFF, 0xFF, 0xFF //Timer2
                             ));
        // @formatter:on

        sim.response(p -> isRequestFor(DM34NTEStatus.PGN, p),
                     p -> DM34NTEStatus.create(ADDR,
                                               p.getSource(),
                                               OUTSIDE,
                                               OUTSIDE,
                                               OUTSIDE,
                                               OUTSIDE,
                                               OUTSIDE,
                                               OUTSIDE)
                                       .getPacket());

        // DM56 Engine Model Year
        sim.response(p -> isRequestFor(DM56EngineFamilyPacket.PGN, p),
                     DM56EngineFamilyPacket.create(ADDR, 2015, true, "US HD OBD    ")::getPacket);
    }

    private List<CompositeSystem> getCompleteDM26Systems() {
        return List.of(AC_SYSTEM_REFRIGERANT,
                       CATALYST,
                       COLD_START_AID_SYSTEM,
                       COMPREHENSIVE_COMPONENT,
                       EVAPORATIVE_SYSTEM,
                       HEATED_CATALYST,
                       SECONDARY_AIR_SYSTEM);
    }

    private static byte[] combine(byte[]... bytes) {
        int length = 0;
        for (byte[] b : bytes) {
            length += b.length;
        }
        ByteBuffer bb = ByteBuffer.allocate(length);
        for (byte[] data : bytes) {
            bb.put(data);
        }
        return bb.array();
    }

    private static boolean isRequestForDM30(Packet packet) {
        return packet.getPgn() == 0xE300;
    }

    private static boolean isRequestFor(int pgn, Packet packet) {
        return (packet.getId(0xFFFF) == (0xEA00 | Engine.ADDR) || packet.getId(0xFFFF) == 0xEAFF)
                && packet.get24(0) == pgn;
    }

    @Override
    public void close() {
        sim.close();
    }

    private void setKeyState(KeyState keyState) {
        J1939_84.getLogger().log(Level.INFO, "to " + keyState);

        if (this.keyState != KEY_ON_ENGINE_RUNNING && keyState == KEY_ON_ENGINE_RUNNING) {
            ignitionCycles++;
            System.out.println("Ign Cycles are " + ignitionCycles);
            secondsSCC += 60; // Because there are "human delays" in this testing

            if (TimeUnit.SECONDS.toMinutes(secondsRunning) > 9) {
                obdConditions++;
                activeDTCs.clear();
                permanentDTCs.clear();
            }

            if (!pendingDTCs.isEmpty() && ignitionCycles == 3) {
                System.out.println("Found Pending DTC & IgnCycles = " + ignitionCycles);
                DiagnosticTroubleCode dtc = pendingDTCs.get(0);
                activeDTCs.add(dtc);
                permanentDTCs.add(dtc);
                pendingDTCs.clear();
            }

            if (nextFault != null) {
                System.out.println("Setting Pending Fault");
                pendingDTCs.add(nextFault);
                nextFault = null;
            }

        }

        this.keyState = keyState;
    }

    private long getMinutesWithMil() {
        return TimeUnit.SECONDS.toMinutes(secondsWithMIL);
    }

    private long getMinutesSCC() {
        return TimeUnit.SECONDS.toMinutes(secondsSCC);
    }

    private boolean isEngineOn() {
        return keyState == KEY_ON_ENGINE_RUNNING;
    }

    private boolean isKeyOn() {
        return keyState != KeyState.KEY_OFF;
    }

    private byte[] getKeyStateAsBytes() {
        long a = isEngineOn() ? 1 : 0;
        long a1 = isKeyOn() ? 1 : 0;
        return combine(to4Bytes(a1), to4Bytes(a));
    }

    private LampStatus getMilStatus() {
        return activeDTCs.isEmpty() ? OFF : ON;
    }

    private List<CompositeSystem> getEnabledSystems() {
        return List.of(BOOST_PRESSURE_CONTROL_SYS,
                       COMPREHENSIVE_COMPONENT,
                       DIESEL_PARTICULATE_FILTER,
                       EGR_VVT_SYSTEM,
                       EXHAUST_GAS_SENSOR,
                       EXHAUST_GAS_SENSOR_HEATER,
                       FUEL_SYSTEM,
                       MISFIRE,
                       NMHC_CONVERTING_CATALYST,
                       NOX_CATALYST_ABSORBER);
    }

    private List<CompositeSystem> getCompleteDM5Systems() {
        return List.of(AC_SYSTEM_REFRIGERANT,
                       CATALYST,
                       COLD_START_AID_SYSTEM,
                       COMPREHENSIVE_COMPONENT,
                       EVAPORATIVE_SYSTEM,
                       HEATED_CATALYST,
                       SECONDARY_AIR_SYSTEM);
    }
}
