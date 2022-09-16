/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.engine.simulated;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.COMPREHENSIVE_COMPONENT;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EVAPORATIVE_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR_HEATER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.FUEL_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.MISFIRE;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.NOX_CATALYST_ABSORBER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_ACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_REQ;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_REQ;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByteSpecificIndicator.GENERAL_NACK;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.OUTSIDE;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939tools.j1939.packets.ParsedPacket.to2Bytes;
import static org.etools.j1939tools.j1939.packets.ParsedPacket.to4Bytes;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.etools.j1939_84.model.KeyState;
import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.model.SpnFmi;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.etools.j1939tools.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939tools.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939tools.j1939.packets.DM34NTEStatus;
import org.etools.j1939tools.j1939.packets.DM3DiagnosticDataClearPacket;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939tools.j1939.packets.DTCLampStatus;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.EngineHoursPacket;
import org.etools.j1939tools.j1939.packets.EngineSpeedPacket;
import org.etools.j1939tools.j1939.packets.FreezeFrame;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;

/**
 * Simulated Engine used for System Testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Engine implements AutoCloseable {
    // TODO Move this back
    private static final Charset A_UTF8 = StandardCharsets.UTF_8;
    private final static int ADDR = 0x00;
    private static final int BUS_ADDR = 0xA5;
    private static final byte[] COMPONENT_ID = "INT*570261221315646M13*570HM2U3545277**".getBytes(A_UTF8);
    private static final byte[] DISTANCE = to4Bytes(256345L * 8); // km

    /*
     * Calibration ID must be 16 bytes
     */
    private static final byte[] ENGINE_CAL_ID1 = "PBT5MPR3        ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CVN1 = to4Bytes(0x40DCBF96L);

    private static final byte[] ENGINE_SPEED = to2Bytes(1400 * 8); // rpm
    private static final byte[] ENGINE_SPEED_IDLE = to2Bytes(600 * 8); // rpm
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

    private int ignitionCycles = 1;
    private int obdConditions = 0;
    private int secondsSCC = 100000;
    private int secondsWithMIL = 0;
    private int warmUpsSCC = 132948;
    private int secondsRunning = 0;
    private int ignitionCycleSecondsRunning = 0;
    private boolean warmedUp = false;

    private final List<CompositeSystem> completedDM5Systems = new ArrayList<>(List.of(AC_SYSTEM_REFRIGERANT,
                                                                                      CATALYST,
                                                                                      COLD_START_AID_SYSTEM,
                                                                                      COMPREHENSIVE_COMPONENT,
                                                                                      EVAPORATIVE_SYSTEM,
                                                                                      HEATED_CATALYST,
                                                                                      SECONDARY_AIR_SYSTEM));
    private DiagnosticTroubleCode nextFault;

    public Engine(Bus bus) throws BusException {
        sim = new Sim(bus, false);

        // One second timer to create ticks
        sim.schedule(1, SECONDS, () -> {
            if (isEngineOn()) {
                secondsRunning++;
                secondsSCC++;
                if (getMilStatus() == ON) {
                    secondsWithMIL++;
                }

                ignitionCycleSecondsRunning++;
                if (ignitionCycleSecondsRunning > 5 && !warmedUp) {
                    warmedUp = true;
                    warmUpsSCC++;
                }

                if (secondsRunning > 180 && obdConditions == 0) {
                    obdConditions++;
                }
            }
        });

        // Listeners for key state change
        sim.response(p -> isRequestFor(0x1FFFF, p), p -> {
            setKeyState(KEY_ON_ENGINE_RUNNING);
            return Packet.create(0x1FFFF, ADDR, getKeyStateAsBytes());
        });

        sim.response(p -> isRequestFor(0x1FFFE, p), () -> {
            setKeyState(KeyState.KEY_ON_ENGINE_OFF);
            return Packet.create(0x1FFFE, ADDR, getKeyStateAsBytes());
        });

        sim.response(p -> isRequestFor(0x1FFFC, p), () -> {
            setKeyState(KEY_OFF);
            return Packet.create(0x1FFFC, ADDR, getKeyStateAsBytes());
        });

        // Listeners to implant faults
        sim.response(p -> isRequestFor(0x1FFFA, p), () -> {
            nextFault = DiagnosticTroubleCode.create(0xFA, 0x0A, 0, 1);
            return Packet.create(0x1FFFA, ADDR, NA8);
        });

        sim.response(p -> isRequestFor(0x1FFFB, p), () -> {
            nextFault = DiagnosticTroubleCode.create(0xFB, 0x0B, 0, 1);
            return Packet.create(0x1FFFB, ADDR, NA8);
        });

        // BCT PGN 61444 from Engine #1 (0) with SPNs 190, 512, 513
        // xmsn rate is actually engine speed dependent
        sim.schedule(50,
                     MILLISECONDS,
                     () -> {
                         if (!isKeyOn()) {
                             // Rather then stopping the schedule, just send a different message
                             return Packet.create(0x1FFFF, ADDR, NA8);
                         } else {
                             byte[] engineSpeed;
                             if (isEngineOn()) {
                                 if (secondsRunning > 330 + 180
                                         && secondsRunning < 330 + 180 + 330) {
                                     engineSpeed = ENGINE_SPEED;
                                 } else {
                                     engineSpeed = ENGINE_SPEED_IDLE;
                                 }
                             } else {
                                 engineSpeed = ENGINE_SPEED_ZERO;
                             }
                             return Packet.create(EngineSpeedPacket.PGN,
                                                  ADDR,
                                                  combine(new byte[] { NA, (byte) 0xFF, (byte) 0xFF }, engineSpeed, NA3));
                         }
                     });

        sim.schedule(100, MILLISECONDS, () -> Packet.create(65248, ADDR, combine(NA4, DISTANCE)));

        sim.response(p -> isRequestFor(65259, p), () -> Packet.create(65259, ADDR, COMPONENT_ID));

        sim.response(p -> isRequestFor(EngineHoursPacket.PGN, p),
                     EngineHoursPacket.create(ADDR, secondsRunning)::getPacket);

        // Address Claim
        sim.response(p -> isRequestFor(0xEE00, p),
                     p -> Packet.create(0xEEFF, ADDR, 0x00, 0x00, 0x40, 0x05, 0x00, 0x00, 0x65, 0x14));

        sim.response(p -> isRequestFor(VehicleIdentificationPacket.PGN, p),
                     p -> Packet.create(VehicleIdentificationPacket.PGN, ADDR, VIN));

        // DM1
        sim.schedule(1,
                     SECONDS,
                     () -> DM1ActiveDTCsPacket.create(ADDR,
                                                      getMilStatus(),
                                                      OFF,
                                                      OFF,
                                                      OFF,
                                                      activeDTCs.toArray(new DiagnosticTroubleCode[0]))
                                              .getPacket());

        // DM2
        sim.response(p -> isRequestFor(DM2PreviouslyActiveDTC.PGN, p),
                     p -> DM2PreviouslyActiveDTC.create(ADDR,
                                                        getMilStatus(),
                                                        OFF,
                                                        OFF,
                                                        OFF,
                                                        previousDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                .getPacket());

        // DM3
        sim.response(p -> isRequestFor(DM3DiagnosticDataClearPacket.PGN, p),
                     p -> AcknowledgmentPacket.create(ADDR,
                                                      NACK,
                                                      0,
                                                      p.getSource(),
                                                      DM3DiagnosticDataClearPacket.PGN)
                                              .getPacket());

        // DM5
        sim.response(p -> isRequestFor(DM5DiagnosticReadinessPacket.PGN, p),
                     p -> DM5DiagnosticReadinessPacket.create(ADDR,
                                                              activeDTCs.size(),
                                                              previousDTCs.size(),
                                                              0x14,
                                                              getEnabledSystems(),
                                                              completedDM5Systems)
                                                      .getPacket());

        // DM6
        sim.response(p -> isRequestFor(DM6PendingEmissionDTCPacket.PGN, p),
                     p -> DM6PendingEmissionDTCPacket.create(ADDR,
                                                             getMilStatus(),
                                                             OFF,
                                                             OFF,
                                                             OFF,
                                                             pendingDTCs.toArray(new DiagnosticTroubleCode[0]))
                                                     .getPacket());

        // DM11
        sim.response(p -> isRequestFor(DM11ClearActiveDTCsPacket.PGN, p),
                     p -> {
                         new Timer().schedule(new TimerTask() {
                             @Override
                             public void run() {
                                 sim.sendNow(AcknowledgmentPacket.create(ADDR,
                                                    NACK,
                                                    0,
                                                    p.getSource(),
                                                    DM11ClearActiveDTCsPacket.PGN).getPacket());
                             }
                         }, 4800);

                         return AcknowledgmentPacket.create(0x85,
                                       ACK,
                                       0,
                                       p.getSource(),
                                       DM11ClearActiveDTCsPacket.PGN).getPacket();
                     });

        // DM11 Global Request
        sim.response(p -> (p.getId(0xFFFF) == 0xEAFF) && p.get24(0) == DM11ClearActiveDTCsPacket.PGN,
                     p -> {
                         activeDTCs.clear(); // DM1 & DM12
                         previousDTCs.clear(); // DM2 & DM23
                         secondsWithMIL = 0;
                         secondsSCC = 0;
                         warmUpsSCC = 0;

                         return Packet.create(0, ADDR, NA8); // Don't return anything
                     });

        // DM11 DS Request
        sim.response(p -> p.getId(0xFFFF) == (0xEA00 | Engine.ADDR) && p.get24(0) == DM11ClearActiveDTCsPacket.PGN,
                     p -> AcknowledgmentPacket.create(ADDR, NACK, 0, p.getSource(), DM11ClearActiveDTCsPacket.PGN)
                                              .getPacket());

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
                     p -> DM20MonitorPerformanceRatioPacket.create(ADDR,
                                                                   p.getSource(),
                                                                   ignitionCycles,
                                                                   obdConditions,
                                                                   new PerformanceRatio(5322,
                                                                                        1,
                                                                                        obdConditions,
                                                                                        0),
                                                                   new PerformanceRatio(5318,
                                                                                        1,
                                                                                        obdConditions,
                                                                                        0),
                                                                   new PerformanceRatio(3058,
                                                                                        1,
                                                                                        obdConditions,
                                                                                        0),
                                                                   new PerformanceRatio(3064,
                                                                                        1,
                                                                                        obdConditions,
                                                                                        0),
                                                                   new PerformanceRatio(5321,
                                                                                        1,
                                                                                        obdConditions,
                                                                                        0),
                                                                   new PerformanceRatio(3055,
                                                                                        1,
                                                                                        obdConditions,
                                                                                        0),
                                                                   new PerformanceRatio(4792,
                                                                                        1,
                                                                                        obdConditions,
                                                                                        0))
                                                           .getPacket());

        // DM21
        sim.response(p -> isRequestFor(DM21DiagnosticReadinessPacket.PGN, p),
                     p -> DM21DiagnosticReadinessPacket.create(ADDR,
                                                               p.getSource(),
                                                               0,
                                                               0,
                                                               (int) getMinutesWithMil(),
                                                               (int) getMinutesSCC())
                                                       .getPacket());

        // DM22
        sim.response(p -> p.getPgn() == DM22IndividualClearPacket.PGN && p.getDestination() == ADDR,
                     p -> {
                         DM22IndividualClearPacket dm22 = new DM22IndividualClearPacket(p);
                         DM22IndividualClearPacket.ControlByte controlByte = CLR_ACT_ACK;
                         if (dm22.getControlByte() == CLR_ACT_REQ) {
                             controlByte = CLR_ACT_NACK;
                         } else if (dm22.getControlByte() == CLR_PA_REQ) {
                             controlByte = CLR_PA_NACK;
                         }
                         return DM22IndividualClearPacket.create(ADDR,
                                                                 p.getSource(),
                                                                 controlByte,
                                                                 GENERAL_NACK,
                                                                 dm22.getSpn(),
                                                                 dm22.getFmi())
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
                     p -> DM24SPNSupportPacket.create(ADDR,
                                                      SupportedSPN.create(27, false, true, false, false, 1),
                                                      SupportedSPN.create(84, false, true, false, false, 1),
                                                      SupportedSPN.create(91, false, true, false, false, 1),
                                                      SupportedSPN.create(92, false, true, true, false, 1),
                                                      SupportedSPN.create(94, false, true, false, false, 1),
                                                      SupportedSPN.create(102, true, true, false, false, 1),
                                                      SupportedSPN.create(108, false, true, false, false, 1),
                                                      SupportedSPN.create(110, false, true, true, false, 1),
                                                      SupportedSPN.create(157, true, false, false, false, 1),
                                                      SupportedSPN.create(158, false, true, false, false, 1),
                                                      SupportedSPN.create(183, false, true, false, false, 1),
                                                      SupportedSPN.create(190, false, true, true, true, 2),
                                                      SupportedSPN.create(235, false, true, false, false, 1),
                                                      SupportedSPN.create(247, false, true, false, false, 1),
                                                      SupportedSPN.create(248, false, true, false, false, 1),
                                                      SupportedSPN.create(512, false, true, true, false, 1),
                                                      SupportedSPN.create(513, false, true, true, false, 1),
                                                      SupportedSPN.create(514, false, true, false, false, 1),
                                                      SupportedSPN.create(528, false, false, false, true, 0),
                                                      SupportedSPN.create(529, false, false, true, true, 2),
                                                      SupportedSPN.create(530, false, true, false, true, 0),
                                                      SupportedSPN.create(531, false, true, true, true, 1),
                                                      SupportedSPN.create(532, true, false, false, true, 4),
                                                      SupportedSPN.create(533, true, false, true, true, 2),
                                                      SupportedSPN.create(534, true, true, false, true, 0),
                                                      SupportedSPN.create(535, true, true, true, true, 1),
                                                      SupportedSPN.create(536, false, false, false, true, 0),
                                                      SupportedSPN.create(537, false, false, true, true, 0),
                                                      SupportedSPN.create(538, false, true, false, true, 1),
                                                      SupportedSPN.create(539, false, true, false, true, 1),
                                                      SupportedSPN.create(540, false, true, false, true, 1),
                                                      SupportedSPN.create(541, false, true, false, true, 1),
                                                      SupportedSPN.create(542, false, true, false, true, 1),
                                                      SupportedSPN.create(543, false, true, false, false, 1),
                                                      SupportedSPN.create(544, false, true, false, false, 1),
                                                      SupportedSPN.create(651, true, false, false, false, 1),
                                                      SupportedSPN.create(1323, true, false, false, false, 1),
                                                      SupportedSPN.create(1324, true, false, false, false, 1),
                                                      SupportedSPN.create(1325, true, false, false, false, 1),
                                                      SupportedSPN.create(1326, true, false, false, false, 1),
                                                      SupportedSPN.create(1413, false, true, false, false, 1),
                                                      SupportedSPN.create(1634, false, true, false, false, 15),
                                                      SupportedSPN.create(1635, false, true, false, false, 4),
                                                      SupportedSPN.create(2630, true, false, false, false, 1),
                                                      SupportedSPN.create(2791, false, true, false, false, 1),
                                                      SupportedSPN.create(2978, false, true, false, false, 1),
                                                      SupportedSPN.create(3031, false, true, false, false, 1),
                                                      SupportedSPN.create(3058, true, false, false, false, 1),
                                                      SupportedSPN.create(3226, true, true, false, false, 1),
                                                      SupportedSPN.create(3251, true, false, false, false, 1),
                                                      SupportedSPN.create(3301, false, false, true, true, 2),
                                                      SupportedSPN.create(3361, true, false, false, false, 1),
                                                      SupportedSPN.create(3516, false, true, false, false, 1),
                                                      SupportedSPN.create(3609, false, true, false, false, 1),
                                                      SupportedSPN.create(3700, false, true, false, false, 1),
                                                      SupportedSPN.create(3713, true, false, false, false, 1),
                                                      SupportedSPN.create(4364, true, false, false, false, 1),
                                                      SupportedSPN.create(4752, true, false, false, false, 1),
                                                      SupportedSPN.create(5018, true, false, false, false, 1),
                                                      SupportedSPN.create(5466, false, true, false, false, 1),
                                                      SupportedSPN.create(5827, false, true, false, false, 1),
                                                      SupportedSPN.create(5829, false, true, false, false, 1),
                                                      SupportedSPN.create(5837, false, true, false, false, 1),
                                                      SupportedSPN.create(6895, false, true, false, false, 1),
                                                      SupportedSPN.create(7333, false, true, false, false, 1),
                                                      SupportedSPN.create(12675, false, true, false, false, 1),
                                                      SupportedSPN.create(12691, false, true, false, false, 1),
                                                      SupportedSPN.create(12730, false, true, false, false, 1),
                                                      SupportedSPN.create(12797, false, true, false, false, 1),
                                                      SupportedSPN.create(12783, false, true, false, false, 1))
                                              .getPacket());

        // DM25
        sim.response(p -> isRequestFor(DM25ExpandedFreezeFrame.PGN, p),
                     p -> {
                         var dtcs = new HashSet<DiagnosticTroubleCode>();

                         if (!pendingDTCs.isEmpty()) {
                             dtcs.add(pendingDTCs.get(0));
                         }

                         if (!activeDTCs.isEmpty()) {
                             dtcs.add(activeDTCs.get(0));
                         }

                         if (!previousDTCs.isEmpty()) {
                             dtcs.add(previousDTCs.get(0));
                         }

                         var freezeFrames = dtcs.stream()
                                                .map(dtc -> {
                                                    int[] data = { 0x80, 0x7F, 0x11, 0x22, 0x80, 0x80, 0x80, 0x80 };
                                                    return new FreezeFrame(dtc, data);
                                                })
                                                .toArray(FreezeFrame[]::new);

                         var dm25 = DM25ExpandedFreezeFrame.create(ADDR, freezeFrames);
                         return dm25.getPacket();
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

        // DM29
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
                    results.add(ScaledTestResult.create(testId, spn, 18, 385, secondsSCC, 0, 0));
                    results.add(ScaledTestResult.create(testId, spn, 16, 385, secondsSCC, 0, 0));
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

        // DM31
        sim.response(p -> isRequestFor(DM31DtcToLampAssociation.PGN, p),
                     p -> {
                         List<DTCLampStatus> lampStatuses = new ArrayList<>();

                         if (!activeDTCs.isEmpty()) {
                             lampStatuses.add(DTCLampStatus.create(activeDTCs.get(0), OFF, getMilStatus(), OFF, OFF));
                         }

                         if (!previousDTCs.isEmpty() && getMilStatus() == ON) {
                             lampStatuses.add(DTCLampStatus.create(previousDTCs.get(0), OFF, OFF, OFF, OFF));
                         }

                         return DM31DtcToLampAssociation.create(ADDR,
                                                                p.getSource(),
                                                                lampStatuses.toArray(new DTCLampStatus[0]))
                                                        .getPacket();
                     });

        // @formatter:off
        // DM33
        sim.response(p -> isRequestFor(DM33EmissionIncreasingAECDActiveTime.PGN, p),
                     p -> Packet.create(DM33EmissionIncreasingAECDActiveTime.PGN | p.getSource(),
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
                     DM56EngineFamilyPacket.create(ADDR, 2022, true, "US HD OBD    ")::getPacket);

        sim.response(p -> isRequestFor(0xFB02, p),
                     () -> Packet.create(0xFB02, ADDR,
                     // @formatter:off
                                         0x40, 0x84, 0x00, 0x10, 0x41, 0x84, 0x00, 0x10,
                                         0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                         0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                         0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                         0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                         0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                         0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                         0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                         0x5F, 0x84, 0x00, 0x10));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB03, p),
                     () -> Packet.create(0xFB03, ADDR,
                     // @formatter:off
                                         0x60, 0x84, 0x00, 0x10, 0x61, 0x84, 0x00, 0x10,
                                         0x63, 0x84, 0x00, 0x10, 0x65, 0x84, 0x00, 0x10,
                                         0x67, 0x84, 0x00, 0x10, 0x69, 0x84, 0x00, 0x10,
                                         0x6B, 0x84, 0x00, 0x10, 0x6D, 0x84, 0x00, 0x10,
                                         0x6F, 0x84, 0x00, 0x10, 0x71, 0x84, 0x00, 0x10,
                                         0x73, 0x84, 0x00, 0x10, 0x75, 0x84, 0x00, 0x10,
                                         0x77, 0x84, 0x00, 0x10, 0x79, 0x84, 0x00, 0x10,
                                         0x7B, 0x84, 0x00, 0x10, 0x7D, 0x84, 0x00, 0x10,
                                         0x7F, 0x84, 0x08, 0x10));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB04, p),
                     () -> Packet.create(0xFB04, ADDR,
                     // @formatter:off
                                         0x80, 0x84, 0x00, 0x10, 0x81, 0x84, 0x00, 0x10,
                                         0x83, 0x84, 0x00, 0x10, 0x85, 0x84, 0x00, 0x10,
                                         0x87, 0x84, 0x00, 0x10, 0x89, 0x84, 0x00, 0x10,
                                         0x8B, 0x84, 0x00, 0x10, 0x8D, 0x84, 0x00, 0x10,
                                         0x8F, 0x84, 0x00, 0x10, 0x91, 0x84, 0x00, 0x10,
                                         0x93, 0x84, 0x00, 0x10, 0x95, 0x84, 0x00, 0x10,
                                         0x97, 0x84, 0x00, 0x10, 0x99, 0x84, 0x00, 0x10,
                                         0x9B, 0x84, 0x00, 0x10, 0x9D, 0x84, 0x00, 0x10,
                                         0x9F, 0x84, 0x00, 0x10));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB05, p),
                     () -> Packet.create(0xFB05, ADDR,
                     // @formatter:off
                                         0xA0, 0x84, 0x00, 0x10, 0xA1, 0x84, 0x00, 0x10,
                                         0xA3, 0x84, 0x00, 0x10, 0xA5, 0x84, 0x00, 0x10,
                                         0xA7, 0x84, 0x00, 0x10, 0xA9, 0x84, 0x00, 0x10,
                                         0xAB, 0x84, 0x00, 0x10, 0xAD, 0x84, 0x00, 0x10,
                                         0xAF, 0x84, 0x00, 0x10, 0xB1, 0x84, 0x00, 0x10,
                                         0xB3, 0x84, 0x00, 0x10, 0xB5, 0x84, 0x00, 0x10,
                                         0xB7, 0x84, 0x00, 0x10, 0xB9, 0x84, 0x00, 0x10,
                                         0xBB, 0x84, 0x00, 0x10, 0xBD, 0x84, 0x00, 0x10,
                                         0xBF, 0x84, 0x00, 0x10));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB06, p),
                     () -> Packet.create(0xFB06, ADDR,
                     // @formatter:off
                                         0x00, 0x04, 0x00, 0x0D, 0x01, 0x04, 0x00, 0x0D,
                                         0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                         0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                         0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                         0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                         0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                         0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                         0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                         0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB07, p),
                     () -> Packet.create(0xFB07, ADDR,
                     // @formatter:off
                                         0x20, 0x04, 0x00, 0x0D, 0x21, 0x04, 0x00, 0x0D,
                                         0x23, 0x04, 0x00, 0x0D, 0x25, 0x04, 0x00, 0x0D,
                                         0x27, 0x04, 0x00, 0x0D, 0x29, 0x04, 0x00, 0x0D,
                                         0x2B, 0x04, 0x00, 0x0D, 0x2D, 0x04, 0x00, 0x0D,
                                         0x2F, 0x04, 0x00, 0x0D, 0x31, 0x04, 0x00, 0x0D,
                                         0x33, 0x04, 0x00, 0x0D, 0x35, 0x04, 0x00, 0x0D,
                                         0x37, 0x04, 0x00, 0x0D, 0x39, 0x04, 0x00, 0x0D,
                                         0x3B, 0x04, 0x00, 0x0D, 0x3D, 0x04, 0x00, 0x0D,
                                         0x3F, 0x04, 0x00, 0x0D));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB08, p),
                     () -> Packet.create(0xFB08, ADDR,
                     // @formatter:off
                                         0x40, 0x04, 0x00, 0x0D, 0x41, 0x04, 0x00, 0x0D,
                                         0x43, 0x04, 0x00, 0x0D, 0x45, 0x04, 0x00, 0x0D,
                                         0x47, 0x04, 0x00, 0x0D, 0x49, 0x04, 0x00, 0x0D,
                                         0x4B, 0x04, 0x00, 0x0D, 0x4D, 0x04, 0x00, 0x0D,
                                         0x4F, 0x04, 0x00, 0x0D, 0x51, 0x04, 0x00, 0x0D,
                                         0x53, 0x04, 0x00, 0x0D, 0x55, 0x04, 0x00, 0x0D,
                                         0x57, 0x04, 0x00, 0x0D, 0x59, 0x04, 0x00, 0x0D,
                                         0x5B, 0x04, 0x00, 0x0D, 0x5D, 0x04, 0x00, 0x0D,
                                         0x5F, 0x04, 0x00, 0x0D));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB09, p),

                     () -> Packet.create(0xFB09, ADDR,
                     // @formatter:off
                                         0x60, 0x04, 0x00, 0x0D, 0x61, 0x04, 0x00, 0x0D,
                                         0x63, 0x04, 0x00, 0x0D, 0x65, 0x04, 0x00, 0x0D,
                                         0x67, 0x04, 0x00, 0x0D, 0x69, 0x04, 0x00, 0x0D,
                                         0x6B, 0x04, 0x00, 0x0D, 0x6D, 0x04, 0x00, 0x0D,
                                         0x6F, 0x04, 0x00, 0x0D, 0x71, 0x04, 0x00, 0x0D,
                                         0x73, 0x04, 0x00, 0x0D, 0x75, 0x04, 0x00, 0x0D,
                                         0x77, 0x04, 0x00, 0x0D, 0x79, 0x04, 0x00, 0x0D,
                                         0x7B, 0x04, 0x00, 0x0D, 0x7D, 0x04, 0x00, 0x0D,
                                         0x7F, 0x04, 0x00, 0x0D));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB0A, p),
                     () -> Packet.create(0xFB0A, ADDR,
                     // @formatter:off
                                         0x80, 0x04, 0x00, 0x0D, 0x81, 0x04, 0x00, 0x0D,
                                         0x83, 0x04, 0x00, 0x0D, 0x85, 0x04, 0x00, 0x0D,
                                         0x87, 0x04, 0x00, 0x0D, 0x89, 0x04, 0x00, 0x0D,
                                         0x8B, 0x04, 0x00, 0x0D, 0x8D, 0x04, 0x00, 0x0D,
                                         0x8F, 0x04, 0x00, 0x0D, 0x91, 0x04, 0x00, 0x0D,
                                         0x93, 0x04, 0x00, 0x0D, 0x95, 0x04, 0x00, 0x0D,
                                         0x97, 0x04, 0x00, 0x0D, 0x99, 0x04, 0x00, 0x0D,
                                         0x9B, 0x04, 0x00, 0x0D, 0x9D, 0x04, 0x00, 0x0D,
                                         0x9F, 0x04, 0x00, 0x0D));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB0B, p),
                     () -> Packet.create(0xFB0B, ADDR,
                     // @formatter:off
                                         0xA0, 0x04, 0x00, 0x0D, 0xA1, 0x04, 0x00, 0x0D,
                                         0xA3, 0x04, 0x00, 0x0D, 0xA5, 0x04, 0x00, 0x0D,
                                         0xA7, 0x04, 0x00, 0x0D, 0xA9, 0x04, 0x00, 0x0D,
                                         0xAB, 0x04, 0x00, 0x0D, 0xAD, 0x04, 0x00, 0x0D,
                                         0xAF, 0x04, 0x00, 0x0D, 0xB1, 0x04, 0x00, 0x0D,
                                         0xB3, 0x04, 0x00, 0x0D, 0xB5, 0x04, 0x00, 0x0D,
                                         0xB7, 0x04, 0x00, 0x0D, 0xB9, 0x04, 0x00, 0x0D,
                                         0xBB, 0x04, 0x00, 0x0D, 0xBD, 0x04, 0x00, 0x0D,
                                         0xBF, 0x04, 0x00, 0x0D));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB0C, p),
                     () -> Packet.create(0xFB0C, ADDR,
                     // @formatter:off
                                         0x00, 0x84, 0x01, 0x84, 0x03, 0x84, 0x05, 0x84,
                                         0x07, 0x84, 0x09, 0x84, 0x0B, 0x84, 0x0D, 0x84,
                                         0x0F, 0x84, 0x11, 0x84, 0x13, 0x84, 0x15, 0x84,
                                         0x17, 0x84, 0x19, 0x84, 0x1B, 0x84, 0x1D, 0x84,
                                         0x1F, 0x84));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB0D, p),
                     () -> Packet.create(0xFB0D, ADDR,
                     // @formatter:off
                                         0x20, 0x84, 0x21, 0x84, 0x23, 0x84, 0x25, 0x84,
                                         0x27, 0x84, 0x29, 0x84, 0x2B, 0x84, 0x2D, 0x84,
                                         0x2F, 0x84, 0x31, 0x84, 0x33, 0x84, 0x35, 0x84,
                                         0x37, 0x84, 0x39, 0x84, 0x3B, 0x84, 0x3D, 0x84,
                                         0x3F, 0x84));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB0E, p),
                     () -> Packet.create(0xFB0E, ADDR,
                     // @formatter:off
                                         0x40, 0x84, 0x41, 0x84, 0x43, 0x84, 0x45, 0x84,
                                         0x47, 0x84, 0x49, 0x84, 0x4B, 0x84, 0x4D, 0x84,
                                         0x4F, 0x84, 0x51, 0x84, 0x53, 0x84, 0x55, 0x84,
                                         0x57, 0x84, 0x59, 0x84, 0x5B, 0x84, 0x5D, 0x84,
                                         0x5F, 0x84));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB0F, p),
                     () -> Packet.create(0xFB0F, ADDR,
                     // @formatter:off
                                         0x60, 0x84, 0x61, 0x84, 0x63, 0x84, 0x65, 0x84,
                                         0x67, 0x84, 0x69, 0x84, 0x6B, 0x84, 0x6D, 0x84,
                                         0x6F, 0x84, 0x71, 0x84, 0x73, 0x84, 0x75, 0x84,
                                         0x77, 0x84, 0x79, 0x84, 0x7B, 0x84, 0x7D, 0x84,
                                         0x7F, 0x84));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB10, p),
                     () -> Packet.create(0xFB10, ADDR,
                     // @formatter:off
                                         0x80, 0x84, 0x00, 0x00, 0x81, 0x84, 0x00, 0x00,
                                         0x83, 0x84, 0x00, 0x00, 0x85, 0x84, 0x00, 0x00,
                                         0x87, 0x84, 0x00, 0x00, 0x89, 0x84, 0x00, 0x00,
                                         0x8B, 0x84, 0x00, 0x00, 0x8D, 0x84, 0x00, 0x00,
                                         0x8F, 0x84, 0x00, 0x00, 0x91, 0x84, 0x00, 0x00,
                                         0x93, 0x84, 0x00, 0x00, 0x95, 0x84, 0x00, 0x00,
                                         0x97, 0x84, 0x00, 0x00, 0x99, 0x84, 0x00, 0x00,
                                         0x9B, 0x84, 0x00, 0x00, 0x9D, 0x84, 0x00, 0x00,
                                         0x9F, 0x84, 0x00, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB11, p),
                     () -> Packet.create(0xFB11, ADDR,
                     // @formatter:off
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB12, p),
                     () -> Packet.create(0xFB12, ADDR,
                     // @formatter:off
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                         0x00, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB13, p),
                     () -> Packet.create(0xFB13, ADDR,
                     // @formatter:off
                                         0x20, 0x04, 0x21, 0x04, 0x23, 0x04, 0x25, 0x04,
                                         0x27, 0x04, 0x29, 0x04, 0x2B, 0x04, 0x2D, 0x04,
                                         0x2F, 0x04, 0x31, 0x04, 0x33, 0x04, 0x35, 0x04,
                                         0x37, 0x04, 0x39, 0x04, 0x3B, 0x04, 0x3D, 0x04,
                                         0x3F, 0x04));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB14, p),
                     () -> Packet.create(0xFB14, ADDR,
                     // @formatter:off
                                         0x40, 0x04, 0x41, 0x04, 0x43, 0x04, 0x45, 0x04,
                                         0x47, 0x04, 0x49, 0x04, 0x4B, 0x04, 0x4D, 0x04,
                                         0x4F, 0x04, 0x51, 0x04, 0x53, 0x04, 0x55, 0x04,
                                         0x57, 0x04, 0x59, 0x04, 0x5B, 0x04, 0x5D, 0x04,
                                         0x5F, 0x04));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB15, p),
                     () -> Packet.create(0xFB15, ADDR,
                     // @formatter:off
                                         0x60, 0x04, 0x61, 0x04, 0x63, 0x04, 0x65, 0x04,
                                         0x67, 0x04, 0x69, 0x04, 0x6B, 0x04, 0x6D, 0x04,
                                         0x6F, 0x04, 0x71, 0x04, 0x73, 0x04, 0x75, 0x04,
                                         0x77, 0x04, 0x79, 0x04, 0x7B, 0x04, 0x7D, 0x04,
                                         0x7F, 0x04));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB16, p),
                     () -> Packet.create(0xFB16, ADDR,
                     // @formatter:off
                                         0x80, 0x04, 0x00, 0x00, 0x81, 0x04, 0x00, 0x00,
                                         0x83, 0x04, 0x00, 0x00, 0x85, 0x04, 0x00, 0x00,
                                         0x87, 0x04, 0x00, 0x00, 0x89, 0x04, 0x00, 0x00,
                                         0x8B, 0x04, 0x00, 0x00, 0x8D, 0x04, 0x00, 0x00,
                                         0x8F, 0x04, 0x00, 0x00, 0x91, 0x04, 0x00, 0x00,
                                         0x93, 0x04, 0x00, 0x00, 0x95, 0x04, 0x00, 0x00,
                                         0x97, 0x04, 0x00, 0x00, 0x99, 0x04, 0x00, 0x00,
                                         0x9B, 0x04, 0x00, 0x00, 0x9D, 0x04, 0x00, 0x00,
                                         0x9F, 0x04, 0x00, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB17, p),
                     () -> Packet.create(0xFB17, ADDR,
                     // @formatter:off
                                         0xA0, 0x04, 0x00, 0x00, 0xA1, 0x04, 0x00, 0x00,
                                         0xA3, 0x04, 0x00, 0x00, 0xA5, 0x04, 0x00, 0x00,
                                         0xA7, 0x04, 0x00, 0x00, 0xA9, 0x04, 0x00, 0x00,
                                         0xAB, 0x04, 0x00, 0x00, 0xAD, 0x04, 0x00, 0x00,
                                         0xAF, 0x04, 0x00, 0x00, 0xB1, 0x04, 0x00, 0x00,
                                         0xB3, 0x04, 0x00, 0x00, 0xB5, 0x04, 0x00, 0x00,
                                         0xB7, 0x04, 0x00, 0x00, 0xB9, 0x04, 0x00, 0x00,
                                         0xBB, 0x04, 0x00, 0x00, 0xBD, 0x04, 0x00, 0x00,
                                         0xBF, 0x04, 0x00, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAF6, p),
                     () -> Packet.create(0xFAF6, ADDR,
                     // @formatter:off
                                         0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                         0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                         0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                         0x05, 0x00, 0x46, 0x05));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAF5, p),
                     () -> Packet.create(0xFAF5, ADDR,
                     // @formatter:off
                                         0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                         0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                         0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                         0x07, 0x00, 0x08, 0x07));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAF4, p),
                     () -> Packet.create(0xFAF4, ADDR,
                     // @formatter:off
                                         0xB0, 0x30, 0x2C, 0x02, 0x58, 0x94, 0x62, 0x06,
                                         0x7A, 0xD6, 0x05, 0x00, 0x5D, 0x30, 0x1D, 0x00,
                                         0x27, 0x76, 0x4A, 0x00, 0x4F, 0xD6, 0xF8, 0x0B,
                                         0x9D, 0xE7, 0x0D, 0x00, 0x2E, 0x06, 0x00, 0x00,
                                         0x4A, 0x18, 0x61, 0x01, 0xCC, 0x3D, 0x00, 0x00,
                                         0xD9, 0x02, 0x00, 0x00, 0x3B, 0xCF, 0x1B, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB00, p),
                     () -> Packet.create(0xFB00, ADDR,
                     // @formatter:off
                                         0x06, 0xAD, 0x01, 0x68, 0x01, 0x04, 0x03, 0x04,
                                         0x58, 0x03, 0x02, 0x23, 0x00, 0x1C, 0x00, 0xF9,
                                         0x3D, 0x01, 0x08, 0x01, 0xF7, 0x49, 0x00, 0x3C,
                                         0x00, 0xF5, 0xDB, 0x00, 0xB8, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAFF, p),
                     () -> Packet.create(0xFAFF,
                                         ADDR,
                                         // @formatter:off
                                         0x06, 0x5B, 0x32, 0x34, 0x04, 0x04, 0x06, 0x08,
                                         0xB0, 0x06, 0x02, 0x6B, 0x00, 0x58, 0x00, 0xF9,
                                         0x7A, 0x02, 0x10, 0x02, 0xF7, 0xDC, 0x00, 0x74,
                                         0x22, 0xF5, 0x91, 0x02, 0x24, 0x02));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB01, p),
                     () -> Packet.create(0xFB01,
                                         ADDR,
                                         // @formatter:off
                                         0x06, 0x7D, 0x60, 0x10, 0x00, 0xC0, 0xBC, 0x05,
                                         0x00, 0x04, 0xCE, 0x31, 0x02, 0x00, 0x02, 0x49,
                                         0x1D, 0x00, 0x00, 0xE0, 0x79, 0x00, 0x00, 0xF9,
                                         0x86, 0xAD, 0x00, 0x00, 0xA8, 0xD2, 0x02, 0x00,
                                         0xF7, 0x4B, 0xC3, 0x00, 0x00, 0x90, 0xFB, 0x00,
                                         0x00, 0xF5, 0xD0, 0xB3, 0x00, 0x00, 0x38, 0xED,
                                         0x02, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAFE, p),
                     () -> Packet.create(0xFAFE, ADDR,
                     // @formatter:off
                                         0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                         0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                         0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                         0x05, 0x00, 0x46, 0x05));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAFC, p),
                     () -> Packet.create(0xFAFC, ADDR,
                     // @formatter:off
                                         0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                         0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                         0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                         0x07, 0x00, 0x08, 0x07));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAFD, p),
                     () -> Packet.create(0xFAFD, ADDR,
                     // @formatter:off
                                         0xB0, 0x30, 0x2C, 0x02, 0x58, 0x94, 0x62, 0x06,
                                         0x7A, 0xD6, 0x05, 0x00, 0x5D, 0x30, 0x1D, 0x00,
                                         0x27, 0x76, 0x4A, 0x00, 0x4F, 0xD6, 0xF8, 0x0B,
                                         0x9D, 0xE7, 0x0D, 0x00, 0x2E, 0x06, 0x00, 0x00,
                                         0x4A, 0x18, 0x61, 0x01, 0xCC, 0x3D, 0x00, 0x00,
                                         0xD9, 0x02, 0x00, 0x00, 0x3B, 0xCF, 0x1B, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAF6, p),
                     () -> Packet.create(0xFAF6, ADDR,
                     // @formatter:off
                                         0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                         0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAF5, p),
                     () -> Packet.create(0xFAF5, ADDR,
                     // @formatter:off
                                         0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                         0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAF4, p),
                     () -> Packet.create(0xFAF4,
                                         ADDR,
                                         // @formatter:off
                                         0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                         0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                         0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                         0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB00, p),
                     () -> Packet.create(0xFB00,
                                         ADDR,
                                         // @formatter:off
                                         0x06, 0xAD, 0x01, 0x68, 0x01, 0x04, 0x03, 0x04,
                                         0x58, 0x03, 0x02, 0x23, 0x00, 0x1C, 0x00, 0xF9,
                                         0x3D, 0x01, 0x08, 0x01, 0xF7, 0x49, 0x00, 0x3C,
                                         0x00, 0xF5, 0xDB, 0x00, 0xB8, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAFF, p),
                     () -> Packet.create(0xFAFF,
                                         ADDR,
                                         // @formatter:off
                                         0x06, 0x5B, 0x32, 0x34, 0x04, 0x04, 0x06, 0x08,
                                         0xB0, 0x06, 0x02, 0x6B, 0x00, 0x58, 0x00, 0xF9,
                                         0x7A, 0x02, 0x10, 0x02, 0xF7, 0xDC, 0x00, 0x74,
                                         0x22, 0xF5, 0x91, 0x02, 0x24, 0x02));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFB01, p),
                     () -> Packet.create(0xFB01,
                                         ADDR,
                                         // @formatter:off
                                         0x06, 0x7D, 0x60, 0x10, 0x00, 0xC0, 0xBC, 0x05,
                                         0x00, 0x04, 0xCE, 0x31, 0x02, 0x00, 0x02, 0x49,
                                         0x1D, 0x00, 0x00, 0xE0, 0x79, 0x00, 0x00, 0xF9,
                                         0x86, 0xAD, 0x00, 0x00, 0xA8, 0xD2, 0x02, 0x00,
                                         0xF7, 0x4B, 0xC3, 0x00, 0x00, 0x90, 0xFB, 0x00,
                                         0x00, 0xF5, 0xD0, 0xB3, 0x00, 0x00, 0x38, 0xED,
                                         0x02, 0x00));
        // @formatter:on

        sim.response(p -> isRequestFor(0xFAF3, p),
                     () -> Packet.create(0xFAF3, ADDR, 0x78, 0x69, 0x8C, 0x0A, 0x8E, 0x44, 0xFF, 0xFF));

        sim.response(p -> isRequestFor(0xFAF2, p),
                     () -> Packet.create(0xFAF2, ADDR, 0xA0, 0x8C, 0x10, 0x0E, 0x68, 0x5B, 0xFF, 0xFF));

        sim.response(p -> isRequestFor(0xFAF1, p),
                     () -> Packet.create(0xFAF1,
                                         ADDR,
                                         // @formatter:off
                                         0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                         0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on

        // Req PGN 64587 from Engine #1 (0) with SPNs 6895, 7333
        sim.response(p -> isRequestFor(64587, p),
                     () -> Packet.create(64587,
                                         ADDR,
                                         0,
                                         0,
                                         0,
                                         0,
                                         (isEngineOn() ? 0xA5 : 0),
                                         (isEngineOn() ? 0xA5 : 0),
                                         0,
                                         0));

        // Req PGN 64891 from Engine #1 (0) with SPNs 5466
        sim.response(p -> isRequestFor(64891, p),
                     () -> Packet.create(64891, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // Req PGN 64920 from Engine #1 (0) with SPNs 5827
        sim.response(p -> isRequestFor(64920, p),
                     () -> Packet.create(64920, ADDR, new int[40]));

        // Req PGN 64962 from Engine #1 (0) with SPNs 5829, 5837
        sim.response(p -> isRequestFor(64962, p),
                     () -> Packet.create(64962, ADDR, 0, 0, 0, 0, 0, 0, 0x04, 0));

        // Req PGN 64981 from Engine #1 (0) with SPNs 2791
        sim.response(p -> isRequestFor(64981, p),
                     () -> Packet.create(64981, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // Req PGN 65154 from Engine #1 (0) with SPNs 1413
        sim.response(p -> isRequestFor(65154, p),
                     () -> Packet.create(65154, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // Req PGN 65244 from Engine #1 (0) with SPNs 235
        sim.response(p -> isRequestFor(65244, p),
                     () -> Packet.create(65244, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // Req PGN 65253 from Engine #1 (0) with SPNs 247
        sim.response(p -> isRequestFor(65253, p),
                     () -> Packet.create(65253, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // Req PGN 65255 from Engine #1 (0) with SPNs 248
        sim.response(p -> isRequestFor(65255, p),
                     () -> Packet.create(65255, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // DM58 Req PGN 64475 from Engine #1 (0)
        sim.response(Engine::isRequestForDM58,
                     (p) -> {
                         DM7CommandTestsPacket dm7 = new DM7CommandTestsPacket(p);
                         if (dm7.getSpn() == 102) {
                             return AcknowledgmentPacket.create(ADDR, NACK).getPacket();
                         }
                         return DM58RationalityFaultSpData.create(ADDR,
                                                                  dm7.getTestId(),
                                                                  dm7.getSpn(),
                                                                  new int[] { 0xF0, 0xFF, 0xFF, 0xFF })
                                                          .getPacket();
                     });

        // BCT PGN 61443 from Engine #1 (0) with SPNs 91, 92
        sim.schedule(50,
                     MILLISECONDS,
                     () -> Packet.create(61443, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 61455 from Engine #1 (0) with SPNs 3226
        sim.schedule(50,
                     MILLISECONDS,
                     () -> Packet.create(61455, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 64892 from Engine #1 (0) with SPNs 3700
        sim.schedule(1000,
                     MILLISECONDS,
                     () -> Packet.create(64892, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 64908 from Engine #1 (0) with SPNs 3609
        sim.schedule(500,
                     MILLISECONDS,
                     () -> Packet.create(64908, ADDR, 0, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));

        // BCT PGN 64916 from Engine #1 (0) with SPNs 27
        sim.schedule(100,
                     MILLISECONDS,
                     () -> Packet.create(64916, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 64923 from Engine #1 (0) with SPNs 3516
        sim.schedule(1000,
                     MILLISECONDS,
                     () -> Packet.create(64923, ADDR, 0xFF, 0, 0xFF, 0, 0, 0, 0xFF, 0xFF));

        // BCT PGN 65110 from Engine #1 (0) with SPNs 3031
        sim.schedule(1000,
                     MILLISECONDS,
                     () -> Packet.create(65110, ADDR, 0, 0x7F, 0, 0, 0, 0, 0, 0));

        // BCT PGN 65247 from Engine #1 (0) with SPNs 514, 2978
        sim.schedule(250,
                     MILLISECONDS,
                     () -> Packet.create(65247,
                                         ADDR,
                                         (isEngineOn() ? 0x7F : 0),
                                         0,
                                         0,
                                         0,
                                         (isEngineOn() ? 0x7F : 0),
                                         0,
                                         0,
                                         0));

        // BCT PGN 65251 from Engine #1 (0) with SPNs 539, 540, 541, 542, 543, 544
        // @formatter:off
        sim.schedule(5000,
                     MILLISECONDS,
                     () -> Packet.create(65251,
                                         ADDR,
                                         0, 0, 0xA5, 0, 0, 0xA5, 0, 0,
                                         0xA5, 0, 0, 0xA5, 0, 0, 0, 0,
                                         0, 0, 0, 0xA5, 0x01, 0, 0, 0,
                                         0, 0, 0, 0, 0, 0, 0, 0,
                                         0, 0, 0, 0, 0, 0, 0, 0,
                                         0, 0, 0, 0, 0, 0, 0, 0,
                                         0, 0, 0, 0, 0, 0, 0, 0));
        // @formatter:on

        // BCT PGN 65262 from Engine #1 (0) with SPNs 110
        sim.schedule(1000,
                     MILLISECONDS,
                     () -> Packet.create(65262, ADDR, 0x7F, 0, 0xFF, 0xFF, 0, 0, 0, 0));

        // BCT PGN 65263 from Engine #1 (0) with SPNs 94
        sim.schedule(500,
                     MILLISECONDS,
                     () -> Packet.create(65263, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 65265 from Engine #1 (0) with SPNs 84
        sim.schedule(100,
                     MILLISECONDS,
                     () -> Packet.create(65265, ADDR, 0, 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 65266 from Engine #1 (0) with SPNs 183
        sim.schedule(100,
                     MILLISECONDS,
                     () -> Packet.create(65266, ADDR, (isEngineOn() ? 40 : 0), 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 65269 from Engine #1 (0) with SPNs 108
        sim.schedule(1000,
                     MILLISECONDS,
                     () -> Packet.create(65269, ADDR, 0x7F, 0, 0, 0, 0, 0, 0, 0));

        // BCT PGN 65270 from Engine #1 (0) with SPNs 102
        sim.schedule(500,
                     MILLISECONDS,
                     () -> Packet.create(65270, ADDR, 0, 0, 0, 0xFF, 0, 0, 0, 0));

        // BCT PGN 65271 from Engine #1 (0) with SPNs 158
        sim.schedule(1000,
                     MILLISECONDS,
                     () -> Packet.create(65271, ADDR, 0, 0, 0, 0, 0xFF, 0xFF, 0, 0x7F));

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
        if (packet.getPgn() == DM7CommandTestsPacket.PGN) {
            DM7CommandTestsPacket dm7 = new DM7CommandTestsPacket(packet);
            return dm7.getTestId() == 247;
        }
        return false;
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
        if (this.keyState != KEY_ON_ENGINE_RUNNING && keyState == KEY_ON_ENGINE_RUNNING) {
            ignitionCycles++;
            ignitionCycleSecondsRunning = 0;
            warmedUp = false;

            if (getMilStatus() == ON) {
                secondsWithMIL += 60;
            }

            if (TimeUnit.SECONDS.toMinutes(secondsRunning) > 9) {
                obdConditions++;
                activeDTCs.clear();
                permanentDTCs.clear();
            }

            if (!pendingDTCs.isEmpty() && (ignitionCycles == 4 || ignitionCycles == 10)) {
                DiagnosticTroubleCode dtc = pendingDTCs.get(0);
                activeDTCs.add(dtc);
                permanentDTCs.add(dtc);
                pendingDTCs.clear();
            }

            if (nextFault != null) {
                pendingDTCs.add(nextFault);
                nextFault = null;
            }
        } else if (this.keyState != KEY_OFF && keyState == KEY_OFF) {
            if (ignitionCycles == 8 && !activeDTCs.isEmpty()) {
                var dtc = activeDTCs.get(0);
                previousDTCs.add(dtc);
                activeDTCs.clear();
                permanentDTCs.clear();
                pendingDTCs.clear();
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
        return keyState.isEngineOn;
    }

    private boolean isKeyOn() {
        return keyState.isKeyOn;
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

    private static boolean isRequestForDM58(Packet packet) {
        if (packet.getPgn() == DM7CommandTestsPacket.PGN) {
            DM7CommandTestsPacket dm7 = new DM7CommandTestsPacket(packet);
            return dm7.getTestId() == 245;
        }
        return false;
    }

}
