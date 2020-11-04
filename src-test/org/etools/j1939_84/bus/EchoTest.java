/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.bus.simulated.Sim;
import org.junit.Test;

public class EchoTest {

    @Test

    public void failVin() throws BusException {
        Bus bus = new EchoBus(0xF9);
        assertFalse(new J1939(bus).requestGlobal(VehicleIdentificationPacket.class).findFirst().isPresent());
    }

    @Test
    public void getVin() throws BusException {
        Bus bus = new EchoBus(0xF9);
        final String VIN = "SOME VIN";
        try (Sim sim = new Sim(bus)) {
            sim.response(p -> (p.getId() & 0xFF00) == 0xEA00 && p.get24(0) == 65260,
                    () -> Packet.create(65260, 0x0, VIN.getBytes()));

            assertEquals(VIN,
                    new J1939(bus).requestGlobal(VehicleIdentificationPacket.class)
                            .flatMap(e -> e.left.stream())
                            .findFirst()
                            .map(p1 -> new String(p1.getPacket().getBytes())).get());
        }
    }
}
