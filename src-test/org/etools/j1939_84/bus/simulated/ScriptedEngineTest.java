package org.etools.j1939_84.bus.simulated;

import static org.etools.j1939_84.bus.Packet.parsePacket;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

public class ScriptedEngineTest {
    private boolean reqResp(String req, String resp) {
        return ScriptedEngine.isRequestForPredicate(parsePacket(resp))
                             .test(parsePacket(req));
    }

    @Test
    public void testIsRequestForPredicate() {
        // request from broadcast, receive to broadcast
        assertTrue(reqResp("18EAFFF9 00 12 00", "1812FF00 00 11 22 33 44 55 66 77"));
        // request from broadcast, receive to DS
        assertTrue(reqResp("18EAFFF9 00 12 00", "1812F900 00 11 22 33 44 55 66 77"));
        // request from DA, receive to DA
        assertTrue(reqResp("18EA01F9 00 12 00", "1812F901 00 11 22 33 44 55 66 77"));
        // request from DA, receive to DA from wrong source
        assertFalse(reqResp("18EA01F9 00 12 00", "1812F902 00 11 22 33 44 55 66 77"));
        // request from DA, receive to broadcast from wrong source
        assertFalse(reqResp("18EA01F9 00 12 00", "1812FF02 00 11 22 33 44 55 66 77"));

        // request from broadcast, receive to broadcast
        assertTrue(reqResp("18EAFFF9 34 FF 00", "18FF3400 00 11 22 33 44 55 66 77"));
        // request from DA, receive to broadcast
        assertTrue(reqResp("18EA00F9 34 FF 00", "18FF3400 00 11 22 33 44 55 66 77"));
    }

    @Test
    /**
     * Verify that multiple packets are sent for requests to are responses to
     * broadcast
     *
     * @throws Exception
     */
    public void testPriority1() throws Exception {

        try (Bus bus = new EchoBus(0xF9);
             Sim sim = new Sim(bus)) {
            bus.log(p -> "P: " + p);
            Packet respFF = Packet.parsePacket("1812FF00 00 11 22 33 44 55 66 77");
            sim.response(ScriptedEngine.isRequestForPredicate(respFF), () -> respFF);
            sim.response(ScriptedEngine.isRequestForPredicate(respFF), () -> respFF);

            Stream<Packet> s = bus.read(100, TimeUnit.MILLISECONDS);
            bus.send(Packet.parsePacket("18EAFFF9 00 12 00"));
            assertEquals(3, s.count());
        }
    }

    @Test
    /**
     * Verify that multiple packets are sent for requests for broadcast
     *
     * @throws Exception
     */
    public void testPriority2() throws Exception {

        try (Bus bus = new EchoBus(0xF9);
             Sim sim = new Sim(bus)) {
            bus.log(p -> "P: " + p);
            Packet respFF = Packet.parsePacket("18FF0100 00 11 22 33 44 55 66 77");
            sim.response(ScriptedEngine.isRequestForPredicate(respFF), () -> respFF);
            sim.response(ScriptedEngine.isRequestForPredicate(respFF), () -> respFF);

            Stream<Packet> s = bus.read(100, TimeUnit.MILLISECONDS);
            bus.send(Packet.parsePacket("18EAFFF9 01 FF 00"));
            assertEquals(3, s.count());
        }
    }

    @Test
    /**
     * Should only have a single response for da request for da response.
     *
     * @throws Exception
     */
    public void testPriority3() throws Exception {

        try (Bus bus = new EchoBus(0xF9);
             Sim sim = new Sim(bus)) {
            bus.log(p -> "P: " + p);
            Packet respF9 = Packet.parsePacket("1812F901 00 11 22 33 44 55 66 77");
            Packet respFF = Packet.parsePacket("1812FF01 00 11 22 33 44 55 66 77");
            sim.response(ScriptedEngine.isRequestForPredicate(respF9), () -> respF9);
            sim.response(ScriptedEngine.isRequestForPredicate(respFF), () -> respFF);

            Stream<Packet> s = bus.read(100, TimeUnit.MILLISECONDS);
            bus.send(Packet.parsePacket("18EA01F9 00 12 00"));
            assertEquals(2, s.count());
        }
    }
}
