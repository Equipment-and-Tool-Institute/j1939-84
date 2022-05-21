/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.DM29DtcCounts.PGN;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the {@link DM25ExpandedFreezeFrameTest} class
 */
public class DM29DtcCountsTest {

    private DM29DtcCounts instance;

    @Before
    public void setUp() {
        instance = DM29DtcCounts.create(0, 0, 9, 32, 71, 49, 1);
    }

    /**
     * Test method for
     * {@link DM29DtcCounts#getAllPendingDTCCount()}..
     */
    @Test
    public void testGetAllPendingDTCCount() {
        assertEquals(32, instance.getAllPendingDTCCount());
        // Verify the value is cached
        assertEquals(32, instance.getAllPendingDTCCount());
    }

    /**
     * Test method for
     * {@link DM29DtcCounts#getEmissionRelatedMILOnDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedMILOnDTCCount() {
        assertEquals(71, instance.getEmissionRelatedMILOnDTCCount());
        // Verify the value is cached
        assertEquals(71, instance.getEmissionRelatedMILOnDTCCount());
    }

    /**
     * Test method for
     * {@link DM29DtcCounts#getEmissionRelatedPreviouslyMILOnDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedPendingDTCCount() {
        assertEquals(9, instance.getEmissionRelatedPendingDTCCount());
        // Verify the value is cached
        assertEquals(9, instance.getEmissionRelatedPendingDTCCount());

    }

    /**
     * Test method for
     * {@link DM29DtcCounts#getEmissionRelatedPermanentDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedPermanentDTCCount() {
        assertEquals(1, instance.getEmissionRelatedPermanentDTCCount());
        // Verify the value is cached
        assertEquals(1, instance.getEmissionRelatedPermanentDTCCount());
    }

    /**
     * Test method for
     * {@link DM29DtcCounts#getEmissionRelatedPreviouslyMILOnDTCCount()}..
     */
    @Test
    public void testGetEmissionRelatedPreviouslyMILOnDTCCount() {
        assertEquals(49, instance.getEmissionRelatedPreviouslyMILOnDTCCount());
        // Verify the value is cached
        assertEquals(49, instance.getEmissionRelatedPreviouslyMILOnDTCCount());

    }

    /**
     * Test method for
     * {@link DM29DtcCounts#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals("DM29", instance.getName());
    }

    /**
     * Test method for
     * {@link DM29DtcCounts#DM29DtcCounts(org.etools.j1939_84.bus.Packet)}.
     */
    @Test
    public void testPGN() {
        assertEquals(40448, PGN);
    }

    /**
     * Test method for
     * {@link DM29DtcCounts#toString()}.
     */
    @Test
    public void testToString() {
        String expected = "DM29 from Engine #1 (0): " + NL;
        expected += "Emission-Related Pending DTC Count                               9" + NL;
        expected += "All Pending DTC Count                                           32" + NL;
        expected += "Emission-Related MIL-On DTC Count                               71" + NL;
        expected += "Emission-Related Previously MIL-On DTC Count                    49" + NL;
        expected += "Emission-Related Permanent DTC Count                             1";
        assertEquals(expected, instance.toString());
    }

    /**
     * Test method for
     * {@link DM29DtcCounts#toString()}.
     */
    @Test
    public void testToStringWithNA() {
        Packet packet = Packet.create(PGN, 0, 0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);

        instance = new DM29DtcCounts(packet);
        String expected = "DM29 from Engine #1 (0): " + NL;
        expected += "Emission-Related Pending DTC Count                           error" + NL;
        expected += "All Pending DTC Count                                not available" + NL;
        expected += "Emission-Related MIL-On DTC Count                    not available" + NL;
        expected += "Emission-Related Previously MIL-On DTC Count         not available" + NL;
        expected += "Emission-Related Permanent DTC Count                 not available";
        assertEquals(expected, instance.toString());
    }

}
