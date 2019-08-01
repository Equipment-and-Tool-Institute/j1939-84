/**
 * Copyright 2019 Equipment & Tool Institute
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

/**
 * Simulated Engine used for System Testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
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
        boolean isId = packet.getId() == 58112;
        if (!isId) {
            return false;
        }
        boolean isTestId = packet.get(0) == 247;
        boolean isFmi = (packet.get(3) & 0x1F) == 31;
        int reqSpn = (((packet.get(3) & 0xE0) << 11) & 0xFF0000) | ((packet.get(2) << 8) & 0xFF00)
                | (packet.get(1) & 0xFF);
        boolean isSpn = spn == reqSpn;
        boolean result = isTestId && isFmi && isSpn;
        return result;
    }

    private static boolean isRequestFor(int pgn, Packet packet) {
        return (packet.getId() == (0xEA00 | ADDR) || packet.getId() == 0xEAFF)
                && packet.get24(0) == pgn;
    }

    private int demCount;

    private boolean dtcsCleared = false;

    private boolean[] engineOn = { true };

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private int numCount;

    private final Sim sim;

    public Engine(Bus bus) throws BusException {
        sim = new Sim(bus);

        // xmsn rate is actually engine speed dependent
        sim.schedule(100, 100, TimeUnit.MILLISECONDS,
                () -> Packet.create(61444, ADDR, combine(NA3, engineOn[0] ? ENGINE_SPEED : ENGINE_SPEED_ZERO, NA3)));
        sim.schedule(100, 100, TimeUnit.MILLISECONDS, () -> Packet.create(65248, ADDR, combine(NA4, DISTANCE)));
        sim.response(p -> isRequestFor(65259, p), () -> {
            // Start a timer to turn the engine off
            executor.schedule(() -> engineOn[0] = false, 10, TimeUnit.SECONDS);
            return Packet.create(65259, ADDR, COMPONENT_ID);
        });
        sim.response(p -> isRequestFor(65253, p), () -> {
            // Start a timer that will increment the numerators and denominators
            // for UI demo purposes
            startTimer();
            return Packet.create(65253, ADDR, combine(ENGINE_HOURS, NA4));
        });
        // Address Claim
        sim.response(p -> isRequestFor(0xEE00, p),
                () -> Packet.create(0xEEFF, ADDR, 0x00, 0x00, 0x40, 0x05, 0x00, 0x00, 0x65, 0x14));
        sim.response(p -> isRequestFor(65260, p), () -> Packet.create(65260, ADDR, VIN));
        sim.response(p -> isRequestFor(54016, p),
                () -> Packet.create(54016, ADDR,
                        combine(ENGINE_CVN1, ENGINE_CAL_ID1, ENGINE_CVN2, ENGINE_CAL_ID2, ENGINE_CVN3, ENGINE_CAL_ID3,
                                ENGINE_CVN4, ENGINE_CAL_ID4, ENGINE_CVN5, ENGINE_CAL_ID5, ENGINE_CVN6, ENGINE_CAL_ID6,
                                ENGINE_CVN7, ENGINE_CAL_ID7)));

        // DM6
        sim.response(p -> isRequestFor(65231, p), () -> Packet.create(65231, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));
        // DM12
        sim.response(p -> isRequestFor(65236, p), () -> Packet.create(65236, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));
        // DM23
        sim.response(p -> isRequestFor(64949, p), () -> Packet.create(64949, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));
        // DM28
        sim.response(p -> isRequestFor(64896, p), () -> Packet.create(64896, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00));
        // DM11
        sim.response(p -> isRequestFor(65235, p),
                () -> {
                    dtcsCleared = true;
                    return Packet.create(0xE8FF, ADDR, 0x00, 0xFF, 0xFF, 0xFF, 0xF9, 0xD3, 0xFE, 0x00);
                });
        // DM5
        sim.response(p -> isRequestFor(65230, p),
                () -> Packet.create(65230, ADDR, 0x00, 0x00, 0x14, 0x37, 0xE0, 0x1E, 0xE0, 0x1E));
        // DM26
        sim.response(p -> isRequestFor(0xFDB8, p),
                () -> Packet.create(0xFDB8, ADDR, 0x00, 0x00, 0x00, 0x37, 0xC0, 0x1E, 0xC0, 0x1E));
        // DM20
        sim.response(p -> isRequestFor(0xC200, p),
                () -> Packet.create(0xC200, 0x01,
                        0x0C, 0x00, // Ignition Cycles
                        demCount & 0xFF, (demCount >> 8) & 0xFF, // OBD Counts
                        // Monitors 3 Bytes SPN, 2 bytes: Num, Dem
                        0xCA, 0x14, 0xF8, 0x00, 0x00, demCount & 0xFF, (demCount >> 8) & 0xFF,
                        0xB8, 0x12, 0xF8, 0x00, 0x00, demCount & 0xFF, (demCount >> 8) & 0xFF,
                        0xBC, 0x14, 0xF8, 0x01, 0x00, demCount & 0xFF, (demCount >> 8) & 0xFF,
                        0xF8, 0x0B, 0xF8, numCount & 0xFF, (numCount >> 8) & 0xFF, demCount & 0xFF,
                        (demCount >> 8) & 0xFF,
                        0xC6, 0x14, 0xF8, 0x00, 0x00, demCount & 0xFF, (demCount >> 8) & 0xFF,
                        0xF2, 0x0B, 0xF8, 0x00, 0x00, demCount & 0xFF, (demCount >> 8) & 0xFF,
                        0xC9, 0x14, 0xF8, 0x00, 0x00, demCount & 0xFF, (demCount >> 8) & 0xFF,
                        0xEF, 0x0B, 0xF8, numCount & 0xFF, (numCount >> 8) & 0xFF, demCount & 0xFF,
                        (demCount >> 8) & 0xFF));

        // DM 20 from second module
        sim.response(p -> isRequestFor(0xC200, p),
                () -> Packet.create(0xC200, 61,
                        0x54, 0x00, // Ignition Cycles
                        0x19, 0x00, // OBD Counts
                        0xCA, 0x14, 0xF8, 0x03, 0x00, 0x04, 0x00,
                        0xB8, 0x12, 0xF8, 0x07, 0x00, 0x19, 0x00,
                        0xC6, 0x14, 0xF8, 0x02, 0x00, 0x19, 0x00,
                        0xF8, 0x0B, 0xF8, 0x11, 0x00, 0x19, 0x00));
        // DM21
        sim.response(p -> isRequestFor(49408, p),
                () -> Packet.create(49408, ADDR, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, dtcsCleared ? 0x00 : 0x10, 0x00));

        // DM24 supported SPNs
        sim.response(p -> isRequestFor(64950, p),
                () -> Packet.create(64950, ADDR, 0x66, 0x00, 0x1B, 0x01, 0x95, 0x04, 0x1B, 0x02));
        // DM30 response for DM7 Request for SPN 102
        sim.response(p -> isDM7For(102, p),
                () -> Packet.create(0xA4F9, ADDR,
                        0xF7, 0x66, 0x00, 0x12, 0xD0, 0x00, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF,
                        0xF7, 0x66, 0x00, 0x10, 0x6A, 0x00, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF,
                        0xF7, 0x66, 0x00, 0x0A, 0x0C, 0x00, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF));
        // DM30 response for DM7 Request for SPN 1173
        sim.response(p -> isDM7For(1173, p), () -> Packet.create(0xA4F9, ADDR, 0xF7, 0x95, 0x04, 0x10, 0x66, 0x01, 0x00,
                0xFB, 0xFF, 0xFF, 0xFF, 0xFF));
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
