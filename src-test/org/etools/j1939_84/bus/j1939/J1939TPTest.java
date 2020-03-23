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
import org.junit.Assert;
import org.junit.Test;

public class J1939TPTest {

    static private void assertPacketsEqual(Collection<Packet> expected, Collection<Packet> actual) {
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    static private void assertPacketsEqual(Packet expected, Packet actual) {
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void test3CtsWithoutData() {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            Stream<Packet> s = bus.read(J1939TP.T1 * 5, TimeUnit.MILLISECONDS);
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            // verify exactly 3 CTS
            assertEquals(3, s.filter(p -> p.toString().startsWith("18ECF900 11")).count());
        }
    }

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
                {// test raw packets
                    Stream<Packet> packetStream = bus.read(1, TimeUnit.SECONDS);
                    tp.sendBam(testPacket);
                    assertEquals(expectedResult.toString(), packetStream.collect(Collectors.toList()).toString());
                }
                // test TP
                assertPacketsEqual(testPacket, tpStream.findFirst().orElse(null));
            }
        }
    }

    @Test()
    public void testBamMissingDt() throws BusException, InterruptedException {
        Collection<Packet> expectedPacket = Collections.singletonList(Packet.parsePacket(
                "18FFEE00 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09"));
        List<Packet> testPackets = new ArrayList<>(Packet.parseCollection("18ECFF00 20 00 28 06 FF EE FF 00\n" +
                "18EBFF00 01 00 01 02 03 04 05 06\n" +
                "18EBFF00 02 07 08 09 00 01 02 03\n" +
                "18EBFF00 03 04 05 06 07 08 09 00\n" +
                "18EBFF00 04 01 02 03 04 05 06 07\n" +
                "18EBFF00 05 08 09 00 01 02 03 04\n" +
                "18EBFF00 06 05 06 07 08 09 FF FF"));
        try (EchoBus bus = new EchoBus(0);
                J1939TP tp = new J1939TP(bus, 0xF9)) {
            {
                // only wait T1 between packets
                Stream<Packet> packetStream = tp.read(testPackets.size() * J1939TP.T1, TimeUnit.MILLISECONDS);
                for (Packet p : testPackets) {
                    bus.send(p);
                    // delay each packet by 80% of T1
                    Thread.sleep((long) (0.8 * J1939TP.T1));
                }
                // verify message received
                assertEquals(expectedPacket.toString(), packetStream.collect(Collectors.toList()).toString());
            }
            { // skip packet 3
                testPackets.remove(3);
                // only wait T1 between packets
                Stream<Packet> packetStream = tp.read(testPackets.size() * J1939TP.T1, TimeUnit.MILLISECONDS);
                for (Packet p : testPackets) {
                    bus.send(p);
                    Thread.sleep((long) (0.8 * J1939TP.T1));
                }
                // verify that no message was received
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
    public void testEchoBus() throws BusException {
        // baseline test verifying that the bus works.
        try (Bus bus = new EchoBus(0xF9)) {
            Stream<Packet> s = bus.read(1, TimeUnit.SECONDS);
            Packet p = Packet.create(0xFF, 0xF9, 1, 2, 3, 4);
            bus.send(p);
            assertEquals(Collections.singletonList(p), s.collect(Collectors.toList()));
        }
    }

    @Test
    public void testMissingDT() throws BusException {
        try (Bus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0);) {
            Stream<Packet> tpStream = tp.read(1, TimeUnit.SECONDS);

            Stream<Packet> s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            // RTS for 2 packets
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            // verify CTS for 2 packets starting with packet 1
            assertEquals("18ECF900 11 02 01 FF FF 00 EA 00", s.findFirst().get().toString());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            // send packet 2
            bus.send(Packet.parse("18EB00F9 02 08 09 FF FF FF FF FF"));
            // verify CTR for 1 packet starting with packet 1
            assertEquals("18ECF900 11 01 01 FF FF 00 EA 00", s.findFirst().get().toString());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            // send packet 1
            bus.send(Packet.parse("18EB00F9 01 01 02 03 04 05 06 07"));
            // verify EOM ACK
            assertEquals("18ECF900 13 00 09 02 FF 00 EA 00", s.findFirst().get().toString());

            // verify packet received
            assertEquals("18EA00F9 01 02 03 04 05 06 07 08 09",
                    tpStream.findFirst().orElse(null).toString());
        }
    }

    /**
     * Due to the implementation that creates a new thread for each CTS, this test
     * verifies that only a single TP packet is reconstructed when two CTSs are
     * received.
     *
     * @throws InterruptedException
     */
    @Test
    public void testMultipleBamCts() throws BusException, InterruptedException {
        try (EchoBus bus = new EchoBus(0);
                Bus tp = new J1939TP(bus, 0xF9);) {
            Stream<Packet> tpStream = tp.read(J1939TP.T1 * 3, TimeUnit.MILLISECONDS);
            // send first CTS
            bus.send(Packet.parse("18ECFF00 20 00 0E 03 FF F9 12 00"));
            Thread.sleep(50);
            // send full TP
            Packet p = Packet.parse("18FFFF00 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D");
            try (J1939TP tpOut = new J1939TP(bus)) {
                tpOut.send(p);
            }
            // verify that exactly one packet is received.
            assertEquals(Collections.singletonList(p).toString(), tpStream.collect(Collectors.toList()).toString());
        }
    }

    /**
     * Due to the implementation that creates a new thread for each CTS, this test
     * verifies that only a single TP packet is reconstructed when two CTSs are
     * received.
     *
     * @throws InterruptedException
     */
    @Test
    public void testMultipleCts() throws BusException, InterruptedException {
        try (EchoBus bus = new EchoBus(0);
                Bus tp = new J1939TP(bus, 0xF9);) {
            Stream<Packet> tpStream = tp.read(J1939TP.T1 * 3, TimeUnit.MILLISECONDS);
            // send first CTS
            bus.send(Packet.parse("18ECF900 10 00 0E 03 FF F9 12 00"));
            Thread.sleep(50);
            // send full TP
            Packet p = Packet.parse("1812F900 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D");
            try (J1939TP tpOut = new J1939TP(bus)) {
                tpOut.send(p);
            }
            // verify that exactly one packet is received.
            assertEquals(Collections.singletonList(p).toString(), tpStream.collect(Collectors.toList()).toString());
        }
    }

    @Test
    public void testNonTPOnTPBus() throws BusException {
        // verify that the tp bus will pass non-tp traffic.
        try (EchoBus bus = new EchoBus(0);
                Bus tp = new J1939TP(bus, 0xF9);) {
            { // tool to ECM
                Packet p = Packet.parse("18FFFFF9 01 02 03 04");
                Stream<Packet> busStream = bus.read(1, TimeUnit.SECONDS);
                tp.send(p);
                assertEquals(Collections.singletonList(p), busStream.collect(Collectors.toList()));
            }
            { // ECM to tool
                Packet p = Packet.parse("18FFFF00 01 02 03 04");
                Stream<Packet> tpStream = tp.read(1, TimeUnit.SECONDS);
                bus.send(p);
                assertPacketsEqual(Collections.singletonList(p), tpStream.collect(Collectors.toList()));
            }
        }
    }

    @Test
    public void testOutOfOrderDT() throws BusException {
        try (Bus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0);) {
            Stream<Packet> s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            Stream<Packet> tpStream = tp.read(3, TimeUnit.SECONDS);
            // send RTS
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            // verify CTS
            assertEquals("18ECF900 11 02 01 FF FF 00 EA 00", s.findFirst().get().toString());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            // send packet 2
            bus.send(Packet.parse("18EB00F9 02 08 09 00 00 00 00 00"));
            // send packet 1
            bus.send(Packet.parse("18EB00F9 01 01 02 03 04 05 06 07"));
            // verify EOM
            assertEquals("18ECF900 13 00 09 02 FF 00 EA 00", s.findFirst().get().toString());

            // verify packet
            assertEquals("18EA00F9 01 02 03 04 05 06 07 08 09", tpStream.findFirst().orElse(null).toString());
        }
    }

    @Test
    public void testSimpleTP() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tpIn = new J1939TP(bus, 0);
                J1939TP tpOut = new J1939TP(bus, 0xF9);) {

            // a stream of all the fragments
            Stream<Packet> stream = bus.read(100, TimeUnit.MILLISECONDS);

            // a stream with only the result
            Stream<Packet> tpStream = tpIn.read(3, TimeUnit.DAYS).limit(1);

            tpOut.send(Packet.parse("18EA00F9 01 02 03 04 05 06 07 08 09"));

            // verify that all the packets and only the expected packets were broadcast
            assertEquals(Packet.parseCollection("18EC00F9 10 00 09 02 FF 00 EA 00\n" +
                    "18ECF900 11 02 01 FF FF 00 EA 00\n" +
                    "18EB00F9 01 01 02 03 04 05 06 07\n" +
                    "18EB00F9 02 08 09 00 00 00 00 00\n" +
                    "18ECF900 13 00 09 02 FF 00 EA 00").toString(),
                    stream.collect(Collectors.toList()).toString());

            // verify that the complete packet and only the complete packet is on the TP
            // stream.
            assertEquals(Collections.singletonList("18EA00F9 01 02 03 04 05 06 07 08 09").toString(),
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
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));

            // verify CTS is for 2 packets starting at packet 1
            {
                Packet cts1 = it.next();
                System.err.println(cts1);
                assertEquals("18ECF900 11 02 01 FF FF 00 EA 00", cts1.toString());
            }

            // test T2
            // verify CTS is for packet 1
            assertEquals("18ECF900 11 02 01 FF FF 00 EA 00", it.next().toString());
            // verify that there was a T2 long delay
            assertTrue("T2 timing too fast", System.currentTimeMillis() - start > J1939TP.T2);

            // send packet 1
            start = System.currentTimeMillis();
            bus.send(Packet.parse("18EB00F9 01 55 55 55 55 55 55 55"));

            // test T1
            // verify CTS is for packet 2
            assertEquals("18ECF900 11 01 02 FF FF 00 EA 00", it.next().toString());
            assertEquals("T1 timing wrong", System.currentTimeMillis() - start, J1939TP.T1, 20);

            // verify that no TP packet is decoded
            Optional<Packet> result = tp.read(5, TimeUnit.SECONDS).findAny();
            assertFalse("result: " + result, result.isPresent());
        }
    }

    @Test()
    public void testT3() throws BusException {
        long start = System.currentTimeMillis();
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            tp.send(Packet.parse("1812F900 01 02 03 04 05 06 07 08 09 10"));
        } catch (CtsBusException e) {
            assertEquals("T3 not honored", J1939TP.T3, (System.currentTimeMillis() - start), 200);
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
                            tp.send(Packet.parse("1812F900 00 01 02 03 04 05 06 07 08 09 10"));
                        } catch (BusException e1) {
                        }
                        return null;
                    });
            assertTrue(waitForRts.findFirst().isPresent());

            // send the "hold he connection open" CTS
            bus.send(Packet.parse("18EC00F9 11 00 FF FF FF FF FF FF"));
            sender.join();
            assertEquals("T4 pause was incorrect",
                    J1939TP.T4,
                    System.currentTimeMillis() - start,
                    150);
        }
    }

    @Test
    public void verifyAbort() throws BusException {
        try (EchoBus bus = new EchoBus(0);
                J1939TP tpOut = new J1939TP(bus, 0xF9);) {

            // a stream of all the fragments
            Stream<Packet> stream = bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS);

            new Thread(() -> {
                try {
                    tpOut.send(Packet.parse(
                            "18EA00F9 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F"));
                } catch (BusException e) {
                    e.printStackTrace();
                }
            }).start();

            // verify RTS
            assertPacketsEqual(Packet.parse("18EC00F9 10 00 2D 07 FF 00 EA 00"),
                    stream.findFirst().orElse(null));

            stream = bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS)
                    .filter(p -> p.getSource() == 0xF9);
            // send CTS for 2 packets
            bus.send(Packet.parse("18ECF900 11 02 01 FF FF 00 EA 00"));
            // verify that all the packets and only the expected packets were broadcast
            assertPacketsEqual(Packet.parseCollection("18EB00F9 01 01 02 03 04 05 06 07\n" +
                    "18EB00F9 02 08 09 0A 0B 0C 0D 0E"),
                    stream.collect(Collectors.toList()));

            // FIXME this should be in terms of J1939TP.Tx
            stream = bus.read(2, TimeUnit.SECONDS).filter(p -> p.getSource() == 0xF9);
            // send abort
            bus.send(Packet.parse("18ECF900 FF FF FF FF FF 00 EA 00"));
            // verify no more traffic
            assertEquals("[]", stream.collect(Collectors.toList()).toString());
        }
    }

    @Test
    public void verifyConstants() {
        assertEquals(750, J1939TP.T1);
        assertEquals(1250, J1939TP.T2);
        assertEquals(1250, J1939TP.T3);
        assertEquals(1050, J1939TP.T4);
    }

    @Test
    public void verifyCountMax() throws BusException {
        try (EchoBus bus = new EchoBus(0);
                J1939TP tpOut = new J1939TP(bus, 0xF9);) {

            // a stream of all the fragments
            Stream<Packet> stream = bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS);
            new Thread(() -> {
                try {
                    tpOut.send(Packet.parse(
                            "18EA00F9 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F"));
                } catch (BusException e) {
                    e.printStackTrace();
                }
            }).start();

            // verify RTS
            assertPacketsEqual(Packet.parse("18EC00F9 10 00 2D 07 FF 00 EA 00"), stream.findFirst().orElse(null));

            stream = bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS).filter(p -> p.getSource() == 0xF9);
            // send CTS for 2 packets
            bus.send(Packet.parse("18ECF900 11 02 01 FF FF 00 EA 00"));
            // verify that all the packets and only the expected packets were broadcast
            assertPacketsEqual(Packet.parseCollection("18EB00F9 01 01 02 03 04 05 06 07\n" +
                    "18EB00F9 02 08 09 0A 0B 0C 0D 0E"),
                    stream.collect(Collectors.toList()));
        }
    }

}
