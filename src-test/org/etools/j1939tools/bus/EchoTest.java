/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.junit.Assert.assertFalse;

import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.junit.Test;

public class EchoTest {

    @Test
    public void failVin() throws BusException {
        Bus bus = new EchoBus(0xF9);
        assertFalse(new J1939(bus).requestGlobal(null, VehicleIdentificationPacket.class, x->{})
                                  .toPacketStream()
                                  .findFirst()
                                  .isPresent());
    }

}
