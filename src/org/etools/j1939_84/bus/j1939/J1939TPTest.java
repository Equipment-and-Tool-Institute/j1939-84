package org.etools.j1939_84.bus.j1939;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;
import org.junit.Assert;
import org.junit.Test;

public class J1939TPTest {
    @Test
    public void testBus() {
        // verify that the bus works as expected
        int count = 10;
        EchoBus bus = new EchoBus(0);
        Stream<Packet> a = bus.read(1, TimeUnit.SECONDS).limit(count - 1);
        Stream<Packet> b = bus.read(1, TimeUnit.SECONDS).limit(count);
        for (int i = 0; i < count; i++) {
            bus.send(Packet.create(0xFF00 | i, 2, 0, 1, 2, 3, 4, 5, 6, 7));
        }
        Collection<Packet> ac = a.collect(Collectors.toList());
        Collection<Packet> bc = b.collect(Collectors.toList());
        Assert.assertEquals(count - 1, ac.size());
        Assert.assertEquals(count, bc.size());
    }

    public void testMissingCTS() {
    }

    public void testMissingDT() {
    }

    public void testMultipleCTS() {
    }

    @Test
    public void testNonBus() throws BusException {
        try (Bus bus = new EchoBus(0xF9)) {
            Stream<Packet> s = bus.read(1, TimeUnit.SECONDS);
            Packet p = Packet.create(0xFF, 0xF9, 1, 2, 3, 4);
            bus.send(p);
            Assert.assertEquals(Collections.singletonList(p), s.collect(Collectors.toList()));
        }
    }

    @Test
    public void testNonTP() throws BusException {
        try (EchoBus bus = new EchoBus(0xF9);
                Bus tp = new J1939TP(bus);) {
            Stream<Packet> s = bus.read(1, TimeUnit.SECONDS);
            Packet p = Packet.create(0xFF, 0xF9, 1, 2, 3, 4);
            tp.send(p);
            Assert.assertEquals(Collections.singletonList(p), s.collect(Collectors.toList()));
        }
    }

    public void testOutOfOrderDT() {
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

            Assert.assertEquals(
                    Arrays.asList(Packet.create(0xEC00, 0xF9, 0x10, 0x00, 0x09, 0x02, 0xFF, 0x00, 0xEA, 0x00),
                            Packet.create(0xECF9, 0x00, 0x11, 0x02, 0x01, 0xFF, 0xFF, 0x00, 0xEA, 0x00),
                            Packet.create(0xEB00, 0xF9, 0x01, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07),
                            Packet.create(0xEB00, 0xF9, 0x02, 0x08, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00),
                            Packet.create(0xECF9, 0x00, 0x13, 0x00, 0x09, 0x02, 0xFF, 0x00, 0xEA, 0x00)),
                    stream.collect(Collectors.toList()));
            Assert.assertEquals(Collections.singletonList(Packet.create(0xEA00, 0xF9, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
                    tpStream.collect(Collectors.toList()));
        }
    }

    public void testTh() {
    }
}
