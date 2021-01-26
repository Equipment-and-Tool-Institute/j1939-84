/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.simulated;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime;
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
    private static final byte[] ENGINE_CAL_ID2 = "******* **      ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CAL_ID3 = "NOx-SAE14a ATI1 ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CAL_ID4 = "NOx-SAE14a ATO1 ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CAL_ID5 = "0201011002      ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CAL_ID6 = "PMS12330A100    ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CAL_ID7 = "021205FFFF      ".getBytes(A_UTF8);
    private static final byte[] ENGINE_CVN1 = as4Bytes(0x40DCBF96);
    private static final byte[] ENGINE_CVN2 = as4Bytes(0x5A1536EB);
    private static final byte[] ENGINE_CVN3 = as4Bytes(0x138973A8);
    private static final byte[] ENGINE_CVN4 = as4Bytes(0xC9F94B8C);
    private static final byte[] ENGINE_CVN5 = as4Bytes(0xC41FBB48);
    private static final byte[] ENGINE_CVN6 = as4Bytes(0xD7169BE8);
    private static final byte[] ENGINE_CVN7 = as4Bytes(0x1B72F353);

    private static final byte[] ENGINE_HOURS = as4Bytes(3564 * 20); // hrs
    private static final byte[] ENGINE_MODEL_YEAR = "2015E-MYUS HD OBD   ".getBytes(A_UTF8);
    private static final byte[] ENGINE_SPEED = as2Bytes(400 * 8); // rpm
    private static final byte[] ENGINE_SPEED_ZERO = as2Bytes(0); // rpm
    private static final byte NA = (byte) 0xFF;
    private static final byte[] NA3 = new byte[] { NA, NA, NA };
    private static final byte[] NA4 = new byte[] { NA, NA, NA, NA };

    /*
     * VIN can be any length up to 200 bytes, but should end with *
     */
    private static final byte[] VIN = "3HAMKSTN0FL575012*".getBytes(A_UTF8);

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

    private static boolean isDM7For(int spn, Packet packet) {
        boolean isId = packet.getPgn() == 0xE300;
        if (!isId) {
            return false;
        }
        boolean isTestId = packet.get(0) == 247;
        boolean isFmi = (packet.get(3) & 0x1F) == 31;
        int reqSpn = (((packet.get(3) & 0xE0) << 11) & 0xFF0000) | ((packet.get(2) << 8) & 0xFF00)
                | (packet.get(1) & 0xFF);
        boolean isSpn = spn == reqSpn;
        return isTestId && isFmi && isSpn;
    }

    private static boolean isRequestFor(int pgn, int address, Packet packet) {
        return (packet.getId(0xFFFF) == (0xEA00 | address) || packet.getId(0xFFFF) == 0xEAFF) && packet.get24(0) == pgn;
    }

    private static boolean isRequestFor(int pgn, Packet packet) {
        return isRequestFor(pgn, ADDR, packet);
    }

    private int demCount;

    private boolean dtcsCleared = false;

    private final boolean[] engineOn = { false };

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private int numCount;

    private final Sim sim;

    public Engine(Bus bus) throws BusException {
        sim = new Sim(bus);

        // xmsn rate is actually engine speed dependent
        sim.schedule(100,
                     100,
                     TimeUnit.MILLISECONDS,
                     () -> Packet.create(61444,
                                         ADDR,
                                         combine(NA3, engineOn[0] ? ENGINE_SPEED : ENGINE_SPEED_ZERO, NA3)));
        sim.schedule(100, 100, TimeUnit.MILLISECONDS, () -> Packet.create(65248, ADDR, combine(NA4, DISTANCE)));

        sim.schedule(50, 50, TimeUnit.MILLISECONDS, () -> Packet.create(0x0C,
                                                                        0xF00A,
                                                                        0x00,
                                                                        false,
                                                                        (byte) 0x00,
                                                                        (byte) 0x00,
                                                                        (byte) 0x00,
                                                                        (byte) 0x00,
                                                                        (byte) 0xFF,(byte) 0xFF,
                                                                        (byte) 0xFF,
                                                                        (byte) 0xFF));
        sim.response(p -> isRequestFor(65259, p), () -> Packet.create(65259, ADDR, COMPONENT_ID));

        // 65278, Auxiliary Water Pump Pressure, AWPP, 1 s, 1, 73, Auxiliary Pump Pressure,4 9
        // When the DM24 is reported as supporting SPN 73, this will begin the count down to report the key
        //  state of key on.
        sim.response(p -> isRequestFor(65278, ADDR, p), () -> {
            // Start a timer to turn the engine on
            executor.schedule(() -> engineOn[0] = true, 30, TimeUnit.SECONDS);
            return Packet.create(65278, ADDR, new byte[8]);
        });

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

        // DM19
        sim.response(p -> isRequestFor(54016, p),
                     p -> Packet.create(54016 | p.getSource(),
                                        ADDR,
                                        combine(ENGINE_CVN1,
                                                ENGINE_CAL_ID1,
                                                ENGINE_CVN2,
                                                ENGINE_CAL_ID2,
                                                ENGINE_CVN3,
                                                ENGINE_CAL_ID3,
                                                ENGINE_CVN4,
                                                ENGINE_CAL_ID4,
                                                ENGINE_CVN5,
                                                ENGINE_CAL_ID5,
                                                ENGINE_CVN6,
                                                ENGINE_CAL_ID6,
                                                ENGINE_CVN7,
                                                ENGINE_CAL_ID7)));

        // DM1
        sim.schedule(1, 1, TimeUnit.SECONDS,
                     () -> Packet.create(DM1ActiveDTCsPacket.PGN,
                                         ADDR,
                                         0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF));
        // DM6
        sim.response(p -> isRequestFor(65231, p), () -> Packet.create(65231, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));
        // DM12
        sim.response(p -> isRequestFor(DM12MILOnEmissionDTCPacket.PGN, p),
                     () -> Packet.create(DM12MILOnEmissionDTCPacket.PGN, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));
        // DM23
        sim.response(p -> isRequestFor(64949, p), () -> Packet.create(64949, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));

        // DM27
        sim.response(p -> isRequestFor(DM27AllPendingDTCsPacket.PGN, p),
                     () -> Packet.create(DM27AllPendingDTCsPacket.PGN,
                                         ADDR,
                                         0x03, 0xFF, 0x66, 0x00, 0x04, 0x01, 0xFF, 0xFF));
        sim.response(p -> isRequestFor(DM27AllPendingDTCsPacket.PGN, p),
                     () -> Packet.create(DM27AllPendingDTCsPacket.PGN,
                                         0x17,
                                         0x43, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF));
        // DM28
        sim.response(p -> isRequestFor(DM28PermanentEmissionDTCPacket.PGN, p),
                     () -> Packet.create(DM28PermanentEmissionDTCPacket.PGN,
                                         ADDR,
                                         0x03, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF));
        // DM11
        sim.response(p -> isRequestFor(65235, p), p -> {
            dtcsCleared = true;
            return Packet.create(0xE8FF, ADDR, 0x00, 0xFF, 0xFF, 0xFF, p.getSource(), 0xD3, 0xFE, 0x00);
        });
        // DM5
        sim.response(p -> isRequestFor(65230, p),
                     () -> Packet.create(65230, ADDR, 0x00, 0x00, 0x14, 0x37, 0xE0, 0x1E, 0xE0, 0x1E));
        sim.response(p -> isRequestFor(65230, p),
                     () -> Packet.create(65230, 0x17, 0x00, 0x00, 0x13, 0x37, 0xE0, 0x1E, 0xE0, 0x1E));
        sim.response(p -> isRequestFor(65230, p),
                     p -> Packet.create(0xE8FF, 0x21, 0x01, 0xFF, 0xFF, 0xFF, p.getSource(), 0xCE, 0xFE, 0x00));

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
        sim.response(p -> isRequestFor(0xFDB8, p),
                     () -> Packet.create(0xFDB8, ADDR, 0x00, 0x00, 0x00, 0x37, 0xC0, 0x1E, 0xC0, 0x1E));

        // @formatter:off
        // DM20
        sim.response(p -> isRequestFor(DM20MonitorPerformanceRatioPacket.PGN, 0x01, p),
                     p -> Packet.create(DM20MonitorPerformanceRatioPacket.PGN | p.getSource(),
                                        ADDR,
                                        0x0C, 0x00,// Ignition Cycles
                                        demCount & 0xFF, (demCount >> 8) & 0xFF,
                                        // OBD Counts
                                        // Monitors 3 Bytes SPN, 2 bytes: Num, Dem
                                        0xCA,
                                        0x14,
                                        0xF8,
                                        0x00,
                                        0x00,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF,
                                        0xB8,
                                        0x12,
                                        0xF8,
                                        0x00,
                                        0x00,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF,
                                        0xBC,
                                        0x14,
                                        0xF8,
                                        0x01,
                                        0x00,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF,
                                        0xF8,
                                        0x0B,
                                        0xF8,
                                        numCount & 0xFF,
                                        (numCount >> 8) & 0xFF,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF,
                                        0xC6,
                                        0x14,
                                        0xF8,
                                        0x00,
                                        0x00,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF,
                                        0xF2,
                                        0x0B,
                                        0xF8,
                                        0x00,
                                        0x00,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF,
                                        0xC9,
                                        0x14,
                                        0xF8,
                                        0x00,
                                        0x00,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF,
                                        0xEF,
                                        0x0B,
                                        0xF8,
                                        numCount & 0xFF,
                                        (numCount >> 8) & 0xFF,
                                        demCount & 0xFF,
                                        (demCount >> 8) & 0xFF));
        // @formatter:on

        // @formatter:off
        // DM 20 from second module
        sim.response(p -> isRequestFor(DM20MonitorPerformanceRatioPacket.PGN, 0x61, p),
                     p -> Packet.create(DM20MonitorPerformanceRatioPacket.PGN | p.getSource(),
                                        0x61,
                                        0x54, 0x00, // Ignition Cycles
                                        0x19, 0x00, // OBD Counts
                                        0xCA, 0x14, 0xF8, //SPN
                                        0x03, 0x00, //Numerator
                                        0x04, 0x00, //Denominator
                                        0xB8, 0x12, 0xF8, //SPN
                                        0x07, 0x00, //Numerator
                                        0x19, 0x00, //Denominator
                                        0xC6, 0x14, 0xF8, //SPN
                                        0x02, 0x00, //Numerator
                                        0x19, 0x00, //Denominator
                                        0xF8, 0x0B, 0xF8, //SPN
                                        0x11, 0x00,//Numerator
                                        0x19, 0x00 //Denominator
                     ));
        // @formatter:on

        // @formatter:off
        // DM 20 from third module
        sim.response(p -> isRequestFor(DM20MonitorPerformanceRatioPacket.PGN, ADDR, p),
                     p -> Packet.create(DM20MonitorPerformanceRatioPacket.PGN | p.getSource(),
                                        ADDR,
                                        0x54, 0x00, // Ignition Cycles
                                        0x19, 0x00, // OBD Counts
                                        0xCA, 0x14, 0xF8, //SPN
                                        0x03, 0x00, //Numerator
                                        0x04, 0x00, //Denominator
                                        0xB8, 0x12, 0xF8, //SPN
                                        0x07, 0x00, //Numerator
                                        0x19, 0x00, //Denominator
                                        0xC6, 0x14, 0xF8, //SPN
                                        0x02, 0x00, //Numerator
                                        0x19, 0x00, //Denominator
                                        0xF8, 0x0B, 0xF8, //SPN
                                        0x11, 0x00, //Numerator
                                        0x19, 0x00 //Denominator
                     ));
        // @formatter:on

        // DM21
        sim.response(p -> isRequestFor(49408, p),
                     p -> Packet.create(49408 | p.getSource(), ADDR,
                                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                        dtcsCleared ? 0x00 : 0x10,
                                        0x00));

        // DM24 supported SPNs
        sim.response(p -> isRequestFor(DM24SPNSupportPacket.PGN, p),
                     () -> {
                         SupportedSPN spn1173 = SupportedSPN.create(1173, true, true, true, 1);
                         SupportedSPN spn102 = SupportedSPN.create(102, true, true, true, 1);
                         // support for the SPN of 73 (Aux. water pump is added here to trigger a key state change)
                         SupportedSPN spn73 = SupportedSPN.create(73, true, true, true, 1);
                         return DM24SPNSupportPacket.create(ADDR, spn1173, spn102, spn73).getPacket();
                     });

        // DM29 response
        sim.response(p -> isRequestFor(DM29DtcCounts.PGN, p),
                     p -> Packet.create(DM29DtcCounts.PGN | p.getSource(),
                                        ADDR,
                                        0x00, 0x00, 0x01, 0x00, 0x01, 0xFF, 0xFF, 0xFF));
        sim.response(p -> isRequestFor(DM29DtcCounts.PGN, p),
                     p -> Packet.create(DM29DtcCounts.PGN | p.getSource(),
                                        0x33,
                                        0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF));

        // @formatter:off
        // DM30 response for DM7 Request for SPN 102
        sim.response(p -> isDM7For(102, p),
                     p -> Packet.create(0xA400 | p.getSource(),
                                        ADDR,
                                        0xF7, //Test ID
                                        0x66, 0x00, 0x12, //SPN
                                        0xD0, 0x00, //SLOT
                                        0x00, 0xFB, //Value
                                        0xFF, 0xFF, //Max
                                        0xFF, 0xFF, //Min

                                        0xF7, //Test ID
                                        0x66, 0x00, 0x10, //SPN
                                        0x6A, 0x00, //SLOT
                                        0x00, 0xFB, //Value
                                        0xFF, 0xFF, //Max
                                        0xFF, 0xFF, //Min

                                        0xF7, //Test ID
                                        0x66, 0x00, 0x0A, //SPN
                                        0x0C, 0x00, //SLOT
                                        0x00, 0xFB, //Value
                                        0xFF, 0xFF, //Max
                                        0xFF, 0xFF //Min
                     ));
        // @formatter:on

        // DM30 response for DM7 Request for SPN 1173
        sim.response(p -> isDM7For(1173, p),
                     p -> Packet
                             .create(0xA400 | p.getSource(), ADDR, 0xF7, 0x95, 0x04, 0x10, 0x66, 0x01, 0x00, 0xFB, 0xFF,
                                     0xFF, 0xFF, 0xFF));

        // DM31 response
        sim.response(p -> isRequestFor(DM31DtcToLampAssociation.PGN, p),
                     () -> Packet.create(DM31DtcToLampAssociation.PGN | 0xFF,
                                         ADDR,
                                         0x61, 0x02, 0x13, 0x81, 0x62, 0x1D));
        sim.response(p -> isRequestFor(DM31DtcToLampAssociation.PGN, p),
                     () -> Packet.create(DM31DtcToLampAssociation.PGN | 0xFF,
                                         0x33,
                                         0x21, 0x06, 0x1F, 0x23, 0x22, 0xDD));

        // @formatter:off
        // DM33 response for DM33 Global Request for PGN 41216
        sim.response(p -> isRequestFor(DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN, p),
                     p -> Packet
                             .create(DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN | p.getSource(),
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
        sim.response(p -> isRequestFor(DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN, p),
                     p -> Packet
                             .create(DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN | p.getSource(),
                                     0x17,
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
        sim.response(p -> isRequestFor(64711, p),
                     () -> Packet.create(64711, ADDR, ENGINE_MODEL_YEAR));
    }

    @Override
    public void close() {
        sim.close();
    }

    private void startTimer() {
        executor.scheduleAtFixedRate(() -> {
            if (numCount < 0xFAFF) {
                numCount++;
            }
            if (demCount < 0xFAFF) {
                demCount++;
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
}
