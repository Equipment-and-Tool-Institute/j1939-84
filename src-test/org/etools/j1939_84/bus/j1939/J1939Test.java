/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import static org.etools.j1939_84.bus.j1939.J1939.ENGINE_ADDR;
import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineSpeedPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.junit.Before;
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
@RunWith(MockitoJUnitRunner.class)
public class J1939Test {

    final private static class TestPacket extends ParsedPacket {
        // used by tests in getPgn(Packet)
        @SuppressWarnings("unused")
        public static int PGN = -1;

        public TestPacket(Packet packet) {
            super(packet);
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

    @Before
    public void setup() {
        when(bus.getAddress()).thenReturn(BUS_ADDR);

        sendPacketCaptor = ArgumentCaptor.forClass(Packet.class);
        instance = new J1939(bus);
    }

    /**
     * The purpose of this test is to verify that processing doesn't hang on any
     * possible PGN
     *
     * @throws Exception
     */
    @Test
    public void testAllPgns() throws Exception {
        EchoBus echoBus = new EchoBus(BUS_ADDR);
        J1939 j1939 = new J1939(echoBus);
        for (int id = 0; id < 0x1FFFFF; id++) {
            Packet packet = Packet.create(id, 0x17, 11, 22, 33, 44, 55, 66, 77, 88);
            Stream<ParsedPacket> stream = j1939.read();
            echoBus.send(packet);
            assertTrue("Failed on id " + id, stream.findFirst().isPresent());
        }
    }

    @Test
    public void testCreateRequestPacket() {
        Packet actual = instance.createRequestPacket(12345, 0x99);
        assertEquals(0xEA99, actual.getId());
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

        Stream<EngineSpeedPacket> response = instance.read(EngineSpeedPacket.class, 5000, TimeUnit.DAYS);
        List<EngineSpeedPacket> packets = response.collect(Collectors.toList());
        assertEquals(1, packets.size());
    }

    /**
     * This sends request for DM7 but gets back a DM30
     *
     * @throws Exception
     */
    @Test
    public void testRequestDM7() throws Exception {
        Packet packet1 = Packet.create(DM30ScaledTestResultsPacket.PGN
                | 0xA5, 0x00, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0x0A, 0x0B, 0x0C, 0x0D);
        when(bus.read(2500, TimeUnit.MILLISECONDS)).thenReturn(Stream.of(packet1));

        int spn = 1024;

        Packet requestPacket = Packet.create(DM7CommandTestsPacket.PGN
                | 0x00, BUS_ADDR, 247, spn & 0xFF, (spn >> 8) & 0xFF, (spn >> 16) & 0xFF | 31, 0xFF, 0xFF, 0xFF, 0xFF);

        DM30ScaledTestResultsPacket packet = instance
                .requestPacket(requestPacket, DM30ScaledTestResultsPacket.class, 0x00, 4).orElse(null);
        assertNotNull(packet);

        verify(bus).send(sendPacketCaptor.capture());
        Packet request = sendPacketCaptor.getValue();
        assertEquals(DM7CommandTestsPacket.PGN, request.getId());
        assertEquals(BUS_ADDR, request.getSource());
    }

    /**
     * This sends request for DM7 but times out
     *
     * @throws Exception
     */
    @Test
    public void testRequestDM7Timesout() throws Exception {
        when(bus.read(2500, TimeUnit.MILLISECONDS)).thenReturn(Stream.of()).thenReturn(Stream.of())
                .thenReturn(Stream.of());

        int spn = 1024;

        Packet requestPacket = Packet.create(DM7CommandTestsPacket.PGN
                | 0x00, BUS_ADDR, 247, spn & 0xFF, (spn >> 8) & 0xFF, (spn >> 16) & 0xFF | 31, 0xFF, 0xFF, 0xFF, 0xFF);

        DM30ScaledTestResultsPacket packet = instance
                .requestPacket(requestPacket, DM30ScaledTestResultsPacket.class, 0x00, 3).orElse(null);
        assertNull(packet);

        verify(bus, times(3)).send(sendPacketCaptor.capture());
        Packet request = sendPacketCaptor.getValue();
        assertEquals(DM7CommandTestsPacket.PGN, request.getId());
        assertEquals(BUS_ADDR, request.getSource());
    }

    /**
     * This sends request for DM7 and eventually gets back a DM30
     *
     * @throws Exception
     */
    @Test
    public void testRequestDM7WillTryThreeTimes() throws Exception {
        Packet packet1 = Packet.create(DM30ScaledTestResultsPacket.PGN
                | 0xA5, 0x00, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0x0A, 0x0B, 0x0C, 0x0D);
        when(bus.read(2500, TimeUnit.MILLISECONDS)).thenReturn(Stream.of()).thenReturn(Stream.of())
                .thenReturn(Stream.of(packet1));

        int spn = 1024;

        Packet requestPacket = Packet.create(DM7CommandTestsPacket.PGN
                | 0x00, BUS_ADDR, 247, spn & 0xFF, (spn >> 8) & 0xFF, (spn >> 16) & 0xFF | 31, 0xFF, 0xFF, 0xFF, 0xFF);

        DM30ScaledTestResultsPacket packet = instance
                .requestPacket(requestPacket, DM30ScaledTestResultsPacket.class, 0x00, 3).orElse(null);
        assertNotNull(packet);

        verify(bus, times(3)).send(sendPacketCaptor.capture());
        Packet request = sendPacketCaptor.getValue();
        assertEquals(DM7CommandTestsPacket.PGN, request.getId());
        assertEquals(BUS_ADDR, request.getSource());
    }

    @Test
    public void testRequestMultipleByClassHandlesException() throws Exception {
        Stream<TestPacket> response = instance.requestMultiple(TestPacket.class);
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

        Stream<VehicleIdentificationPacket> response = instance.requestMultiple(VehicleIdentificationPacket.class);
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
        Stream<DM5DiagnosticReadinessPacket> response = instance.requestMultiple(DM5DiagnosticReadinessPacket.class,
                request);
        assertEquals(0, response.count());
    }

    @Test
    public void testRequestMultipleHandlesDSRequests() throws Exception {
        Packet packet = Packet.create(EngineHoursPacket.PGN, 0x00, 1, 2, 3, 4, 5, 6, 7, 8);
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(Stream.of(packet));

        Packet requestPacket = instance.createRequestPacket(EngineHoursPacket.PGN, ENGINE_ADDR);

        Stream<EngineHoursPacket> response = instance.requestMultiple(EngineHoursPacket.class, requestPacket);
        List<EngineHoursPacket> packets = response.collect(Collectors.toList());
        assertEquals(1, packets.size());
        assertEquals(3365299.25, packets.get(0).getEngineHours(), 0.0001);

        verify(bus).send(requestPacket);
    }

    @Test
    public void testRequestMultipleHandlesException() throws Exception {
        Stream<TestPacket> response = instance.requestMultiple(TestPacket.class,
                Packet.create(0xEA00 | 0, 0, 0, 0 >> 8, 0 >> 16));
        assertEquals(0, response.count());
    }

    @Test
    public void testRequestMultipleHandlesTimeout() throws Exception {
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());
        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);
        Stream<VehicleIdentificationPacket> response = instance.requestMultiple(VehicleIdentificationPacket.class,
                request);
        assertEquals(0, response.count());
        verify(bus, times(3)).send(request);
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

        Stream<VehicleIdentificationPacket> response = instance.requestMultiple(VehicleIdentificationPacket.class,
                request);
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

        List<AcknowledgmentPacket> responses = instance.requestMultiple(AcknowledgmentPacket.class, requestPacket)
                .collect(Collectors.toList());
        assertEquals(3, responses.size());

        assertEquals("NACK", responses.get(0).getResponse().toString());
        assertEquals("ACK", responses.get(1).getResponse().toString());
        assertEquals("ACK", responses.get(2).getResponse().toString());

        verify(bus).send(sendPacketCaptor.capture());
        List<Packet> packets = sendPacketCaptor.getAllValues();
        assertEquals(1, packets.size());
        Packet request = packets.get(0);
        assertEquals(0xEAFF, request.getId());
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
        Stream<VehicleIdentificationPacket> response = instance.requestMultiple(VehicleIdentificationPacket.class,
                request);
        List<VehicleIdentificationPacket> packets = response.collect(Collectors.toList());
        assertEquals(3, packets.size());
        assertEquals("EngineVIN", packets.get(0).getVin());
        assertEquals("ClusterVIN", packets.get(1).getVin());
        assertEquals("BodyControllerVIN", packets.get(2).getVin());

        verify(bus).send(request);
    }

    @Test
    public void testRequestMultipleTriesAgain() throws Exception {
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(Stream.empty())
                .thenReturn(Stream.of(Packet.create(VehicleIdentificationPacket.PGN, 0x00, "*".getBytes(UTF8))));
        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);
        Stream<VehicleIdentificationPacket> response = instance.requestMultiple(VehicleIdentificationPacket.class,
                request);
        assertEquals(1, response.count());
        verify(bus, times(2)).send(request);
    }

    @Test
    public void testRequestMultipleTriesThreeTimes() throws Exception {
        when(bus.read(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(Stream.empty())
                .thenReturn(Stream.empty())
                .thenReturn(Stream.of(Packet.create(VehicleIdentificationPacket.PGN, 0x00, "*".getBytes(UTF8))));
        Packet request = instance.createRequestPacket(VehicleIdentificationPacket.PGN, 0xFF);
        Stream<VehicleIdentificationPacket> response = instance.requestMultiple(VehicleIdentificationPacket.class,
                request);
        assertEquals(1, response.count());
        verify(bus, times(3)).send(request);
    }

    @Test
    public void testRequestRawReturns() throws Exception {
        // NACK to addr
        Packet response1 = Packet.create(0xE8FF, 0x01, 0, 0, 0, 0, BUS_ADDR, 0xD3, 0xFE, 0x00);
        // NACK to global
        Packet response2 = Packet.create(0xE8FF, 0x01, 0, 0, 0, 0, 0xFF, 0xD3, 0xFE, 0x00);
        // NACK to addr DS
        Packet response3 = Packet.create(0xE8A5, 0x01, 0, 0, 0, 0, BUS_ADDR, 0xD3, 0xFE, 0x00);
        // NACK to global DS
        Packet response4 = Packet.create(0xE8A5, 0x01, 0, 0, 0, 0, 0xFF, 0xD3, 0xFE, 0x00);
        // ACK to addr
        Packet response5 = Packet.create(0xE8FF, 0x00, 0, 0, 0, 0, BUS_ADDR, 0xD3, 0xFE, 0x00);
        // ACK to global
        Packet response6 = Packet.create(0xE8FF, 0x00, 0, 0, 0, 0, 0xFF, 0xD3, 0xFE, 0x00);
        // ACK to addr DS
        Packet response7 = Packet.create(0xE8A5, 0x00, 0, 0, 0, 0, BUS_ADDR, 0xD3, 0xFE, 0x00);
        // ACK to global DS
        Packet response8 = Packet.create(0xE8A5, 0x00, 0, 0, 0, 0, 0xFF, 0xD3, 0xFE, 0x00);
        // Junk to different PGN
        Packet response9 = Packet.create(0xE8FF, 0x00, 0, 0, 0, 0, 0xFF, 0xFA, 0xFE, 0x00);
        // Junk with different pgn
        Packet response10 = Packet.create(0xF004, 0x00, 0, 0, 0, 0, 0xFF, 0xD3, 0xFE, 0x00);

        when(bus.read(5500, TimeUnit.MILLISECONDS)).thenReturn(Stream.of(response1,
                response2,
                response3,
                response4,
                response5,
                response6,
                response7,
                response8,
                response9,
                response10));

        Packet request = instance.createRequestPacket(DM11ClearActiveDTCsPacket.PGN, GLOBAL_ADDR);
        List<ParsedPacket> packets = instance
                .requestRaw(AcknowledgmentPacket.class, request, 5500, TimeUnit.MILLISECONDS)
                .collect(Collectors.toList());

        assertEquals(9, packets.size());
    }

}
