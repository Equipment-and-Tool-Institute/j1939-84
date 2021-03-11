/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.simulated;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;

/**
 * Simulated Engine used for System Testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Engine implements AutoCloseable {
    private static final Charset A_UTF8 = StandardCharsets.UTF_8;
    private final static int ADDR = 0x00;
    private static final byte[] COMPONENT_ID = "INT*570261221315646M13*570HM2U3545277**".getBytes(A_UTF8);
    private static final byte[] DISTANCE = as4Bytes(256345 * 8); // km
    /*
     * Calibration ID must be 16 bytes
     */
    private static final byte[] ENGINE_CAL_ID1 = "PBT5MPR3        ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CVN1 = as4Bytes(0x40DCBF96);

    private static final byte[] ENGINE_HOURS = as4Bytes(3564 * 20); // hrs
    private static final byte[] ENGINE_SPEED = as2Bytes(1400 * 8); // rpm
    private static final byte[] ENGINE_SPEED_ZERO = as2Bytes(0); // rpm
    private static final byte NA = (byte) 0xFF;
    private static final byte[] NA3 = new byte[] { NA, NA, NA };
    private static final byte[] NA4 = new byte[] { NA, NA, NA, NA };
    private static final byte[] NA8 = new byte[] { NA, NA, NA, NA, NA, NA, NA, NA };

    /*
     * VIN can be any length up to 200 bytes, but should end with *
     */
    private static final byte[] VIN = "3HAMKSTN0FL575012*".getBytes(A_UTF8);
    private final Boolean[] engineOn = { false };
    private final Boolean[] keyOn = { false };
    private final Sim sim;
    private boolean dtcsCleared = false;

    private final List<DiagnosticTroubleCode> activeDTCs = new ArrayList<>();
    private final List<DiagnosticTroubleCode> previouslyActiveDTCs = new ArrayList<>();
    private final List<DiagnosticTroubleCode> pendingActiveDTCs = new ArrayList<>();
    private final List<DiagnosticTroubleCode> permanentActiveDTCs = new ArrayList<>();
    private final int ignitionCycles = 0;
    private final int obdConditions = 0;
    private final long millisecondsSCC = 0;
    private final int warmUpsSCC = 0;

    public Engine(Bus bus) throws BusException {
        sim = new Sim(bus);

        // xmsn rate is actually engine speed dependent
        sim.schedule(100,
                     100,
                     TimeUnit.MILLISECONDS,
                     () -> {
                         if (!isKeyOn()) {
                             return Packet.create(0x1FFFF, ADDR, NA8);
                         } else {
                             return Packet.create(61444,
                                                  ADDR,
                                                  combine(NA3, isEngineOn() ? ENGINE_SPEED : ENGINE_SPEED_ZERO, NA3));
                         }
                     });

        sim.schedule(100, 100, TimeUnit.MILLISECONDS, () -> Packet.create(65248, ADDR, combine(NA4, DISTANCE)));

        sim.schedule(50,
                     50,
                     TimeUnit.MILLISECONDS,
                     () -> Packet.create(0x0C, 0xF00A, ADDR, false, combine(as4Bytes(0), NA4)));

        sim.response(p -> isRequestFor(65259, p), () -> Packet.create(65259, ADDR, COMPONENT_ID));

        sim.response(p -> isRequestFor(0x1FFFF, p), p -> {
            setKeyState(0x1FFFF);
            return Packet.create(0x1FFFF, ADDR, getKeyStateAsBytes());
        });

        sim.response(p -> isRequestFor(0x1FFFE, p), () -> {
            setKeyState(0x1FFFE);
            return Packet.create(0x1FFFE, ADDR, getKeyStateAsBytes());
        });

        sim.response(p -> isRequestFor(0x1FFFC, p), () -> {
            setKeyState(0x1FFFC);
            return Packet.create(0x1FFFC, ADDR, getKeyStateAsBytes());
        });

        // 65278, Auxiliary Water Pump Pressure, AWPP, 1 s, 1, 73, Auxiliary Pump Pressure,4 9
        sim.response(p -> isRequestFor(65278, p), () -> Packet.create(65278, ADDR, NA8));

        sim.response(p -> isRequestFor(65253, p),
                     () -> {
                         // Start a timer that will increment the numerators and
                         // denominators for UI demo purposes startTimer();
                         return Packet.create(65253, ADDR, combine(ENGINE_HOURS, NA4));
                     });

        // Address Claim
        sim.response(p -> isRequestFor(0xEE00, p),
                     () -> Packet.create(0xEEFF, ADDR, 0x00, 0x00, 0x40, 0x05, 0x00, 0x00, 0x65, 0x14));

        sim.response(p -> isRequestFor(65260, p), () -> Packet.create(65260, ADDR, VIN));

        // DM1
        sim.schedule(1,
                     1,
                     SECONDS,
                     () -> DM1ActiveDTCsPacket.create(ADDR,
                                                      OFF,
                                                      OFF,
                                                      OFF,
                                                      OFF,
                                                      activeDTCs.toArray(new DiagnosticTroubleCode[0])));

        // DM 2
        sim.response(p -> isRequestFor(DM2PreviouslyActiveDTC.PGN, p),
                     () -> {
                         return DM2PreviouslyActiveDTC.create(ADDR,
                                                              OFF,
                                                              OFF,
                                                              OFF,
                                                              OFF,
                                                              previouslyActiveDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                      .getPacket();
                     });

        // DM5
        sim.response(p -> isRequestFor(DM5DiagnosticReadinessPacket.PGN, p),
                     () -> {
                         var supportedSystems = List.of(CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                                        CompositeSystem.EGR_VVT_SYSTEM,
                                                        CompositeSystem.EXHAUST_GAS_SENSOR,
                                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                                        CompositeSystem.FUEL_SYSTEM,
                                                        CompositeSystem.MISFIRE,
                                                        CompositeSystem.NMHC_CONVERTING_CATALYST,
                                                        CompositeSystem.NOX_CATALYST_ABSORBER);

                         var completeSystems = List.of(CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                                       CompositeSystem.CATALYST,
                                                       CompositeSystem.COLD_START_AID_SYSTEM,
                                                       CompositeSystem.COMPREHENSIVE_COMPONENT,
                                                       CompositeSystem.EVAPORATIVE_SYSTEM,
                                                       CompositeSystem.HEATED_CATALYST,
                                                       CompositeSystem.SECONDARY_AIR_SYSTEM);

                         return DM5DiagnosticReadinessPacket.create(ADDR,
                                                                    activeDTCs.size(),
                                                                    previouslyActiveDTCs.size(),
                                                                    0x14,
                                                                    supportedSystems,
                                                                    completeSystems)
                                                            .getPacket();
                     });

        // DM6
        sim.response(p -> isRequestFor(65231, p),
                     () -> DM6PendingEmissionDTCPacket.create(0,
                                                              ON,
                                                              OFF,
                                                              ON,
                                                              ON,
                                                              DiagnosticTroubleCode.create(123, 12, 0, 1))
                                                      .getPacket());

        // DM11
        sim.response(p -> isRequestFor(DM11ClearActiveDTCsPacket.PGN, p), p -> {
            dtcsCleared = true;
            return Packet.create(0, ADDR, NA8); // Don't return anything
        });

        // DM12
        sim.response(p -> isRequestFor(DM12MILOnEmissionDTCPacket.PGN, p),
                     () -> DM12MILOnEmissionDTCPacket.create(0,
                                                             ON,
                                                             OFF,
                                                             ON,
                                                             ON,
                                                             DiagnosticTroubleCode.create(123, 12, 0, 1))
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
                                                                         1,
                                                                         1,
                                                                         new PerformanceRatio(5322, 0, 0, 0),
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
                     p -> Packet.create(DM21DiagnosticReadinessPacket.PGN | p.getSource(),
                                        ADDR,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x00,
                                        dtcsCleared ? 0x00 : 0x10,
                                        0x00));

        // DM23
        sim.response(p -> isRequestFor(64949, p), () -> Packet.create(64949, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));

        // DM24 supported SPNs
        sim.response(p -> isRequestFor(DM24SPNSupportPacket.PGN, p),
                     () -> {
                         return DM24SPNSupportPacket.create(ADDR,
                                                            SupportedSPN.create(27, false, true, false, 1),
                                                            SupportedSPN.create(84, false, true, false, 1),
                                                            SupportedSPN.create(91, false, true, false, 1),
                                                            SupportedSPN.create(92, false, true, true, 1),
                                                            SupportedSPN.create(94, false, true, false, 1),
                                                            SupportedSPN.create(102, true, false, false, 1),
                                                            SupportedSPN.create(108, false, true, false, 1),
                                                            SupportedSPN.create(110, false, true, false, 1),
                                                            SupportedSPN.create(157, true, false, false, 1),
                                                            SupportedSPN.create(158, false, true, false, 1),
                                                            SupportedSPN.create(183, false, true, false, 1),
                                                            SupportedSPN.create(190, false, true, true, 1),
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
                                                            SupportedSPN.create(3301, false, false, true, 1),
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

        // @formatter:off
        // DM25
        sim.response(p -> isRequestFor(DM25ExpandedFreezeFrame.PGN, p),
                     () -> Packet.create(DM25ExpandedFreezeFrame.PGN,
                                         ADDR,
                                         0x56, 0x9D, 0x00, 0x07, 0x7F, 0x00, 0x01, 0x7B,
                                         0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                                         0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                                         0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                                         0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                                         0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                                         0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                                         0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                                         0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                                         0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                                         0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA));
        // @formatter:on

        // DM26
        sim.response(p -> isRequestFor(DM26TripDiagnosticReadinessPacket.PGN, p),
                     () -> {
                         var enabledSystems = List.of(CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                                      CompositeSystem.COMPREHENSIVE_COMPONENT,
                                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                                      CompositeSystem.EGR_VVT_SYSTEM,
                                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                                      CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                                      CompositeSystem.FUEL_SYSTEM,
                                                      CompositeSystem.MISFIRE,
                                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                                      CompositeSystem.NOX_CATALYST_ABSORBER);

                         var completeSystems = List.of(CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                                       CompositeSystem.CATALYST,
                                                       CompositeSystem.COLD_START_AID_SYSTEM,
                                                       CompositeSystem.COMPREHENSIVE_COMPONENT,
                                                       CompositeSystem.EVAPORATIVE_SYSTEM,
                                                       CompositeSystem.HEATED_CATALYST,
                                                       CompositeSystem.SECONDARY_AIR_SYSTEM);

                         return DM26TripDiagnosticReadinessPacket.create(ADDR,
                                                                         (int) (millisecondsSCC / 1000),
                                                                         warmUpsSCC,
                                                                         enabledSystems,
                                                                         completeSystems)
                                                                 .getPacket();
                     });

        // DM27
        sim.response(p -> isRequestFor(DM27AllPendingDTCsPacket.PGN, p),
                     () -> Packet.create(DM27AllPendingDTCsPacket.PGN,
                                         ADDR,
                                         0x03,
                                         0xFF,
                                         0x66,
                                         0x00,
                                         0x04,
                                         0x01,
                                         0xFF,
                                         0xFF));

        // DM28
        sim.response(p -> isRequestFor(DM28PermanentEmissionDTCPacket.PGN, p),
                     () -> Packet.create(DM28PermanentEmissionDTCPacket.PGN,
                                         ADDR,
                                         0x03,
                                         0xFF,
                                         0x00,
                                         0x00,
                                         0x00,
                                         0x00,
                                         0xFF,
                                         0xFF));

        // DM29 response
        sim.response(p -> isRequestFor(DM29DtcCounts.PGN, p),
                     p -> Packet.create(DM29DtcCounts.PGN | p.getSource(),
                                        ADDR,
                                        0x00,
                                        0x00,
                                        0x01,
                                        0x00,
                                        0x01,
                                        0xFF,
                                        0xFF,
                                        0xFF));

        // DM30 response for DM7 Request
        sim.response(Engine::isRequestForDM30, p -> {
            var results = new ArrayList<ScaledTestResult>();

            var dm7 = new DM7CommandTestsPacket(p);
            int testId = dm7.getTestId();
            int spn = dm7.getSpn();
            int fmi = dm7.getFmi();

            if (testId == 247 && fmi == 31) {
                if (spn == 157) {
                    results.add(ScaledTestResult.create(testId, spn, 18, 385, 0, 0, 0));
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
            } else {
                results.add(ScaledTestResult.create(testId, spn, fmi, 385, 0, 0, 0));
            }

            return DM30ScaledTestResultsPacket.create(ADDR, results.toArray(new ScaledTestResult[0])).getPacket();
        });

        // DM31 response
        sim.response(p -> isRequestFor(DM31DtcToLampAssociation.PGN, p),
                     () -> Packet.create(DM31DtcToLampAssociation.PGN | 0xFF,
                                         ADDR,
                                         0x61,
                                         0x02,
                                         0x13,
                                         0x81,
                                         0x62,
                                         0x1D));

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

        // @formatter:off
        sim.response(p -> isRequestFor(DM33EmissionIncreasingAECDActiveTime.PGN, p),
                     p -> Packet
                             .create(DM33EmissionIncreasingAECDActiveTime.PGN | p.getSource(), 
                                     ADDR,
                                     0x01,
                                     0x00, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF,
                                     0x04,
                                     0x00, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF,
                                     0x06,
                                     0x00, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF,
                                     0x0B,
                                     0x00, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF,
                                     0x0C,
                                     0x00, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF,
                                     0x0D,
                                     0x00, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF,
                                     0x31,
                                     0x01, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF,
                                     0x38,
                                     0x0D, 0x00, 0x00, 0x00,
                                     0xFF, 0xFF, 0xFF, 0xFF
                             ));
        // @formatter:on

        // DM56 Engine Model Year
        sim.response(p -> isRequestFor(DM56EngineFamilyPacket.PGN, p),
                     DM56EngineFamilyPacket.create(ADDR, 2015, true, "US HD OBD    ")::getPacket);
    }

    private static byte[] as2Bytes(int a) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (a & 0xFF);
        ret[1] = (byte) ((a >> 8) & 0xFF);
        return ret;
    }

    private static byte[] as4Bytes(int a) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (a & 0xFF);
        ret[1] = (byte) ((a >> 8) & 0xFF);
        ret[2] = (byte) ((a >> 16) & 0xFF);
        ret[3] = (byte) ((a >> 24) & 0xFF);
        return ret;
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

    private void setKeyState(int pgn) {
        if (pgn == 0x1FFFF) {
            J1939_84.getLogger().log(Level.INFO, "to Key On, Engine Running");
            engineOn[0] = true;
            keyOn[0] = true;
        } else if (pgn == 0x1FFFE) {
            J1939_84.getLogger().log(Level.INFO, "to Key ON/Engine OFF");
            engineOn[0] = false;
            keyOn[0] = true;
        } else if (pgn == 0x1FFFC) {
            J1939_84.getLogger().log(Level.INFO, "to Key Off");
            engineOn[0] = false;
            keyOn[0] = false;
        }
    }

    private boolean isEngineOn() {
        return engineOn[0];
    }

    private boolean isKeyOn() {
        return keyOn[0];
    }

    private byte[] getKeyStateAsBytes() {
        return combine(as4Bytes(isKeyOn() ? 1 : 0), as4Bytes(isEngineOn() ? 1 : 0));
    }

}
