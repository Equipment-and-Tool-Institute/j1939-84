package org.etools.j1939_84.bus.j1939;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

    interface PacketTask {
        void run() throws BusException;
    }

    static private void assertPacketsEquals(Collection<Packet> expected, Collection<Packet> actual) {
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    static private void assertPacketsEquals(Packet expected, Packet actual) {
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    private final long start = System.currentTimeMillis();

    private CompletableFuture<Void> run(PacketTask task) {
        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (AssertionError | BusException e) {
                throw new CompletionException(e);
            }
        });
    }

    final private void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Verify that failing to send data after a RTS generates 3 CTS responses before
     * giving up.
     *
     * @throws BusException
     */
    @Test
    public void test3CtsWithoutData() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            Stream<Packet> s = bus.read(J1939TP.T1 * 5, TimeUnit.MILLISECONDS);
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            // verify exactly 3 CTS
            assertEquals(3, s.filter(p -> p.toString().startsWith("18ECF900 11")).count());
        }
    }

    /**
     * Verify that TP Abort message is respected when receiving.
     *
     */
    @Test
    public void testAbortOnReceive() throws BusException, InterruptedException {
        try (EchoBus bus = new EchoBus(0);
                J1939TP tpIn = new J1939TP(bus, 0xF9);) {

            // verify test
            {
                Stream<Packet> stream = tpIn.read(2 * J1939TP.T1, TimeUnit.MILLISECONDS);

                // send RTS
                bus.send(Packet.parse("18ECF900 10 00 15 03 FF 00 EA 00"));

                // wait for up to 120% o T1 for CTS
                assertPacketsEquals(Packet.parse("18EC00F9 11 03 01 FF FF 00 EA 00"),
                        bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS)
                                .filter(p -> p.getSource() == 0xF9)
                                .findFirst().orElse(null));

                // send 2 packets and abort
                Packet.parseCollection(
                        "18EBF900 01 01 02 03 04 05 06 07\n" +
                                "18EBF900 02 08 09 0A 0B 0C 0D 0E\n" +
                                // "18ECF900 FF FF FF FF FF 00 EA 00\n" +
                                "18EBF900 03 0F 10 11 12 13 14 15\n")
                        .forEach(p -> bus.send(p));

                // verify packet
                assertPacketsEquals(
                        Packet.parseCollection(
                                "18EAF900 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15"),
                        stream.collect(Collectors.toList()));

            }
            Stream<Packet> stream = tpIn.read(2 * J1939TP.T1, TimeUnit.MILLISECONDS);

            // send RTS
            bus.send(Packet.parse("18ECF900 10 00 15 03 FF 00 EA 00"));

            // wait for up to 120% o T1 for CTS
            assertPacketsEquals(Packet.parse("18EC00F9 11 03 01 FF FF 00 EA 00"),
                    bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS)
                            .filter(p -> p.getSource() == 0xF9)
                            .findFirst().orElse(null));

            // send 2 packets, abort, then 3rd packet
            bus.send(Packet.parsePacket("18EBF900 01 01 02 03 04 05 06 07"));
            bus.send(Packet.parsePacket("18EBF900 02 08 09 0A 0B 0C 0D 0E"));
            bus.send(Packet.parsePacket("18ECF900 FF FF FF FF FF 00 EA 00"));
            sleep(5);
            bus.send(Packet.parsePacket("18EBF900 03 0F 10 11 12 13 14 15"));

            // verify no traffic
            assertPacketsEquals(Collections.emptyList(), stream.collect(Collectors.toList()));
        }
    }

    /** Verify that TP Abort message is respected. */
    @Test
    public void testAbortOnSend() throws BusException {
        try (EchoBus bus = new EchoBus(0);
                J1939TP tpOut = new J1939TP(bus, 0xF9);) {

            // a stream of all the fragments
            Stream<Packet> stream = bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS);

            run(() -> tpOut.send(Packet.parse(
                    "18EA00F9 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F")));

            // verify RTS
            assertPacketsEquals(Packet.parse("18EC00F9 10 00 2D 07 FF 00 EA 00"),
                    stream.findFirst().orElse(null));

            // wait for up to 120% o T1 for data packets
            stream = bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS)
                    .filter(p -> p.getSource() == 0xF9);
            // send CTS for 2 packets
            bus.send(Packet.parse("18ECF900 11 02 01 FF FF 00 EA 00"));
            // verify that all the packets and only the expected packets were broadcast
            assertPacketsEquals(Packet.parseCollection("18EB00F9 01 01 02 03 04 05 06 07\n" +
                    "18EB00F9 02 08 09 0A 0B 0C 0D 0E"),
                    stream.collect(Collectors.toList()));

            // FIXME this should be in terms of J1939TP.Tx
            stream = bus.read((long) (1.2 * J1939TP.T1), TimeUnit.MILLISECONDS).filter(p -> p.getSource() == 0xF9);
            // send abort
            bus.send(Packet.parse("18ECF900 FF FF FF FF FF 00 EA 00"));
            // verify no more traffic
            assertPacketsEquals(Collections.emptyList(), stream.collect(Collectors.toList()));
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
                Stream<Packet> shortPacketStream = bus.read(3 * 50, TimeUnit.MILLISECONDS);
                tp.sendBam(testPacket);
                // verify there was a delay between packets
                assertNotEquals(expectedResult.toString(), shortPacketStream.collect(Collectors.toList()).toString());
                // verify all packets eventually arrived
                assertPacketsEquals(expectedResult, packetStream.collect(Collectors.toList()));
            }

            // test tp bam send and read
            try (J1939TP receivingTp = new J1939TP(bus, 0xF9)) {
                Stream<Packet> tpStream = receivingTp.read(2, TimeUnit.SECONDS);
                {// test raw packets
                    Stream<Packet> packetStream = bus.read(1, TimeUnit.SECONDS);
                    tp.sendBam(testPacket);
                    assertPacketsEquals(expectedResult, packetStream.collect(Collectors.toList()));
                }
                // test TP
                assertPacketsEquals(testPacket, tpStream.findFirst().orElse(null));
            }
        }
    }

    /** Simple BAM test to verify expected packets are sent with some delay. */
    @Test
    public void testBamAbort() throws BusException {
        Packet.parsePacket(
                "18FFEE00 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09 00 01 02 03 04 05 06 07 08 09");
        Collection<Packet> rawPackets = Packet.parseCollection("18ECFF00 20 00 28 06 FF EE FF 00\n" +
                "18EBFF00 01 00 01 02 03 04 05 06\n" +
                "18EBFF00 02 07 08 09 00 01 02 03\n" +
                "18EBFF00 03 04 05 06 07 08 09 00\n" +
                "18ECFF00 FF FF FF FF FF EE FF 00\n" + // ABORT
                "18EBFF00 04 01 02 03 04 05 06 07\n" +
                "18EBFF00 05 08 09 00 01 02 03 04\n" +
                "18EBFF00 06 05 06 07 08 09 FF FF");
        try (EchoBus bus = new EchoBus(0);
                J1939TP tp = new J1939TP(bus, 0xF9)) {
            Stream<Packet> in = tp.read(1, TimeUnit.SECONDS);
            rawPackets.forEach(p -> {
                sleep(50);
                bus.send(p);
            });
            assertPacketsEquals(Collections.emptyList(), in.collect(Collectors.toList()));
        }
    }

    /** Verify that missing BAM data does not result in a packet. */
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
            // First verify that test process works.
            {
                // only wait T1 between packets
                Stream<Packet> packetStream = tp.read(testPackets.size() * J1939TP.T1, TimeUnit.MILLISECONDS);
                for (Packet p : testPackets) {
                    bus.send(p);
                    // delay each packet by 80% of T1
                    sleep((long) (0.8 * J1939TP.T1));
                }
                // verify message received
                assertPacketsEquals(expectedPacket, packetStream.collect(Collectors.toList()));
            }
            // Do the same test, but skip data packet 3.
            {
                testPackets.remove(3);
                // only wait T1 between packets
                Stream<Packet> packetStream = tp.read(testPackets.size() * J1939TP.T1, TimeUnit.MILLISECONDS);
                for (Packet p : testPackets) {
                    bus.send(p);
                    sleep((long) (0.8 * J1939TP.T1));
                }
                // verify that no message was received
                assertTrue(packetStream.collect(Collectors.toList()).isEmpty());
            }
        }
    }

    /** Simple test to verify that the bus streams are collecting packets. */
    @Test
    public void testBus() {
        // verify that the bus works as expected
        try (EchoBus bus = new EchoBus(0)) {
            int count = 10;
            Stream<Packet> streamA = bus.read(1, TimeUnit.SECONDS).limit(count - 1);
            Stream<Packet> streamB = bus.read(1, TimeUnit.SECONDS).limit(count);
            // send count packets
            for (int i = 0; i < count; i++) {
                bus.send(Packet.create(0xFF00 | i, 2, 0, 1, 2, 3, 4, 5, 6, 7));
            }
            // verify that packet counts were received.
            assertEquals(count - 1, streamA.collect(Collectors.toList()).size());
            assertEquals(count, streamB.collect(Collectors.toList()).size());
        }
    }

    /** Verify that count max in RTS is respected. */
    @Test
    public void testCountMax() throws BusException {
        try (EchoBus bus = new EchoBus(0);
                J1939TP tpOut = new J1939TP(bus, 0xF9);) {

            // a stream of all the fragments
            Stream<Packet> stream = bus.read(100, TimeUnit.MILLISECONDS);
            run(() -> tpOut.send(Packet.parse(
                    "18EA00F9 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F")));

            // verify RTS
            assertPacketsEquals(Packet.parse("18EC00F9 10 00 2D 07 FF 00 EA 00"), stream.findFirst().orElse(null));

            // sleep(5);// FIXME why does the RTS sometimes show in the following stream?
            stream = bus.read(100, TimeUnit.MILLISECONDS).filter(p -> p.getSource() == 0xF9);
            // send CTS for 2 packets
            bus.send(Packet.parse("18ECF900 11 02 01 FF FF 00 EA 00"));
            // verify that all the packets and only the expected packets were broadcast
            assertPacketsEquals(Packet.parseCollection("18EB00F9 01 01 02 03 04 05 06 07\n" +
                    "18EB00F9 02 08 09 0A 0B 0C 0D 0E"),
                    stream.collect(Collectors.toList()));
        }
    }

    /** Simple test to verify that the bus passes any packets. */
    @Test
    public void testEchoBus() throws BusException {
        // baseline test verifying that the bus works.
        try (Bus bus = new EchoBus(0xF9)) {
            Stream<Packet> s = bus.read(1, TimeUnit.SECONDS);
            Packet p = Packet.create(0xFF, 0xF9, 1, 2, 3, 4);
            bus.send(p);
            assertPacketsEquals(Collections.singletonList(p), s.collect(Collectors.toList()));
        }
    }

    /** verify that the iterator on the packet stream blocks as expected. */
    @Test
    public void testIteratorBlocks() {
        Bus bus = new EchoBus(0xF9);
        try {
            bus.read(2, TimeUnit.SECONDS).iterator().hasNext();
        } catch (BusException e) {
            fail();
        }
        assertEquals(2000, System.currentTimeMillis() - start, 100);
    }

    /** Verify that when a DT packet is missing, that it is rerequested. */
    @Test
    public void testMissingDT() throws BusException {
        try (Bus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0);) {
            Stream<Packet> tpStream = tp.read(1, TimeUnit.SECONDS);

            Stream<Packet> s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            // RTS for 2 packets
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            // verify CTS for 2 packets starting with packet 1
            assertPacketsEquals(Packet.parsePacket("18ECF900 11 02 01 FF FF 00 EA 00"), s.findFirst().get());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            // send packet 2
            bus.send(Packet.parse("18EB00F9 02 08 09 FF FF FF FF FF"));
            // verify CTR for 1 packet starting with packet 1
            assertPacketsEquals(Packet.parsePacket("18ECF900 11 01 01 FF FF 00 EA 00"), s.findFirst().get());

            s = bus.read(3, TimeUnit.SECONDS).filter(p -> p.getSource() == 0);
            // send packet 1
            bus.send(Packet.parse("18EB00F9 01 01 02 03 04 05 06 07"));
            // verify EOM ACK
            assertPacketsEquals(Packet.parsePacket("18ECF900 13 00 09 02 FF 00 EA 00"), s.findFirst().get());

            // verify packet received
            assertPacketsEquals(Packet.parsePacket("18EA00F9 01 02 03 04 05 06 07 08 09"),
                    tpStream.findFirst().orElse(null));
        }
    }

    /**
     * Due to the implementation that creates a new thread for each RTS, this test
     * verifies that only a single TP packet is reconstructed when two BAM RTSs are
     * received.
     *
     * @throws InterruptedException
     */
    @Test
    public void testMultipleBamRts() throws BusException, InterruptedException {
        try (EchoBus bus = new EchoBus(0);
                Bus tp = new J1939TP(bus, 0xF9);) {
            Stream<Packet> tpStream = tp.read(J1939TP.T1 * 3, TimeUnit.MILLISECONDS);
            // send first RTS
            bus.send(Packet.parse("18ECFF00 20 00 0E 03 FF F9 12 00"));
            sleep(50);
            // send full TP
            Packet p = Packet.parse("18FFFF00 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D");
            try (J1939TP tpOut = new J1939TP(bus)) {
                tpOut.send(p);
            }
            // verify that exactly one packet is received.
            assertPacketsEquals(Collections.singletonList(p), tpStream.collect(Collectors.toList()));
        }
    }

    /**
     * Due to the implementation that creates a new thread for each RTS, this test
     * verifies that only a single TP packet is reconstructed when two RTSs are
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
            sleep(50);
            // send full TP
            Packet p = Packet.parse("1812F900 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D");
            try (J1939TP tpOut = new J1939TP(bus)) {
                tpOut.send(p);
            }
            // verify that exactly one packet is received.
            assertPacketsEquals(Collections.singletonList(p), tpStream.collect(Collectors.toList()));
        }
    }

    /** Verify that non-TP packets are passed through TP layer. */
    @Test
    public void testNonTPOnTPBus() throws BusException {
        // verify that the tp bus will pass non-tp traffic.
        try (EchoBus bus = new EchoBus(0);
                Bus tp = new J1939TP(bus, 0xF9);) {
            { // tool to ECM
                Packet p = Packet.parse("18FFFFF9 01 02 03 04");
                Stream<Packet> busStream = bus.read(1, TimeUnit.SECONDS);
                tp.send(p);
                assertPacketsEquals(Collections.singletonList(p), busStream.collect(Collectors.toList()));
            }
            { // ECM to tool
                Packet p = Packet.parse("18FFFF00 01 02 03 04");
                Stream<Packet> tpStream = tp.read(1, TimeUnit.SECONDS);
                bus.send(p);
                assertPacketsEquals(Collections.singletonList(p), tpStream.collect(Collectors.toList()));
            }
        }
    }

    /** Verfy that out of order DT packets are accepted. */
    @Test
    public void testOutOfOrderDT() throws BusException {
        try (Bus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0);) {
            Stream<Packet> stream = bus.read(500, TimeUnit.MILLISECONDS).filter(p -> p.getSource() == 0);
            Stream<Packet> tpStream = tp.read(2, TimeUnit.SECONDS);
            // send RTS
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));
            // verify CTS
            assertPacketsEquals(Packet.parse("18ECF900 11 02 01 FF FF 00 EA 00"), stream.findFirst().get());

            // sleep(5); // FIXME, why does the CTS keep ending up in the following stream?
            stream = bus.read(500, TimeUnit.MILLISECONDS).filter(p -> p.getSource() == 0);
            // send packet 2
            bus.send(Packet.parse("18EB00F9 02 08 09 00 00 00 00 00"));
            // send packet 1
            bus.send(Packet.parse("18EB00F9 01 01 02 03 04 05 06 07"));
            // verify EOM
            assertPacketsEquals(Packet.parseCollection("18ECF900 13 00 09 02 FF 00 EA 00"),
                    stream.collect(Collectors.toList()));

            // verify packet
            assertPacketsEquals(Packet.parseCollection("18EA00F9 01 02 03 04 05 06 07 08 09"),
                    tpStream.collect(Collectors.toList()));
        }
    }

    /** Simple TP verification */
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
            assertPacketsEquals(Packet.parseCollection("18EC00F9 10 00 09 02 FF 00 EA 00\n" +
                    "18ECF900 11 02 01 FF FF 00 EA 00\n" +
                    "18EB00F9 01 01 02 03 04 05 06 07\n" +
                    "18EB00F9 02 08 09 00 00 00 00 00\n" +
                    "18ECF900 13 00 09 02 FF 00 EA 00"),
                    stream.collect(Collectors.toList()));

            // verify that the complete packet and only the complete packet is on the TP
            // stream.
            assertPacketsEquals(Collections.singletonList(Packet.parsePacket("18EA00F9 01 02 03 04 05 06 07 08 09")),
                    tpStream.collect(Collectors.toList()));
        }
    }

    /** Verify that T1 and T2 timeouts are respected. */
    @Test()
    public void testT2T1() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            Iterator<Packet> it = bus.read(5, TimeUnit.SECONDS).filter(p -> p.getSource() == 0).iterator();

            // send RTS
            bus.send(Packet.parse("18EC00F9 10 00 09 02 FF 00 EA 00"));

            // verify CTS is for 2 packets starting at packet 1
            {
                Packet cts1 = it.next();
                System.err.println(cts1);
                assertPacketsEquals(Packet.parsePacket("18ECF900 11 02 01 FF FF 00 EA 00"), cts1);
            }

            // test T2
            // verify CTS is for packet 1
            assertPacketsEquals(Packet.parsePacket("18ECF900 11 02 01 FF FF 00 EA 00"), it.next());
            // verify that there was a T2 long delay
            assertTrue("T2 timing too fast", System.currentTimeMillis() - start > J1939TP.T2);

            // send packet 1
            long start2 = System.currentTimeMillis();
            bus.send(Packet.parse("18EB00F9 01 55 55 55 55 55 55 55"));

            // test T1
            // verify CTS is for packet 2
            assertPacketsEquals(Packet.parsePacket("18ECF900 11 01 02 FF FF 00 EA 00"), it.next());
            assertEquals("T1 timing wrong", System.currentTimeMillis() - start2, J1939TP.T1, 20);

            // verify that no TP packet is decoded
            Optional<Packet> result = tp.read(5, TimeUnit.SECONDS).findAny();
            assertFalse("result: " + result, result.isPresent());
        }
    }

    /** Verify that T3 timeout is respected. */
    @Test()
    public void testT3() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
            tp.send(Packet.parse("1812F900 01 02 03 04 05 06 07 08 09 10"));
        } catch (CtsBusException e) {
            assertEquals("T3 not honored", J1939TP.T3, (System.currentTimeMillis() - start), 200);
        }
    }

    /** Verify that T4 timeout is respected. */
    @Test
    public void testT4() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0)) {
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

            // send the "hold the connection open" CTS
            bus.send(Packet.parse("18EC00F9 11 00 FF FF FF FF FF FF"));
            sender.join();
            assertEquals("T4 pause was incorrect",
                    J1939TP.T4,
                    System.currentTimeMillis() - start,
                    150);
        }
    }

    @Test
    public void testWaringsCts45() throws Exception {
        RuntimeException success = new RuntimeException();
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0) {

                    @Override
                    public void warn(String msg, Object... a) {
                        assertEquals("TP.CM_CTS bytes 4-5 should be FFFF: %04X  %s", msg);
                        throw success; // exit early
                    }
                }) {
            Stream<Packet> stream = bus.read(J1939TP.T1, TimeUnit.MILLISECONDS);
            run(() -> {
                // wait for RTS
                assertPacketsEquals(Packet.parsePacket("18ECF900 10 00 0A 02 FF F9 EA 00"),
                        stream.findFirst().orElse(null));
                // send invalid CTS
                bus.send(Packet.parse("18EC00F9 11 FF 01 11 11 F9 EA 00"));
            });

            tp.send(Packet.parse("18EAF900 01 02 03 04 05 06 07 08 09 10"));
            fail();
        } catch (RuntimeException e) {
            assertEquals(success, e);
        }
    }

    @Test
    public void testWaringsCtsHoldTheConnectionOpen() throws Exception {
        RuntimeException success = new RuntimeException();
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0) {

                    @Override
                    public void warn(String msg, Object... a) {
                        assertEquals("TP.CM_CTS \"hold the connection open\" should be: %04X  %s", msg);
                        throw success; // exit early
                    }
                }) {
            Stream<Packet> stream = bus.read(J1939TP.T1, TimeUnit.MILLISECONDS);
            run(() -> {
                // wait for RTS
                assertPacketsEquals(Packet.parsePacket("18ECF900 10 00 0A 02 FF F9 EA 00"),
                        stream.findFirst().orElse(null));
                // send invalid CTS
                bus.send(Packet.parse("18EC00F9 11 00 11 11 11 11 11 11"));
            });

            tp.send(Packet.parse("18EAF900 01 02 03 04 05 06 07 08 09 10"));
            fail();
        } catch (RuntimeException e) {
            assertEquals(success, e);
        }
    }

    @Test
    public void testWaringsCtsPgn() throws Exception {
        RuntimeException success = new RuntimeException();
        try (EchoBus bus = new EchoBus(0xF9);
                J1939TP tp = new J1939TP(bus, 0) {

                    @Override
                    public void warn(String msg, Object... a) {
                        assertEquals("TP.CM_CTS bytes 6-8 should be the PGN: %04X  %s", msg);
                        throw success; // exit early
                    }
                }) {
            Stream<Packet> stream = bus.read(J1939TP.T1, TimeUnit.MILLISECONDS);
            run(() -> {
                // wait for RTS
                assertPacketsEquals(Packet.parsePacket("18ECF900 10 00 0A 02 FF F9 EA 00"),
                        stream.findFirst().orElse(null));
                // send invalid CTS
                bus.send(Packet.parse("18EC00F9 11 01 02 FF FF 11 11 11"));
            });

            tp.send(Packet.parse("18EAF900 01 02 03 04 05 06 07 08 09 10"));
            fail();
        } catch (RuntimeException e) {
            assertEquals(success, e);
        }
    }

    /** Verify constants are correct. */
    @Test
    public void verifyConstants() {
        assertEquals(750, J1939TP.T1);
        assertEquals(1250, J1939TP.T2);
        assertEquals(1250, J1939TP.T3);
        assertEquals(1050, J1939TP.T4);
    }
}
