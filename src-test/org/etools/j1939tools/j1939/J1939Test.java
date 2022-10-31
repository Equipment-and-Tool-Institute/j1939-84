/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.etools.j1939tools.j1939.J1939.ENGINE_ADDR;
import static org.etools.j1939tools.j1939.J1939.GLOBAL_ADDR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.EchoBus;
import org.etools.j1939tools.bus.Either;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.bus.TestResultsListener;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939tools.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939tools.j1939.packets.EngineHoursPacket;
import org.etools.j1939tools.j1939.packets.EngineSpeedPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.etools.testdoc.TestDoc;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for the {@link J1939} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@RunWith(MockitoJUnitRunner.class)
public class J1939Test {

    /**
     * The address of the tool on the bus - for testing. This is NOT the right
     * service tool address to confirm it's not improperly hard-coded (because
     * it was)
     */
    private static final int BUS_ADDR = 0xA5;
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Mock
    private Bus bus;
    private J1939 instance;
    private ArgumentCaptor<Packet> sendPacketCaptor;

    private static final CommunicationsListener NOOP = x -> {
    };
    private static final int TIMEOUT = 750;

    private static void sleep(double d) throws InterruptedException {
        Thread.sleep((long) (d * 1000));
    }

    @Test
    public void busyNack4Test() throws Exception {
        try (EchoBus echoBus = new EchoBus(0xF9)) {
            J1939TP tpBus = new J1939TP(echoBus);

            J1939 j1939 = new J1939(tpBus);
            Stream<Packet> reqStream = echoBus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // Global request 1
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", reqStream.findFirst().get().toString());

                    Stream<Packet> s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8FF00 [8] 03 FF FF FF F9 00 C1 00"));

                    // Global request 2
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());

                    s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8FF00 [8] 03 FF FF FF F9 00 C1 00"));
                    // DS request 1
                    assertEquals("18EA00F9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());

                    s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8F900 [8] 03 FF FF FF F9 00 C1 00"));

                    // DS request 2
                    assertEquals("18EA00F9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());
                    echoBus.send(Packet.parse("18E8F900 [8] 03 FF FF FF F9 00 C1 00"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail(e.getMessage());
                }
            }).start();
            RequestResult<DM21DiagnosticReadinessPacket> response = j1939.requestGlobal("test",
                                                                                        DM21DiagnosticReadinessPacket.class,
                                                                                        NOOP);
            List<Either<DM21DiagnosticReadinessPacket, AcknowledgmentPacket>> list = response.getEither();
            assertEquals(1, list.size());
            assertEquals(org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY,
                         list.get(0).right.get().getResponse());
        }
    }

    @Test
    public void busyNack3Test() throws Exception {
        try (EchoBus echoBus = new EchoBus(0xF9)) {
            J1939TP tpBus = new J1939TP(echoBus);

            J1939 j1939 = new J1939(tpBus);
            Stream<Packet> reqStream = echoBus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // Global request 1
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", reqStream.findFirst().get().toString());

                    Stream<Packet> s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8FF00 [8] 03 FF FF FF F9 00 C1 00"));

                    // Global request 2
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());

                    s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8FF00 [8] 03 FF FF FF F9 00 C1 00"));
                    // DS request 1
                    assertEquals("18EA00F9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());

                    s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8F900 [8] 03 FF FF FF F9 00 C1 00"));

                    // DS request 2
                    assertEquals("18EA00F9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());
                    echoBus.send(Packet.parse("18C1F900 [8] 00 00 00 00 00 00 00 00"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail(e.getMessage());
                }
            }).start();
            RequestResult<DM21DiagnosticReadinessPacket> response = j1939.requestGlobal("test",
                                                                                        DM21DiagnosticReadinessPacket.class,
                                                                                        NOOP);
            List<Either<DM21DiagnosticReadinessPacket, AcknowledgmentPacket>> list = response.getEither();
            assertEquals(1, list.size());
            assertEquals(Packet.parse("18C1F900 [8] 00 00 00 00 00 00 00 00"),
                         list.get(0).left.get().getPacket());
        }
    }

    @Test
    public void busyNack2Test() throws Exception {
        try (EchoBus echoBus = new EchoBus(0xF9)) {
            J1939TP tpBus = new J1939TP(echoBus);

            J1939 j1939 = new J1939(tpBus);
            Stream<Packet> reqStream = echoBus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // Global request 1
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", reqStream.findFirst().get().toString());

                    Stream<Packet> s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8FF00 [8] 03 FF FF FF F9 00 C1 00"));

                    // Global request 2
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());

                    s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8FF00 [8] 03 FF FF FF F9 00 C1 00"));
                    // DS request 1
                    assertEquals("18EA00F9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());
                    echoBus.send(Packet.parse("18C1F900 [8] 00 00 00 00 00 00 00 00"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail(e.getMessage());
                }
            }).start();
            RequestResult<DM21DiagnosticReadinessPacket> response = j1939.requestGlobal("test",
                                                                                        DM21DiagnosticReadinessPacket.class,
                                                                                        NOOP);
            List<Either<DM21DiagnosticReadinessPacket, AcknowledgmentPacket>> list = response.getEither();
            assertEquals(1, list.size());
            assertEquals(Packet.parse("18C1F900 [8] 00 00 00 00 00 00 00 00"),
                         list.get(0).left.get().getPacket());
        }
    }

    @Test
    public void busyNack1Test() throws Exception {
        try (EchoBus echoBus = new EchoBus(0xF9)) {
            J1939TP tpBus = new J1939TP(echoBus);

            J1939 j1939 = new J1939(tpBus);
            Stream<Packet> reqStream = echoBus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // Global request 1
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", reqStream.findFirst().get().toString());

                    Stream<Packet> s = echoBus.read(1, TimeUnit.HOURS);
                    echoBus.send(Packet.parse("18E8FF00 [8] 03 FF FF FF F9 00 C1 00"));

                    // Global request 2
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", s.skip(1).findFirst().get().toString());

                    echoBus.send(Packet.parse("18C1FF00 [8] 00 00 00 00 00 00 00 00"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail(e.getMessage());
                }
            }).start();
            RequestResult<DM21DiagnosticReadinessPacket> response = j1939.requestGlobal("test",
                                                                                        DM21DiagnosticReadinessPacket.class,
                                                                                        NOOP);
            List<Either<DM21DiagnosticReadinessPacket, AcknowledgmentPacket>> list = response.getEither();
            assertEquals(1, list.size());
            assertEquals(Packet.parse("18C1FF00 [8] 00 00 00 00 00 00 00 00"),
                         list.get(0).left.get().getPacket());
        }
    }

    @Test
    public void busyNack0Test() throws Exception {
        try (EchoBus echoBus = new EchoBus(0xF9)) {
            J1939TP tpBus = new J1939TP(echoBus);

            J1939 j1939 = new J1939(tpBus);
            Stream<Packet> reqStream = echoBus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // Global request 1
                    assertEquals("18EAFFF9 [3] 00 C1 00 (TX)", reqStream.findFirst().get().toString());

                    echoBus.send(Packet.parse("18C1FF00 [8] 00 00 00 00 00 00 00 00"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail(e.getMessage());
                }
            }).start();
            RequestResult<DM21DiagnosticReadinessPacket> response = j1939.requestGlobal("test",
                                                                                        DM21DiagnosticReadinessPacket.class,
                                                                                        NOOP);
            List<Either<DM21DiagnosticReadinessPacket, AcknowledgmentPacket>> list = response.getEither();
            assertEquals(1, list.size());
            assertEquals(Packet.parse("18C1FF00 [8] 00 00 00 00 00 00 00 00"),
                         list.get(0).left.get().getPacket());
        }
    }

    @Test
    public void aTestTP() throws Exception {
        final String VIN = "Some VINs are garbage, but this test doesn't care.";
        try (EchoBus echoBus = new EchoBus(0xF9)) {
            J1939 j1939 = new J1939(new J1939TP(echoBus));
            Stream<Packet> reqStream = echoBus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                // respond with VIN packets with long delays to verify over all
                // delay
                Optional<Packet> req = reqStream.findFirst();
                assertEquals(req.get(), Packet.parse("18EA00F9 EC FE 00 (TX)"));
                try {
                    echoBus.send(Packet.parse("18ECFF00 20 32 00 08 FF EC FE 00"));
                    int delay = 100;
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 01 53 6F 6D 65 20 56 49"));
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 02 4E 73 20 61 72 65 20"));
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 03 67 61 72 62 61 67 65"));
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 04 2C 20 62 75 74 20 74"));
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 05 68 69 73 20 74 65 73"));
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 06 74 20 64 6F 65 73 6E"));
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 07 27 74 20 63 61 72 65"));
                    Thread.sleep(delay);
                    echoBus.send(Packet.parse("18EBFF00 08 2E FF FF FF FF FF FF"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            System.err.format("### pre %d%n", System.currentTimeMillis());
            BusResult<VehicleIdentificationPacket> response = j1939.requestDS("test",
                                                                              J1939.getPgn(VehicleIdentificationPacket.class),
                                                                              Packet.create(0xEA00,
                                                                                            0xF9,
                                                                                            true,
                                                                                            VehicleIdentificationPacket.PGN,
                                                                                            VehicleIdentificationPacket.PGN >> 8,
                                                                                            VehicleIdentificationPacket.PGN >> 16),
                                                                              NOOP);
            System.err.format("### post %d%n", System.currentTimeMillis());
            String vin2 = response.getPacket().get().left.get().getVin();
            System.err.format("### vin %d%n", System.currentTimeMillis());
            assertEquals(VIN, vin2);
        }
    }

    /*
     * This tests real data that has previously failed. The individual BAM
     * packets take longer than 200 ms, so if we wait for one before trying to
     * read the next, we will miss some. Each BAM message completes at a
     * different time.
     */
    @Test
    public void feebBamTest() throws Exception {
        try (EchoBus echoBus = new EchoBus(0xF9)) {
            J1939TP tpBus = new J1939TP(echoBus);

            J1939 j1939 = new J1939(tpBus);
            Stream<Packet> reqStream = echoBus.read(1, TimeUnit.HOURS);
            Stream<Packet> stream = tpBus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                Optional<Packet> req = reqStream.findFirst();
                assertEquals("18EAFFF9 [3] EB FE 00 (TX)", req.get().toString());
                try {
                    sleep(0.0022);
                    echoBus.send(Packet.parse("1CECFF0B [8] 20 1B 00 04 FF EB FE 00"));
                    sleep(0.0006);
                    echoBus.send(Packet.parse("1CECFF00 [8] 20 2C 00 07 FF EB FE 00"));
                    sleep(0.0008);
                    echoBus.send(Packet.parse("18ECFF2A [8] 20 17 00 04 FF EB FE 00"));
                    sleep(0.0012);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0023);
                    echoBus.send(Packet.parse("18FEBF0B [8] 00 00 7D 7D 7D 7D FF FF"));
                    sleep(0.0034);
                    echoBus.send(Packet.parse("1CECFF8C [8] 20 19 00 04 FF EB FE 00"));
                    sleep(0.0044);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0012);
                    echoBus.send(Packet.parse("18FEF803 [8] FF FF FF FF EC 20 FB DE"));
                    sleep(0.0085);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0097);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0097);
                    echoBus.send(Packet.parse("18FECA03 [8] 00 FF 00 00 00 00 FF FF"));
                    sleep(0.0006);
                    echoBus.send(Packet.parse("18FECA10 [8] 00 FF 00 00 00 00 FF FF"));
                    sleep(0.0003);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0002);
                    echoBus.send(Packet.parse("18ECFF03 [8] 20 11 00 03 FF EB FE 00"));
                    sleep(0.009);
                    echoBus.send(Packet.parse("1CEBFF00 [8] 01 43 4D 4D 4E 53 2A 36"));
                    sleep(0.0006);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0074);
                    echoBus.send(Packet.parse("1CEBFF0B [8] 01 42 4E 44 57 53 2A 45"));
                    sleep(0.002);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0105);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0045);
                    echoBus.send(Packet.parse("18EBFF2A [8] 01 42 4E 44 57 53 2A 46"));
                    sleep(0.0057);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0089);
                    echoBus.send(Packet.parse("18EBFF03 [8] 01 41 4C 4C 53 4E 2A 33"));
                    sleep(0.0009);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0092);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0024);
                    echoBus.send(Packet.parse("18FEBF0B [8] 00 00 7D 7D 7D 7D FF FF"));
                    sleep(0.0023);
                    echoBus.send(Packet.parse("1CEBFF00 [8] 02 4C 20 75 32 31 44 30"));
                    sleep(0.006);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0015);
                    echoBus.send(Packet.parse("1CEBFF8C [8] 01 45 41 54 4F 4E 2A 56"));
                    sleep(0.006);
                    echoBus.send(Packet.parse("1CEBFF0B [8] 02 43 38 30 45 53 50 20"));
                    sleep(0.0028);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0095);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0094);
                    echoBus.send(Packet.parse("18EBFF03 [8] 02 30 30 30 20 50 54 53"));
                    sleep(0.0009);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0097);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0048);
                    echoBus.send(Packet.parse("18EBFF2A [8] 02 4C 52 32 31 2A 31 37"));
                    sleep(0.0049);
                    echoBus.send(Packet.parse("1CEBFF00 [8] 03 38 39 30 30 30 30 30"));
                    sleep(0.0002);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0104);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0075);
                    echoBus.send(Packet.parse("1CEBFF0B [8] 03 2A 30 30 30 30 30 30"));
                    sleep(0.0029);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0087);
                    echoBus.send(Packet.parse("18EBFF03 [8] 03 2A 2A 2A FF FF FF FF"));
                    sleep(0.0006);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0098);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0026);
                    echoBus.send(Packet.parse("18FEBF0B [8] 00 00 7D 7D 7D 7D FF FF"));
                    sleep(0.0077);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.004);
                    echoBus.send(Packet.parse("1CEBFF00 [8] 04 30 30 2A 37 34 36 30"));
                    sleep(0.0017);
                    echoBus.send(Packet.parse("1CEBFF8C [8] 02 53 34 30 30 44 49 55"));
                    sleep(0.0047);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0095);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0048);
                    echoBus.send(Packet.parse("18EBFF2A [8] 03 32 32 30 32 33 31 34"));
                    sleep(0.0028);
                    echoBus.send(Packet.parse("1CEBFF0B [8] 04 30 30 30 30 2A 2A FF"));
                    sleep(0.0026);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.001);
                    echoBus.send(Packet.parse("18EAFFF9 [3] EB FE 00 (TX)"));
                    sleep(0.0094);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0094);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0107);
                    echoBus.send(Packet.parse("1CEBFF00 [8] 05 37 31 39 31 2A 2A 2A"));
                    sleep(0.0003);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0095);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0087);
                    echoBus.send(Packet.parse("18ECFF03 [8] 20 11 00 03 FF EB FE 00"));
                    sleep(0.0012);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0095);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0023);
                    echoBus.send(Packet.parse("18FEBF0B [8] 00 00 7D 7D 7D 7D FF FF"));
                    sleep(0.0055);
                    echoBus.send(Packet.parse("18FECA0B [8] C3 FF 00 00 00 00 FF FF"));
                    sleep(0.0029);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0043);
                    echoBus.send(Packet.parse("18EBFF2A [8] 04 2A 2A FF FF FF FF FF"));
                    sleep(0.0028);
                    echoBus.send(Packet.parse("1CECFF0B [8] 20 1B 00 04 FF EB FE 00"));
                    sleep(0.0029);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0013);
                    echoBus.send(Packet.parse("1CEBFF8C [8] 03 2A 31 33 35 34 39 35"));
                    sleep(0.0014);
                    echoBus.send(Packet.parse("1CEBFF00 [8] 06 2A 2A 2A 2A 2A 2A 2A"));
                    sleep(0.0065);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0099);
                    echoBus.send(Packet.parse("18EBFF03 [8] 01 41 4C 4C 53 4E 2A 33"));
                    sleep(0.0009);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0094);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0096);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0107);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0073);
                    echoBus.send(Packet.parse("1CECFF21 [8] 20 0E 00 02 FF CA FE 00"));
                    sleep(0.0002);
                    echoBus.send(Packet.parse("1CEBFF0B [8] 01 42 4E 44 57 53 2A 45"));
                    sleep(0.0038);
                    echoBus.send(Packet.parse("18FFEB0B [8] 0B 0B 00 00 14 40 40 00"));
                    sleep(0.0014);
                    echoBus.send(Packet.parse("1CEBFF00 [8] 07 2A 2A FF FF FF FF FF"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
            long start = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    Packet p = stream.findFirst().get();
                    System.err.format("exists:  %d%n", System.currentTimeMillis() - start);
                    String timeString = p.toTimeString();
                    System.err.format("complete:  %d  %s%n", System.currentTimeMillis() - start, timeString);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }).start();
            RequestResult<ComponentIdentificationPacket> response = j1939.requestGlobal("test",
                                                                                        ComponentIdentificationPacket.class,
                                                                                        NOOP);
            System.err.format("### post %d%n  %s%n",
                              System.currentTimeMillis() - start,
                              response.getEither()
                                      .stream()
                                      .map(r -> r.left.map(p -> p.getPacket().toString() + "\n").orElse("ERROR"))
                                      .collect(Collectors.toList()));
            // only 4 complete messages are receive. (some duplicates)
            assertEquals(4, response.getEither().size());
        }
    }

    @Before
    public void setup() {
        when(bus.getAddress()).thenReturn(BUS_ADDR);

        sendPacketCaptor = ArgumentCaptor.forClass(Packet.class);
        instance = new J1939(bus);
    }

    @Test()
    @TestDoc(description = "6.1.4.1.b no retry on NACK")
    public void test6141bNoRetryOnNack() throws BusException {
        // verify 3 trys in 3*220ms
        Bus bus = new EchoBus(0xF9);
        bus.log(Packet::toTimeString);
        Stream<Packet> all = bus.read(1, TimeUnit.SECONDS);
        J1939 j1939 = new J1939(bus);
        Stream<Packet> stream = bus.read(200, TimeUnit.SECONDS);
        new Thread(() -> {
            try {
                stream.findFirst();
                bus.send(Packet.parsePacket("18E8F900 01 FF FF FF FF 00 A4 00"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        long start = System.currentTimeMillis();
        BusResult<DM30ScaledTestResultsPacket> requestDm7 = j1939.requestTestResults(247, 123, 31, 0, NOOP);
        long duration = System.currentTimeMillis() - start;
        Optional<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> result = requestDm7
                                                                                               .getPacket();

        assertTrue(result.isPresent());
        Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket> e = result.get();
        assertTrue(e.left.isEmpty());
        assertTrue(e.right.isPresent());
        // 200 ms seems excessive, but I was seeing weird timing in the tests
        // FIXME review.
        assertTrue("Took too long: " + duration, duration < 200);
        assertEquals(2L, all.count());
    }

    @Test(timeout = 4000)
    @TestDoc(description = "6.1.4.1.b retry on timeout of 220 ms")
    public void test6141bRetry() {
        // verify 3 trys in 3*220ms
        Bus bus = new EchoBus(0xF9);
        J1939 j1939 = new J1939(bus);
        long start = System.currentTimeMillis();
        var result = j1939.requestTestResults(247, 123, 31, 0, NOOP).getPacket();
        assertFalse(result.isPresent());
        assertEquals(TIMEOUT * 3, System.currentTimeMillis() - start, 40);
    }

    @Test(timeout = 4000)
    public void testDM7NACK() throws BusException {
        Bus bus = new EchoBus(0xF9);
        J1939 j1939 = new J1939(bus);
        Stream<Packet> stream = bus.read(200, TimeUnit.SECONDS);
        new Thread(() -> {
            try {
                stream.findFirst();
                bus.send(Packet.parsePacket("18E8F900 01 FF FF FF FF 00 E3 00"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        var result = j1939.requestTestResults(247, 123, 31, 0, NOOP).getPacket();
        assertEquals(AcknowledgmentPacket.Response.NACK, result.get().right.get().getResponse());
    }

    /**
     * The purpose of this test is to verify that processing doesn't hang on any
     * possible PGN
     */
    @Test
    @Ignore // very slow should only be run occasionally
    public void testAllPgns() throws Exception {
        EchoBus echoBus = new EchoBus(BUS_ADDR);
        J1939 j1939 = new J1939(echoBus);
        for (int id = 0; id < 0x1FFFFF; id++) {
            Packet packet = Packet.create(id, 0x17, 11, 22, 33, 44, 55, 66, 77, 88);
            Stream<?> stream = j1939.read();
            echoBus.send(packet);
            assertTrue("Failed on id " + id, stream.findFirst().isPresent());
        }
    }

    /**
     * Request the VIN with a long delay and verify the error.
     */
    @Test
    public void testBamTimeout() throws Exception {
        EchoBus bus2 = new EchoBus(0);
        try (Bus bus = new J1939TP(bus2, 0)) {

            Stream<Packet> requestStream = bus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // wait for request
                    requestStream.findAny();
                    // wait to cause warning
                    Thread.sleep(TIMEOUT + 50);
                    bus.send(Packet.create(VehicleIdentificationPacket.PGN,
                                           0xFF,
                                           1,
                                           2,
                                           3,
                                           4,
                                           5,
                                           6,
                                           7,
                                           8,
                                           9,
                                           10,
                                           11,
                                           12,
                                           13,
                                           14,
                                           15,
                                           16,
                                           17));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            TestResultsListener listener = new TestResultsListener();
            RequestResult<VehicleIdentificationPacket> response = new J1939(new J1939TP(bus2, 0xF9))
                                                                                                    .requestGlobal("VIN",
                                                                                                                   VehicleIdentificationPacket.class,
                                                                                                                   listener);
            assertEquals(0, response.getPackets().size());
            /* verify there is an error */
            assertTrue(listener.getResults()
                               .matches("(?s).*Timeout - No Response.*"));
        }
    }

    /** Request the VIN without a delay and verify no warning. */
    @Test
    public void testBamTimeoutNoWarn() throws Exception {
        EchoBus bus2 = new EchoBus(0);
        try (Bus bus = new J1939TP(bus2, 0)) {
            Stream<Packet> requestStream = bus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // wait for request
                    requestStream.findAny();
                    // Thread.sleep(300); test the test
                    bus.send(Packet.create(VehicleIdentificationPacket.PGN,
                                           0xFF,
                                           1,
                                           2,
                                           3,
                                           4,
                                           5,
                                           6,
                                           7,
                                           8,
                                           9,
                                           10,
                                           11,
                                           12,
                                           13,
                                           14,
                                           15,
                                           16,
                                           17));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            TestResultsListener listener = new TestResultsListener();
            RequestResult<VehicleIdentificationPacket> response = new J1939(new J1939TP(bus2, 0xF9))
                                                                                                    .requestGlobal("VIN",
                                                                                                                   VehicleIdentificationPacket.class,
                                                                                                                   listener);
            assertEquals(1, response.getPackets().size());
            /* verify there is not a warning. */
            assertFalse(listener.getResults().matches("(?s).*TIMING: Late BAM response.*"));
        }
    }

    /**
     * Request the VIN with a delay and verify the warning.
     */
    @Test
    public void testBamTimeoutWarn() throws Exception {
        EchoBus bus2 = new EchoBus(0);
        try (Bus bus = new J1939TP(bus2, 0)) {

            Stream<Packet> requestStream = bus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // wait for request
                    requestStream.findAny();
                    // wait to cause warning
                    Thread.sleep(300);
                    bus.send(Packet.create(VehicleIdentificationPacket.PGN,
                                           0xFF,
                                           1,
                                           2,
                                           3,
                                           4,
                                           5,
                                           6,
                                           7,
                                           8,
                                           9,
                                           10,
                                           11,
                                           12,
                                           13,
                                           14,
                                           15,
                                           16,
                                           17));
                    bus.send(Packet.create(VehicleIdentificationPacket.PGN,
                                           0xFF,
                                           1,
                                           2,
                                           3,
                                           4,
                                           5,
                                           6,
                                           7,
                                           8,
                                           9,
                                           10,
                                           11,
                                           12,
                                           13,
                                           14,
                                           15,
                                           16,
                                           17));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            TestResultsListener listener = new TestResultsListener();
            RequestResult<VehicleIdentificationPacket> response = new J1939(new J1939TP(bus2, 0xF9))
                                                                                                    .requestGlobal("VIN",
                                                                                                                   VehicleIdentificationPacket.class,
                                                                                                                   listener);
            assertEquals(2, response.getPackets().size());
            /* verify there is a warning */
            String results = listener.getResults();
            assertTrue(results.matches("(?s).*TIMING: Late response -  [\\d:.]+ 1CECFF00 \\[8] 20 11 00 03 FF EC FE 00.*"));
            /* and only a single warning */
            assertFalse(results.matches("(?s).*TIMING.*TIMING.*"));
        }
    }

    /**
     * Request the VIN with a delay and verify the warning.
     */
    @Test
    public void testDSTimeoutWarn() throws Exception {
        EchoBus bus2 = new EchoBus(0);
        bus2.log(p -> p.toTimeString());
        try (J1939TP bus = new J1939TP(bus2, 0)) {

            Stream<Packet> requestStream = bus.read(1, TimeUnit.HOURS);
            new Thread(() -> {
                try {
                    // wait for request
                    requestStream.findAny();
                    // wait to cause warning
                    Thread.sleep(300);
                    bus.sendDestinationSpecific(0xF9,
                                                Packet.create(VehicleIdentificationPacket.PGN,
                                                              0x00,
                                                              1,
                                                              2,
                                                              3,
                                                              4,
                                                              5,
                                                              6,
                                                              7,
                                                              8,
                                                              9,
                                                              10,
                                                              11,
                                                              12,
                                                              13,
                                                              14,
                                                              15,
                                                              16,
                                                              17));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            TestResultsListener listener = new TestResultsListener();
            new J1939(new J1939TP(bus2, 0xF9)).requestDS("VIN",
                                                         VehicleIdentificationPacket.class,
                                                         0,
                                                         listener);
            /* verify there is a warning */
            assertTrue(listener.getResults()
                               .matches(
                                        "(?s).*TIMING: Late response -  [\\d:.]+ 1CECF900 \\[8] 10 11 00 03 FF EC FE 00.*"));
        }
    }

    @Test
    public void testCreateRequestPacket() {
        Packet actual = instance.createRequestPacket(12345, 0x99);
        assertEquals(0xEA99, actual.getId(0xFFFF));
        assertEquals(BUS_ADDR, actual.getSource());
        assertEquals(12345, actual.get24(0));
    }

    @Test
    public void testRead() throws Exception {
        when(bus.read(365, TimeUnit.DAYS)).thenReturn(Stream.empty());
        instance.read();
        verify(bus).read(365, TimeUnit.DAYS);
    }

    @Test
    public void testReadByClass() throws Exception {
        Packet packet1 = Packet.create(EngineSpeedPacket.PGN, 0x00, 1, 2, 3, 4, 5, 6, 7, 8);
        Packet packet2 = Packet.create(VehicleIdentificationPacket.PGN, 0x00, 1, 2, 3, 4, 5, 6, 7, 8);
        when(bus.read(5000, TimeUnit.DAYS)).thenReturn(Stream.of(packet1, packet2, packet1, packet2, packet1, packet2));

        Stream<?> response = instance.read(EngineSpeedPacket.class, 5000, TimeUnit.DAYS);
        List<?> packets = response.collect(Collectors.toList());
        assertEquals(3, packets.size());
    }

    /**
     * This sends request for DM7 but times out
     */
    @Test
    public void testRequestDM7Timesout() throws Exception {
        Object packet = instance.requestTestResults(247, 1024, 31, 0, NOOP).getPacket().orElse(null);
        assertNull(packet);

        verify(bus, times(3)).send(sendPacketCaptor.capture());
        Packet request = sendPacketCaptor.getValue();
        assertEquals(DM7CommandTestsPacket.PGN, request.getPgn());
        assertEquals(BUS_ADDR, request.getSource());
    }

    /**
     * This sends request for DM7 and eventually gets back a DM30
     */
    @Test
    public void testRequestDM7WillTryThreeTimes() throws Exception {
        Packet packet1 = Packet.create(DM30ScaledTestResultsPacket.PGN
                | BUS_ADDR, 0x00, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0x0A, 0x0B, 0x0C, 0x0D);
        when(bus.read(TIMEOUT, MILLISECONDS)).thenReturn(Stream.of())
                                             .thenReturn(Stream.of())
                                             .thenReturn(Stream.of(packet1));

        Object packet = instance.requestTestResults(247, 1024, 31, 0, NOOP).getPacket().orElse(null);
        assertNotNull(packet);

        verify(bus, times(3)).send(sendPacketCaptor.capture());
        Packet request = sendPacketCaptor.getValue();
        assertEquals(DM7CommandTestsPacket.PGN, request.getPgn());
        assertEquals(BUS_ADDR, request.getSource());
    }

    @Test
    public void testRequestMultipleByClassHandlesException() {
        Stream<?> response = instance.requestGlobal(null, TestPacket.class, NOOP)
                                     .getEither()
                                     .stream();
        assertEquals(0, response.count());
    }

    @Test
    public void testRequestMultipleByClassReturnsAll() throws Exception {
        Packet packet1 = Packet.create(VehicleIdentificationPacket.PGN, 0x00, "EngineVIN*".getBytes(UTF8));
        Packet packet2 = Packet.create(VehicleIdentificationPacket.PGN, 0x17, "ClusterVIN*".getBytes(UTF8));
        Packet packet3 = Packet.create(VehicleIdentificationPacket.PGN, 0x21, "BodyControllerVIN*".getBytes(UTF8));
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                                                                                        .thenReturn(Stream.of(packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);

        Stream<VehicleIdentificationPacket> response = instance
                                                               .requestGlobal(null,
                                                                              VehicleIdentificationPacket.class,
                                                                              NOOP)
                                                               .getEither()
                                                               .stream()
                                                               .flatMap(e -> e.left.stream());
        List<VehicleIdentificationPacket> packets = response.collect(Collectors.toList());
        assertEquals(3, packets.size());
        assertEquals("EngineVIN", packets.get(0).getVin());
        assertEquals("ClusterVIN", packets.get(1).getVin());
        assertEquals("BodyControllerVIN", packets.get(2).getVin());

        verify(bus).send(request);
    }

    @Test
    public void testRequestMultipleHandlesBusException() throws Exception {
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                                                                                        .thenThrow(new BusException("Testing"));
        Stream<DM5DiagnosticReadinessPacket> response = instance.requestGlobal(null,
                                                                               DM5DiagnosticReadinessPacket.class,
                                                                               NOOP)
                                                                .getEither()
                                                                .stream()
                                                                .flatMap(e -> e.left.stream());
        assertEquals(0, response.count());
    }

    /** FIXME What is this? Looks like a DS test to 0, but sends to global. */
    @Test
    public void testRequestMultipleHandlesDSRequests() throws Exception {
        Packet packet = Packet.create(EngineHoursPacket.PGN, ENGINE_ADDR, 1, 2, 3, 4, 5, 6, 7, 8);
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                                                                                        .thenReturn(Stream.of(packet))
                                                                                        .thenReturn(Stream.of(packet))
                                                                                        .thenReturn(Stream.of(packet));

        Packet requestPacket = instance.createRequestPacket(EngineHoursPacket.PGN, ENGINE_ADDR);
        BusResult<EngineHoursPacket> response = instance.requestDS(null,
                                                                   EngineHoursPacket.class,
                                                                   ENGINE_ADDR,
                                                                   NOOP);
        assertEquals(3365299.25, response.getPacket().get().left.get().getEngineHours(), 0.0001);

        verify(bus).send(requestPacket);
    }

    @Test
    public void testRequestMultipleHandlesException() {
        Stream<TestPacket> response = instance.requestGlobal(null, TestPacket.class, NOOP)
                                              .getEither()
                                              .stream()
                                              .flatMap(e -> e.left.stream());
        assertEquals(0, response.count());
    }

    @Test
    public void testRequestMultipleHandlesTimeout() throws Exception {
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(Stream.empty())
                                                                                        .thenReturn(Stream.empty())
                                                                                        .thenReturn(Stream.empty());
        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);
        Stream<VehicleIdentificationPacket> response = instance.<VehicleIdentificationPacket>requestGlobal(null,
                                                                                                           J1939.getPgn(
                                                                                                                        VehicleIdentificationPacket.class),
                                                                                                           request,
                                                                                                           NOOP)
                                                               .getEither()
                                                               .stream()
                                                               .flatMap(e -> e.left.stream());
        assertEquals(0, response.count());
        verify(bus).send(request);
    }

    @Test
    public void testRequestMultipleIgnoresOtherPGNs() throws Exception {
        String expected = "12345678901234567890";
        Packet packet1 = Packet
                               .create(VehicleIdentificationPacket.PGN - 1,
                                       0x17,
                                       ("09876543210987654321*").getBytes(UTF8));
        Packet packet2 = Packet.create(VehicleIdentificationPacket.PGN, 0x17, (expected + "*").getBytes(UTF8));
        Packet packet3 = Packet
                               .create(VehicleIdentificationPacket.PGN + 2,
                                       0x17,
                                       ("alksdfjlasdjflkajsdf*").getBytes(UTF8));
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                                                                                        .thenReturn(Stream.of(packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);

        Stream<VehicleIdentificationPacket> response = instance.<VehicleIdentificationPacket>requestGlobal(null,
                                                                                                           J1939.getPgn(
                                                                                                                        VehicleIdentificationPacket.class),
                                                                                                           request,
                                                                                                           NOOP)
                                                               .getEither()
                                                               .stream()
                                                               .flatMap(e -> e.left.stream());
        List<VehicleIdentificationPacket> packets = response.collect(Collectors.toList());
        assertEquals(1, packets.size());
        assertEquals(expected, packets.get(0).getVin());

        verify(bus).send(request);
    }

    @Test
    public void testRequestMultipleReturnsAck() throws Exception {
        Packet packet1 = Packet.create(0xE8FF, 0x17, 0x01, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00);
        Packet packet2 = Packet.create(0xE8FF, 0x17, 0x00, 0xFF, 0xFF, 0xFF, 0x44, 0xD3, 0xFE, 0x00);
        Packet packet3 = Packet.create(0xEAFF, 0x44, 0x00, 0xFF, 0xFF, 0xFF);
        Packet packet4 = Packet.create(0xE8FF, 0x17, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00);
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                                                                                        .thenReturn(Stream.of(packet1,
                                                                                                              packet2,
                                                                                                              packet3,
                                                                                                              packet4));

        Packet requestPacket = instance.createRequestPacket(DM11ClearActiveDTCsPacket.PGN, GLOBAL_ADDR);

        List<AcknowledgmentPacket> responses = instance.<DM11ClearActiveDTCsPacket>requestGlobal(null,
                                                                                                 J1939.getPgn(
                                                                                                              DM11ClearActiveDTCsPacket.class),
                                                                                                 requestPacket,
                                                                                                 NOOP)
                                                       .getEither()
                                                       .stream()
                                                       .map(e -> (AcknowledgmentPacket) e.resolve())
                                                       .collect(Collectors.toList());
        assertEquals(2, responses.size());

        assertEquals("NACK", responses.get(0).getResponse().toString());
        // only the first is returned
        // assertEquals("ACK", responses.get(1).getResponse().toString());

        verify(bus).send(sendPacketCaptor.capture());
        List<Packet> packets = sendPacketCaptor.getAllValues();
        assertEquals(1, packets.size());
        Packet request = packets.get(0);
        assertEquals(0xEAFF, request.getId(0xFFFF));
        assertEquals(BUS_ADDR, request.getSource());
        assertEquals(DM11ClearActiveDTCsPacket.PGN, request.get24(0));
    }

    @Test
    public void testRequestMultipleReturnsAll() throws Exception {
        Packet packet1 = Packet.create(VehicleIdentificationPacket.PGN, 0x00, "EngineVIN*".getBytes(UTF8));
        Packet packet2 = Packet.create(VehicleIdentificationPacket.PGN, 0x17, "ClusterVIN*".getBytes(UTF8));
        Packet packet3 = Packet.create(VehicleIdentificationPacket.PGN, 0x21, "BodyControllerVIN*".getBytes(UTF8));
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                                                                                        .thenReturn(Stream.of(packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);
        Stream<VehicleIdentificationPacket> response = instance.<VehicleIdentificationPacket>requestGlobal(null,
                                                                                                           J1939.getPgn(
                                                                                                                        VehicleIdentificationPacket.class),
                                                                                                           request,
                                                                                                           NOOP)
                                                               .getEither()
                                                               .stream()
                                                               .flatMap(e -> e.left.stream());
        List<VehicleIdentificationPacket> packets = response.collect(Collectors.toList());
        assertEquals(3, packets.size());
        assertEquals("EngineVIN", packets.get(0).getVin());
        assertEquals("ClusterVIN", packets.get(1).getVin());
        assertEquals("BodyControllerVIN", packets.get(2).getVin());

        verify(bus).send(request);
    }

    final private static class TestPacket extends GenericPacket {
        // used by tests in getPgn(Packet)
        @SuppressWarnings("unused")
        public static int PGN = -1;

        public TestPacket(Packet packet) {
            super(packet);
        }
    }

}
