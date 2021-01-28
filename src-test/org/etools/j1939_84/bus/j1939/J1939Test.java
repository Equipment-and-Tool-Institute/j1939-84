/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import static org.etools.j1939_84.bus.j1939.J1939.ENGINE_ADDR;
import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;
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
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineSpeedPacket;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.etools.testdoc.TestDoc;
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
 *
 */
@SuppressWarnings("ConstantConditions") @RunWith(MockitoJUnitRunner.class)
public class J1939Test {

    final private static class TestPacket extends GenericPacket {
        // used by tests in getPgn(Packet)
        @SuppressWarnings("unused")
        public static int PGN = -1;

        public TestPacket(Packet packet) {
            super(packet, null);
        }
    }

    /**
     * The address of the tool on the bus - for testing. This is NOT the right
     * service tool address to confirm it's not improperly hard-coded (because
     * it was)
     *
     */
    private static final int BUS_ADDR = 0xA5;

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Mock
    private Bus bus;

    private J1939 instance;

    private ArgumentCaptor<Packet> sendPacketCaptor;

    @SuppressWarnings("OptionalGetWithoutIsPresent") @Test
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
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 01 53 6F 6D 65 20 56 49"));
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 02 4E 73 20 61 72 65 20"));
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 03 67 61 72 62 61 67 65"));
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 04 2C 20 62 75 74 20 74"));
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 05 68 69 73 20 74 65 73"));
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 06 74 20 64 6F 65 73 6E"));
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 07 27 74 20 63 61 72 65"));
                    Thread.sleep(100);
                    echoBus.send(Packet.parse("18EBFF00 08 2E FF FF FF FF FF FF"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            System.err.format("### pre %d%n", System.currentTimeMillis());
            BusResult<VehicleIdentificationPacket> response = j1939.requestDS("test", ResultsListener.NOOP, true,
                    VehicleIdentificationPacket.class,
                    J1939.createRequestPacket(VehicleIdentificationPacket.PGN, 0, 0xF9));
            System.err.format("### post %d%n", System.currentTimeMillis());
            final String vin2 = response.getPacket().get().left.get().getVin();
            System.err.format("### vin %d%n", System.currentTimeMillis());
            assertEquals(VIN, vin2);
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
        BusResult<DM30ScaledTestResultsPacket> requestDm7 = j1939
                .requestDm7(null, ResultsListener.NOOP, j1939.createRequestPacket(DM24SPNSupportPacket.PGN, 0));
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

    @Test(timeout = 2000)
    @TestDoc(description = "6.1.4.1.b retry on timeout of 220 ms")
    public void test6141bRetry() {
        // verify 3 trys in 3*220ms
        Bus bus = new EchoBus(0xF9);
        J1939 j1939 = new J1939(bus);
        long start = System.currentTimeMillis();
        Optional<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> result = j1939
                .requestDm7(null, ResultsListener.NOOP, j1939.createRequestPacket(DM24SPNSupportPacket.PGN, 0))
                .getPacket();
        assertFalse(result.isPresent());
        assertEquals(220 * 3, System.currentTimeMillis() - start, 40);
    }

    /**
     * The purpose of this test is to verify that processing doesn't hang on any
     * possible PGN
     */
    @Test
    @Ignore
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
        when(bus.read(220, TimeUnit.MILLISECONDS)).thenReturn(Stream.of()).thenReturn(Stream.of())
                .thenReturn(Stream.of());

        int spn = 1024;

        Packet requestPacket = Packet.create(DM7CommandTestsPacket.PGN, BUS_ADDR, 247, spn & 0xFF, (spn >> 8) & 0xFF, (spn >> 16) & 0xFF | 31, 0xFF, 0xFF, 0xFF, 0xFF);

        Object packet = instance.requestDm7(null, ResultsListener.NOOP, requestPacket).getPacket().orElse(null);
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
        when(bus.read(220, TimeUnit.MILLISECONDS)).thenReturn(Stream.of()).thenReturn(Stream.of())
                .thenReturn(Stream.of(packet1));

        int spn = 1024;

        Packet requestPacket = Packet.create(DM7CommandTestsPacket.PGN, BUS_ADDR, 247, spn & 0xFF, (spn >> 8) & 0xFF, (spn >> 16) & 0xFF | 31, 0xFF, 0xFF, 0xFF, 0xFF);

        Object packet = instance.requestDm7(null, ResultsListener.NOOP, requestPacket).getPacket().orElse(null);
        assertNotNull(packet);

        verify(bus, times(3)).send(sendPacketCaptor.capture());
        Packet request = sendPacketCaptor.getValue();
        assertEquals(DM7CommandTestsPacket.PGN, request.getPgn());
        assertEquals(BUS_ADDR, request.getSource());
    }

    @Test
    public void testRequestMultipleByClassHandlesException() {
        Stream<?> response = instance.requestGlobalResult(null, ResultsListener.NOOP, false, TestPacket.class)
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
                .thenReturn(Stream.of(packet1, packet2, packet3));

        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);

        Stream<VehicleIdentificationPacket> response = instance
                .requestGlobalResult(null, ResultsListener.NOOP, false, VehicleIdentificationPacket.class)
                .getEither().stream()
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
        Packet request = instance.createRequestPacket(DM5DiagnosticReadinessPacket.PGN, 0x00);
        Stream<DM5DiagnosticReadinessPacket> response = instance
                .requestResult(null, ResultsListener.NOOP, false, DM5DiagnosticReadinessPacket.class,
                        request)
                .getEither().stream().flatMap(e -> e.left.stream());
        assertEquals(0, response.count());
    }

    @Test
    public void testRequestMultipleHandlesDSRequests() throws Exception {
        Packet packet = Packet.create(EngineHoursPacket.PGN, ENGINE_ADDR, 1, 2, 3, 4, 5, 6, 7, 8);
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(Stream.of(packet))
                .thenReturn(Stream.of(packet))
                .thenReturn(Stream.of(packet));

        Packet requestPacket = instance.createRequestPacket(EngineHoursPacket.PGN, ENGINE_ADDR);
        RequestResult<EngineHoursPacket> packets = instance.requestResult(null, ResultsListener.NOOP, false,
                EngineHoursPacket.class,
                requestPacket);
        assertEquals(1, packets.getPackets().size());
        assertEquals(3365299.25, packets.getPackets().get(0).getEngineHours(), 0.0001);

        verify(bus).send(requestPacket);
    }

    @Test
    public void testRequestMultipleHandlesException() {
        Stream<TestPacket> response = instance.requestResult(null, ResultsListener.NOOP, false, TestPacket.class,
                Packet.create(0xEA00, 0, 0, 0, 0)).getEither().stream().flatMap(e -> e.left.stream());
        assertEquals(0, response.count());
    }

    @Test
    public void testRequestMultipleHandlesTimeout() throws Exception {
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());
        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);
        Stream<VehicleIdentificationPacket> response = instance
                .requestGlobal(null, ResultsListener.NOOP, false, VehicleIdentificationPacket.class, request)
                .getEither()
                .stream()
                .flatMap(e -> e.left.stream());
        assertEquals(0, response.count());
        verify(bus, times(2)).send(request);
    }

    @Test
    public void testRequestMultipleIgnoresOtherPGNs() throws Exception {
        String expected = "12345678901234567890";
        Packet packet1 = Packet
                .create(VehicleIdentificationPacket.PGN - 1, 0x17, ("09876543210987654321*").getBytes(UTF8));
        Packet packet2 = Packet.create(VehicleIdentificationPacket.PGN, 0x17, (expected + "*").getBytes(UTF8));
        Packet packet3 = Packet
                .create(VehicleIdentificationPacket.PGN + 2, 0x17, ("alksdfjlasdjflkajsdf*").getBytes(UTF8));
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(Stream.of(packet1, packet2, packet3));

        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);

        Stream<VehicleIdentificationPacket> response = instance
                .requestGlobal(null, ResultsListener.NOOP, false, VehicleIdentificationPacket.class, request)
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
        final Packet packet1 = Packet.create(0xE8FF, 0x17, 0x01, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00);
        final Packet packet2 = Packet.create(0xE8FF, 0x17, 0x00, 0xFF, 0xFF, 0xFF, 0x44, 0xD3, 0xFE, 0x00);
        final Packet packet3 = Packet.create(0xEAFF, 0x44, 0x00, 0xFF, 0xFF, 0xFF);
        final Packet packet4 = Packet.create(0xE8FF, 0x17, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00);
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenReturn(Stream.of(packet1, packet2, packet3, packet4));

        Packet requestPacket = instance.createRequestPacket(DM11ClearActiveDTCsPacket.PGN, GLOBAL_ADDR);

        List<AcknowledgmentPacket> responses = instance
                .requestGlobal(null, ResultsListener.NOOP, false, DM11ClearActiveDTCsPacket.class, requestPacket)
                .getEither()
                .stream()
                .map(e -> (AcknowledgmentPacket) e.resolve())
                .collect(Collectors.toList());
        assertEquals(2, responses.size());

        assertEquals("NACK", responses.get(0).getResponse().toString());
        assertEquals("ACK", responses.get(1).getResponse().toString());

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
                .thenReturn(Stream.of(packet1, packet2, packet3));

        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);
        Stream<VehicleIdentificationPacket> response = instance
                .requestGlobal(null, ResultsListener.NOOP, false, VehicleIdentificationPacket.class, request)
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

}
