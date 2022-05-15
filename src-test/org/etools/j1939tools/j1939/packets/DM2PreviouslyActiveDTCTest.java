/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM2PreviouslyActiveDTC} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class DM2PreviouslyActiveDTCTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        assertEquals("DM2", new DM2PreviouslyActiveDTC(packet).getName());
    }

    @Test
    public void testPGN() {
        assertEquals(65227, DM2PreviouslyActiveDTC.PGN);
    }

}
