/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM31ScaledTestResults} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class DM31ScaledTestResultTest {

    @Test
    public void testEmptyDTCs() {
        Packet packet = Packet.create(0,
                0);
        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals("DM31", instance.getName());
        assertTrue(instance.getDtcPackets().isEmpty());
        assertEquals(0, instance.getDtcPackets().size());
    }

    @Test
    public void testOneDTCs() {
        Packet packet = Packet.create(0,
                0,
                0x31,
                0x4E,
                0x4B,
                0x44,
                0x58,
                0x34);
        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals("DM31", instance.getName());
        assertEquals(1, instance.getDtcPackets().size());
    }

    @Test
    public void testPGN() {
        assertEquals(41728, DM31ScaledTestResults.PGN);
    }

    @Test
    public void testThreeDTCs() {
        Packet packet = Packet.create(0,
                0,
                0x31,
                0x4E,
                0x4B,
                0x44,
                0x58,
                0x34,
                0x54,
                0x58,
                0x30,
                0x45,
                0x4A,
                0x34,
                0x30,
                0x37,
                0x36,
                0x36,
                0x37,
                0x2A);
        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals("DM31", instance.getName());
        assertEquals(3, instance.getDtcPackets().size());
    }

}
