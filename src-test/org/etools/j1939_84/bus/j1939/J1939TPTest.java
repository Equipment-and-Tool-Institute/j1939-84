package org.etools.j1939_84.bus.j1939;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939TP.CtsBusException;
import org.junit.Test;

public class J1939TPTest {
    @Test
    public void testBam() throws BusException {
        Packet testPacket = Packet.parsePacket(
                "18FFEE00 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09");
        Collection<Packet> expectedResult = Packet.parseCollection("18ECFF00 20 00 28 06 FF EE FF 00\n" +
                "18EBFF00 01 00 01 02 03 04 05 06\n" +
                "18EBFF00 02 07 08 09 00 01 02 03\n" +
                "18EBFF00 03 04 05 06 07 08 09 00\n" +
                "18EBFF00 04 01 02 03 04 05 06 07\n" +
                "18EBFF00 05 08 09 00 01 02 03 04\n" +
                "18EBFF00 06 05 06 07 08 09 FF FF");
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            // test tp bam send
            {
                Stream<Packet> packetStream = bus.read(1, TimeUnit.SECONDS);
                tp.sendBam(testPacket);
                assertEquals(expectedResult.toString(), packetStream.collect(Collectors.toList()).toString());
            }
            // test tp bam send and read
            try (J1939TP receivingTp = new J1939TP(bus, 0xF9)) {
                Stream<Packet> tpStream = receivingTp.read(2, TimeUnit.SECONDS);
                {
                    Stream<Packet> packetStream = bus.read(1, TimeUnit.SECONDS);
                    tp.sendBam(testPacket);
                    assertEquals(expectedResult.toString(), packetStream.collect(Collectors.toList()).toString());
                }
                assertEquals(testPacket.toString(), tpStream.findFirst().map(p -> p.toString()).orElse("FAIL"));
            }
        }
    }

    @Test()
    public void testBamMissingDt() throws BusException, InterruptedException {
        Collection<Packet> expectedPacket = Collections.singletonList(Packet.parsePacket(
                "18FFEE00 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09"));
        List<Packet> testPacket = new ArrayList<>(Packet.parseCollection("18ECFF00 20 00 28 06 FF EE FF 00\n" +
                "18EBFF00 01 00 01 02 03 04 05 06\n" +
                "18EBFF00 02 07 08 09 00 01 02 03\n" +
                "18EBFF00 03 04 05 06 07 08 09 00\n" +
                "18EBFF00 04 01 02 03 04 05 06 07\n" +
                "18EBFF00 05 08 09 00 01 02 03 04\n" +
                "18EBFF00 06 05 06 07 08 09 FF FF"));
        try (EchoBus bus = new EchoBus(0);
                J1939TP tp = new J1939TP(bus, 0xF9)) {
            {
                Stream<Packet> packetStream = tp.read(1, TimeUnit.SECONDS);
                for (Packet p : testPacket) {
                    bus.send(p);
                    Thread.sleep(50);
                }
                assertEquals(expectedPacket.toString(), packetStream.collect(Collectors.toList()).toString());
            }
            { // should fail
                testPacket.remove(3);
                Stream<Packet> packetStream = tp.read(1, TimeUnit.SECONDS);
                for (Packet p : testPacket) {
                    bus.send(p);
                    Thread.sleep(50);
                }
                assertTrue(packetStream.collect(Collectors.toList()).isEmpty());
            }
        }
    }

    @Test
    public void testBus() {
        // verify that the bus works as expected
        try (EchoBus bus = new EchoBus(0)) {
            int count = 10;
            Stream<Packet> streamA = bus.read(1, TimeUnit.SECONDS).limit(count - 1);
            Stream<Packet> streamB = bus.read(1, TimeUnit.SECONDS).limit(count);
            for (int i = 0; i < count; i++) {
                bus.send(Packet.create(0xFF00 | i, 2, 0, 1, 2, 3, 4, 5, 6, 7));
            }
            assertEquals(count - 1, streamA.collect(Collectors.toList()).size());
            assertEquals(count, streamB.collect(Collectors.toList()).size());
        }
    }

    @Test
    public void testMissingDT() throws BusException {
        try (Bus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0);) {
            bus.log("b:");

            Stream<Packet> s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            assertEquals("18ECF900 11 02 01 FF FF 00 EA 00", s.findFirst().get().toString());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            bus.send(Packet.parse("18EB00F9 02 01 02 03 04 05 06 07"));
            assertEquals("18ECF900 11 01 01 FF FF 00 EA 00", s.findFirst().get().toString());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            bus.send(Packet.parse("18EB00F9 01 01 02 03 04 05 06 07"));
            assertEquals("18ECF900 13 00 09 02 FF 00 EA 00", s.findFirst().get().toString());
        }
    }

    @Test
    public void testMultipleCTS() throws BusException {
        try (EchoBus bus = new EchoBus(0);
                Bus tp = new J1939TP(bus, 0xF9);) {
            bus.log(":");
            Stream<Packet> tpStream = tp.read(1, TimeUnit.SECONDS);
            // send first CTS
            bus.send(Packet.parse("18ECF900 10 00 0E 03 FF F9 12 00"));
            // send full TP
            Packet p = Packet.parse("1812F900 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D");
            try (J1939TP tpOut = new J1939TP(bus)) {
                tpOut.send(p);
            }
            assertEquals(Collections.singletonList(p), tpStream.collect(Collectors.toList()));
        }
    }

    @Test
    public void testNonBus() throws BusException {
        // baseline test verifying that the bus works.
        try (Bus bus = new EchoBus(0xF9)) {
            Stream<Packet> s = bus.read(1, TimeUnit.SECONDS);
            Packet p = Packet.create(0xFF, 0xF9, 1, 2, 3, 4);
            bus.send(p);
            assertEquals(Collections.singletonList(p), s.collect(Collectors.toList()));
        }
    }

    @Test
    public void testNonTPOnTPBus() throws BusException {
        // verify that the tp bus will pass non-tp traffic.
        try (EchoBus bus = new EchoBus(0);
                Bus tp = new J1939TP(bus, 0xF9);) {
            { // tool to ECM
                Packet p = Packet.create(0xFFFF, 0xF9, 1, 2, 3, 4);
                Stream<Packet> busStream = bus.read(1, TimeUnit.SECONDS);
                tp.send(p);
                assertEquals(Collections.singletonList(p), busStream.collect(Collectors.toList()));
            }
            { // ECM to tool
                Packet p = Packet.create(0xFFFF, 0, 1, 2, 3, 4);
                Stream<Packet> tpStream = tp.read(1, TimeUnit.SECONDS);
                bus.send(p);
                assertEquals(Collections.singletonList(p), tpStream.collect(Collectors.toList()));
            }
        }
    }

    @Test
    public void testOutOfOrderDT() throws BusException {
        try (Bus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0);) {
            Stream<Packet> s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            assertEquals("18ECF900 11 02 01 FF FF 00 EA 00", s.findFirst().get().toString());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            bus.send(Packet.parse("18EB00F9 02 08 09 00 00 00 00 00"));
            bus.send(Packet.parse("18EB00F9 01 01 02 03 04 05 06 07"));
            assertEquals("18ECF900 13 00 09 02 FF 00 EA 00", s.findFirst().get().toString());
        }
    }

    @Test
    public void testSimpleTP() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tpIn = new J1939TP(bus, 0);
                J1939TP tpOut = new J1939TP(bus, 0xF9);) {

            // a stream of all the fragements
            Stream<Packet> stream = bus.read(100, TimeUnit.MILLISECONDS);

            // a strem with only the result
            Stream<Packet> tpStream = tpIn.read(3, TimeUnit.DAYS).limit(1);

            tpOut.send(Packet.create(0xEA00, 0xF9, 1, 2, 3, 4, 5, 6, 7, 8, 9));

            assertEquals(Packet.parseCollection("18EC00F9 10 00 09 02 FF 00 EA 00\n" +
                    "18ECF900 11 02 01 FF FF 00 EA 00\n" +
                    "18EB00F9 01 01 02 03 04 05 06 07\n" +
                    "18EB00F9 02 08 09 00 00 00 00 00\n" +
                    "18ECF900 13 00 09 02 FF 00 EA 00").toString(),
                    stream.collect(Collectors.toList()).toString());
            assertEquals(Collections.singletonList(Packet.create(0xEA00, 0xF9, 1, 2, 3, 4, 5, 6, 7, 8, 9)).toString(),
                    tpStream.collect(Collectors.toList()).toString());
        }
    }

    @Test()
    public void testT2T1() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            Iterator<Packet> it = bus.read(5, TimeUnit.SECONDS).filter(p -> p.getSource() == 0).iterator();
            long start = System.currentTimeMillis();

            // send RTS
            bus.send(Packet.create(0xEC00, 0xF9, 0x10, 0x00, 0x09, 0x02, 0xFF, 0x00, 0xEA, 0x00));

            // verify CTS is for packet 1
            Packet cts1 = it.next();
            assertEquals(J1939TP.CM_CTS, cts1.get(0));
            assertEquals(1, cts1.get(2));

            // test T2
            { // verify CTS is for packet 1
                Packet cts2 = it.next();
                assertEquals(J1939TP.CM_CTS, cts2.get(0));
                assertEquals(1, cts2.get(2));
                assertTrue("T2 timing too fast", System.currentTimeMillis() - start > J1939TP.T2);
            }

            // send packet 1
            start = System.currentTimeMillis();
            bus.send(Packet.create(J1939TP.DT | 0, 0xF9, 1, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55));

            // test T1
            { // verify CTS is for packet 2
                Packet cts3 = it.next();
                assertEquals(J1939TP.CM_CTS, cts3.get(0));
                assertEquals(2, cts3.get(2));
                assertEquals("T1 timing wrong", System.currentTimeMillis() - start, J1939TP.T1, 20);
            }

            // verify that no TP packet is decoded
            Optional<Packet> result = tp.read(5, TimeUnit.SECONDS).findAny();
            assertFalse("result: " + result, result.isPresent());
        }
    }

    @Test(expected = CtsBusException.class)
    public void testT3() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            tp.send(Packet.create(0xFF00 | 0xF9, 2, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        }
    }

    @Test
    public void testT4() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            long start = System.currentTimeMillis();
            Stream<Packet> waitForRts = bus.read(5, TimeUnit.SECONDS).limit(1);
            CompletableFuture<Object> sender = CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            tp.send(Packet.create(0x12F9, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
                        } catch (BusException e1) {
                        }
                        return null;
                    });
            assertTrue(waitForRts.findFirst().isPresent());

            // send the "hold he connection open" CTS
            bus.send(Packet.create(0xEC00 | 0x00, 0xF9, J1939TP.CM_CTS, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));
            sender.join();
            assertEquals("T4 pause was incorrect",
                    J1939TP.T4,
                    System.currentTimeMillis() - start,
                    150);
        }
    }
}
