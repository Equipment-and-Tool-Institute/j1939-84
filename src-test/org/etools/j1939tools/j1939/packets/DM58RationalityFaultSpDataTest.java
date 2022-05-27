/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.etools.j1939tools.utils.CollectionUtils;
import org.etools.testdoc.TestDoc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the {@link DM5DiagnosticReadinessPacket} class
 *
 * @author Marianne Schaefer (marianne@soliddesign.net)
 *
 */
@TestDoc()
public class DM58RationalityFaultSpDataTest {
    private DM58RationalityFaultSpData instance;
    private final int[] DATA = { 0xF5, 0xBE, 0x00, 0x17, 0xDD, 0xCC, 0xBB, 0xAA };
    private final int[] DATA1 = { 0xF5, 0xC8, 0x04, 0x00, 0xCD, 0x01, 0xF4, 0x04 };

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        instance = new DM58RationalityFaultSpData(Packet.create(DM58RationalityFaultSpData.PGN, 0x00, DATA));
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
    }

    @Test
    public void testCreate() {
        SupportedSPN spn = new SupportedSPN(new int[] { 0xBE, 0x00, 0x17, 0xDD, 0xCC, 0xBB, 0xAA });
        DM58RationalityFaultSpData createdInstance = DM58RationalityFaultSpData.create(0x00, 245, spn);
        assertEquals("DM58", createdInstance.getName());
        assertEquals(190, createdInstance.getSpnId());
        assertEquals(245, createdInstance.getTestId());
        assertArrayEquals(Arrays.copyOfRange(CollectionUtils.toByteArray(DATA), 4, 8),
                          createdInstance.getSpnDataBytes());

        DM58RationalityFaultSpData createdInstance2 = DM58RationalityFaultSpData.create(0x00,
                                                                                        245,
                                                                                        1224,
                                                                                        new int[] { 0xCD, 0x01, 0xF4,
                                                                                                0x04 });
        assertEquals("DM58", createdInstance2.getName());
        assertEquals(1224, createdInstance2.getSpnId());
        assertEquals(245, createdInstance2.getTestId());
        assertArrayEquals(Arrays.copyOfRange(CollectionUtils.toByteArray(DATA1), 4, 8),
                          createdInstance2.getSpnDataBytes());
    }

    @Test
    public void getName() {
        assertEquals("DM58", instance.getName());
    }

    @Test
    public void getTestId() {
        assertEquals(245, instance.getTestId());
    }

    @Test
    public void getSpnId() {
        assertEquals(190, instance.getSpnId());
    }

    @Test
    public void getSpnData() {
        assertArrayEquals(Arrays.copyOfRange(CollectionUtils.toByteArray(DATA), 4, 8), instance.getSpnDataBytes());
    }

    @Test
    public void testToString() {
        String expected = "DM58 from Engine #1 (0): " + NL;
        expected += "  Test Identifier: 245" + NL;
        expected += "  Rationality Fault SPN: 190" + NL;
        expected += "  Rationality Fault SPN Data Value: [DD CC]" + NL;
        expected += "  SPN   190, Engine Speed: 6555.625 rpm" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPacking() {

        var instance = DM58RationalityFaultSpData.create(0xF9, 247, 0x7FFFF, new int[0]);

        Packet packet = instance.getPacket();
        assertEquals(DM58RationalityFaultSpData.PGN, packet.getPgn());
        assertEquals(0xFF, packet.getDestination());
        assertEquals(0xF9, packet.getSource());

        assertEquals(247, instance.getTestId());
        assertEquals(0x7FFFF, instance.getSpn().getId());
    }
}
