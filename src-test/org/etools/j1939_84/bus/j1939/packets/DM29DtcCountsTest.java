/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.bus.Packet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the {@link DM25ExpandedFreezeFrameTest} class
 *
 */
public class DM29DtcCountsTest {

    private DM29DtcCounts instance;
    private Packet packet;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        packet = Packet.create(DM29DtcCounts.PGN,
                0,
                0x09,
                0x20,
                0x47,
                0x31,
                0x01,
                0xFF,
                0xFF,
                0xFF);

        instance = new DM29DtcCounts(packet);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts#getAllPendingDTCCount()}..
     */
    @Test
    public void testGetAllPendingDTCCount() {
        assertEquals(0x20, instance.getAllPendingDTCCount());
        // Verify the value is cached
        assertEquals(0x20, instance.getAllPendingDTCCount());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts#getEmissionRelatedMILOnDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedMILOnDTCCount() {
        assertEquals(0x47, instance.getEmissionRelatedMILOnDTCCount());
        // Verify the value is cached
        assertEquals(0x47, instance.getEmissionRelatedMILOnDTCCount());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts#getEmissionRelatedPreviouslyMILOnDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedPendingDTCCount() {
        assertEquals(0x09, instance.getEmissionRelatedPendingDTCCount());
        // Verify the value is cached
        assertEquals(0x09, instance.getEmissionRelatedPendingDTCCount());

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts#getEmissionRelatedPermanentDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedPermanentDTCCount() {
        assertEquals(0x01, instance.getEmissionRelatedPermanentDTCCount());
        // Verify the value is cached
        assertEquals(0x01, instance.getEmissionRelatedPermanentDTCCount());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts#getEmissionRelatedPreviouslyMILOnDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedPreviouslyMILOnDTCCount() {
        assertEquals(0x31, instance.getEmissionRelatedPreviouslyMILOnDTCCount());
        // Verify the value is cached
        assertEquals(0x31, instance.getEmissionRelatedPreviouslyMILOnDTCCount());

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals("DM29", instance.getName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts#DM29DtcCounts(org.etools.j1939_84.bus.Packet)}.
     */
    @Test
    public void testPGN() {
        assertEquals(40448, DM29DtcCounts.PGN);
    }

}
