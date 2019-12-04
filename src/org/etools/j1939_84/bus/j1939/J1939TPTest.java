package org.etools.j1939_84.bus.j1939;

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
    public void testMissingCTS() {
    }

    public void testMissingDT() {
    }

    public void testMultipleCTS() {
    }

    @Test
    public void testNonBus() throws BusException {
        Bus bus = new EchoBus(0xF9);
        Stream<Packet> s = bus.read(1, TimeUnit.SECONDS);
        Packet p = Packet.create(0xFF, 0xF9, 1, 2, 3, 4);
        bus.send(p);
        Assert.assertEquals(Collections.singletonList(p), s.collect(Collectors.toList()));
    }

    @Test
    public void testNonTP() throws BusException {
        Bus bus = new EchoBus(0xF9);
        Stream<Packet> s = bus.read(1, TimeUnit.SECONDS);
        Packet p = Packet.create(0xFF, 0xF9, 1, 2, 3, 4);
        new J1939TP(bus).send(p);
        Assert.assertEquals(Collections.singletonList(p), s.collect(Collectors.toList()));
    }

    public void testOutOfOrderDT() {
    }

    public void testTh() {
    }
}
