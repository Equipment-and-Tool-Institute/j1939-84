/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * The Unit tests for the {@link VehicleIdentificationPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleIdentificationPacketTest {

    @Test
    public void testGetVinAndToString() {
        Packet packet = Packet.create(0, 0, 0x33, 0x48, 0x41, 0x4D, 0x4B, 0x53, 0x54, 0x4E, 0x30, 0x46, 0x4C, 0x35,
                0x37, 0x35, 0x30, 0x31, 0x32, 0x2A);
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("3HAMKSTN0FL575012", instance.getVin());
        assertEquals("Vehicle Identification from Engine #1 (0): 3HAMKSTN0FL575012", instance.toString());
    }

    @Test
    public void testGetVinWithoutAsterisk() {
        Packet packet = Packet.create(0, 0, 0x33, 0x48, 0x41, 0x4D, 0x4B, 0x53, 0x54, 0x4E, 0x30, 0x46, 0x4C, 0x35,
                0x37, 0x35, 0x30, 0x31, 0x32);
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("3HAMKSTN0FL575012", instance.getVin());
    }

    @Test
    public void testGetVinWithoutAsteriskExtraBytes() {
        Packet packet = Packet.create(0, 0, 0x33, 0x48, 0x41, 0x4D, 0x4B, 0x53, 0x54, 0x4E, 0x30, 0x46, 0x4C, 0x35,
                0x37, 0x35, 0x30, 0x31, 0x32, 0x2A, 0x33, 0x48, 0x41, 0x4D, 0x4B, 0x53, 0x54, 0x4E, 0x30, 0x46, 0x4C,
                0x35, 0x37, 0x35, 0x30, 0x31, 0x32, 0x2A);
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("3HAMKSTN0FL575012", instance.getVin());
    }

    @Test
    public void testGetVinWithoutAsteriskWith200Characters() {
        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus ultrices vehicula elit, id pharetra lacus. Suspendisse justo nulla, egestas vel volutpat vel, convallis at nisl. Nulla facilisi amet";
        Packet packet = Packet.create(0, 0, (expected + "*").getBytes(StandardCharsets.UTF_8));
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals(expected, instance.getVin());
    }

    @Test
    public void testGetVinWithoutAsteriskWithNoCharacters() {
        Packet packet = Packet.create(0, 0, 0x2A);
        VehicleIdentificationPacket instance = new VehicleIdentificationPacket(packet);
        assertEquals("", instance.getVin());
    }
}
