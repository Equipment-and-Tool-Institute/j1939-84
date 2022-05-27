/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit test for the {@link ComponentIdentificationPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class ComponentIdentificationPacketTest {
    private static final String MAKE = "Solid Design";
    private static final String MODEL = "J1939-84 Tool";
    private static final String SN = "000001";
    private static final String UN = "1234567890";

    @Test
    public void testRealData() {
        int[] data = new int[] { 0x49, 0x4E, 0x54, 0x20, 0x20, 0x2A,
                0x37, 0x35, 0x37, 0x31, 0x30, 0x33, 0x31, 0x35, 0x31, 0x35, 0x2A,
                0x31, 0x32, 0x34, 0x4B, 0x4D, 0x32, 0x59, 0x34, 0x34, 0x31, 0x31, 0x38, 0x38, 0x38, 0x00, 0x00, 0x00,
                0x2A,
                0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2A };
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);

        assertEquals("INT  ", instance.getMake());
        assertEquals("7571031515", instance.getModel());
        assertEquals("124KM2Y4411888", instance.getSerialNumber());
        assertEquals("0\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000", instance.getUnitNumber());

        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: INT" + NL;
        expected += "  Model: 7571031515" + NL;
        expected += "  Serial: 124KM2Y4411888" + NL;
        expected += "  Unit: 0" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMake() {
        byte[] data = (MAKE + "****").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals("", instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMakeModel() {
        byte[] data = (MAKE + "*" + MODEL + "***").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals("", instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMakeModelSerialNumberUnitNumber() {
        byte[] data = (MAKE + "*" + MODEL + "*" + SN + "*" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMakeModelUnitNumber() {
        byte[] data = (MAKE + "*" + MODEL + "**" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMakeSerialNumber() {
        byte[] data = (MAKE + "**" + SN + "**").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals("", instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMakeSerialNumberUnitNumber() {
        byte[] data = (MAKE + "**" + SN + "*" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMakeUnitNumber() {
        byte[] data = (MAKE + "***" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testModel() {
        byte[] data = ("*" + MODEL + "***").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals("", instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testModelSerialNumber() {
        byte[] data = ("*" + MODEL + "*" + SN + "**").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals("", instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testModelSerialNumberUnitNumber() {
        byte[] data = ("*" + MODEL + "*" + SN + "*" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testModelUnitNumber() {
        byte[] data = ("*" + MODEL + "**" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testNoStars() {
        byte[] data = (MAKE + MODEL + SN + UN).getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE + MODEL + SN + UN, instance.getMake());
        assertNull(instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertNull(instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid DesignJ1939-84 Tool0000011234567890" + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testNothing() {
        byte[] data = ("****").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals("", instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testOneStar() {
        byte[] data = (MAKE + "*" + MODEL + SN + UN).getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals(MODEL + SN + UN, instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertNull(instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: J1939-84 Tool0000011234567890" + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPGN() {
        assertEquals(65259, ComponentIdentificationPacket.PGN);
    }

    @Test
    public void testSerialNumber() {
        byte[] data = ("**" + SN + "**").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals("", instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testSerialNumberUnitNumber() {
        byte[] data = ("**" + SN + "*" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testThreeStars() {
        byte[] data = (MAKE + "*" + MODEL + "*" + SN + "*" + UN).getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals(SN, instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: 000001" + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testTwoStars() {
        byte[] data = (MAKE + "*" + MODEL + "*" + SN + UN).getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals(MAKE, instance.getMake());
        assertEquals(MODEL, instance.getModel());
        assertEquals(SN + UN, instance.getSerialNumber());
        assertNull(instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: Solid Design" + NL;
        expected += "  Model: J1939-84 Tool" + NL;
        expected += "  Serial: 0000011234567890" + NL;
        expected += "  Unit: " + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testUnitNumber() {
        byte[] data = ("***" + UN + "*").getBytes(UTF_8);
        Packet packet = Packet.create(0, 0, data);
        ComponentIdentificationPacket instance = new ComponentIdentificationPacket(packet);
        assertEquals("", instance.getMake());
        assertEquals("", instance.getModel());
        assertEquals("", instance.getSerialNumber());
        assertEquals(UN, instance.getUnitNumber());
        String expected = "";
        expected += "Component Identification from Engine #1 (0): {" + NL;
        expected += "  Make: " + NL;
        expected += "  Model: " + NL;
        expected += "  Serial: " + NL;
        expected += "  Unit: 1234567890" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

}
