/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests for the {@link AddressClaimPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class AddressClaimPacketTest {

    @Test
    public void test1() {
        AddressClaimPacket instance = new AddressClaimPacket(Packet.parse("18EEFF55 10 F7 45 01 00 45 00 01"));
        String expected = "";
        expected += "DPF Controller (85) reported as: {" + NL;
        expected += "  Industry Group: Global" + NL;
        expected += "  Vehicle System: Non-specific System, System Instance: 1" + NL;
        expected += "  Function: Engine Emission Aftertreatment System, Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: Cummins Inc, Identity Number: 390928" + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void test2() {
        AddressClaimPacket instance = new AddressClaimPacket(Packet.parse("18EEFF3D 00 00 00 00 00 00 00 00"));
        String expected = "";
        expected += "Exhaust Emission Controller (61) reported as: {" + NL;
        expected += "  Industry Group: Global" + NL;
        expected += "  Vehicle System: Non-specific System, System Instance: 0" + NL;
        expected += "  Function: Engine, Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: Reserved, Identity Number: 0" + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void test3() {
        AddressClaimPacket instance = new AddressClaimPacket(Packet.parse("18EEFF00 00 00 40 05 00 00 65 14"));
        String expected = "";
        expected += "Engine #1 (0) reported as: {" + NL;
        expected += "  Industry Group: On-Highway Equipment" + NL;
        expected += "  Vehicle System: Unknown System (50), System Instance: 4" + NL;
        expected += "  Function: Unknown Function (0), Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: International Truck and Engine Corporation - Engine Electronics, Identity Number: 0"
                + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void test4() {
        AddressClaimPacket instance = new AddressClaimPacket(Packet.parse("18EEFF17 43 47 60 05 00 13 00 00"));
        String expected = "";
        expected += "Instrument Cluster #1 (23) reported as: {" + NL;
        expected += "  Industry Group: Global" + NL;
        expected += "  Vehicle System: Non-specific System, System Instance: 0" + NL;
        expected += "  Function: Instrument Cluster, Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: International Truck and Engine Corporation - Vehicle Electronics, Identity Number: 18243"
                + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void test5() {
        AddressClaimPacket instance = new AddressClaimPacket(Packet.parse("18EEFFF9 00 00 E0 FF 00 82 00 00"));
        String expected = "";
        expected += "Off Board Diagnostic-Service Tool #1 (249) reported as: {" + NL;
        expected += "  Industry Group: Global" + NL;
        expected += "  Vehicle System: Non-specific System, System Instance: 0" + NL;
        expected += "  Function: On-board data logger, Functional Instance: 0, ECU Instance: 0" + NL;
        expected += "  Manufactured by: Equipment & Tool Institute, Identity Number: 0" + NL;
        expected += "  Is not arbitrary address capable." + NL;
        expected += "}";
        assertEquals(expected, instance.toString());
    }
}
