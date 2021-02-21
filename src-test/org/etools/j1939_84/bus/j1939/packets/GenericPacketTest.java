package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GenericPacketTest {

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
    }

    @Test
    public void testPgn0() {
        byte[] data = new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        Packet packet = Packet.create(0, 0, 0, false, data);
        PgnDefinition pgnDefinition = new J1939DaRepository().findPgnDefinition(0);
        GenericPacket instance = new GenericPacket(packet, pgnDefinition);

        List<Spn> spns = instance.getSpns();
        assertEquals(10, spns.size());
        String expected = "";
        expected += "Torque/Speed Control 1 from Engine #1 (0): " + NL;
        expected += "  SPN   695, Engine Override Control Mode: 01" + NL;
        expected += "  SPN   696, Engine Requested Speed Control Conditions: 00" + NL;
        expected += "  SPN   897, Override Control Mode Priority: 01" + NL;
        expected += "  SPN   898, Engine Requested Speed/Speed Limit: 1636.250000 rpm" + NL;
        expected += "  SPN   518, Engine Requested Torque/Torque Limit: -57.000000 %" + NL;
        expected += "  SPN  3349, TSC1 Transmission Rate: 101" + NL;
        expected += "  SPN  3350, TSC1 Control Purpose: 01010" + NL;
        expected += "  SPN  4191, Engine Requested Torque (Fractional): 0.750000 %" + NL;
        expected += "  SPN  4206, Message Counter: 8.000000 count" + NL;
        expected += "  SPN  4207, Message Checksum: 8.000000 count" + NL;
        assertEquals(expected, instance.toString());
    }
}
