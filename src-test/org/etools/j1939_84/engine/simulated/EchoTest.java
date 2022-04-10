/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.engine.simulated;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.etools.j1939_84.controllers.ResultsListener.NOOP;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.EchoBus;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.junit.Test;

public class EchoTest {

    @Test
    public void getVin() throws BusException {
        Bus bus = new EchoBus(0xF9);
        final String VIN = "SOME VIN";
        try (Sim sim = new Sim(bus)) {
            sim.response(p -> p.getPgn() == 0xEA00 && p.get24(0) == 65260,
                         () -> Packet.create(65260, 0x0, VIN.getBytes(UTF_8)));

            var actual = new J1939(bus)
                                       .requestGlobal(null, VehicleIdentificationPacket.class, NOOP)
                                       .toPacketStream()
                                       .map(ParsedPacket::getPacket)
                                       .map(Packet::getBytes)
                                       .map(String::new)
                                       .findFirst()
                                       .orElse("");
            assertEquals(VIN, actual);
        }
    }
}
