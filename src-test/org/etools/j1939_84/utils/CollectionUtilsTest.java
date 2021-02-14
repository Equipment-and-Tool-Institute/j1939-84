/*
 * Copyright (c) 2020. Electronic Tools Institute
 */

package org.etools.j1939_84.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.junit.Test;

/**
 * The unit test for {@link CollectionUtils}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class CollectionUtilsTest {
    @Test
    public void areTwoListsEqualSameType() {
        // verify null objects
        assertTrue(CollectionUtils.areTwoCollectionsEqual(null, null));

        Collection<Packet> collectionA = new HashSet<>();
        // verify one real and one null lists of the same type
        assertFalse(CollectionUtils.areTwoCollectionsEqual(collectionA, null));

        Collection<Packet> collectionB = new HashSet<>();

        // verify two null lists of the same type
        assertTrue(CollectionUtils.areTwoCollectionsEqual(collectionA, collectionB));

        //verify one list null nad one with real packet not equals
        int[] dataA = {
                0x00, // SPN least significant bit
                0x00, // SPN most significant bit
                0x00, // Failure mode indicator
                0x00, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0x00, // Lamp Status/State
        };
        Packet packetA = Packet.create(
                0,
                0x00,
                dataA);
        collectionA.add(packetA);
        assertFalse(CollectionUtils.areTwoCollectionsEqual(collectionA, collectionB));

        //make them equal and verify again
        collectionB.add(packetA);
        assertTrue(CollectionUtils.areTwoCollectionsEqual(collectionA, collectionB));

        // add another packet to one and verify inequility
        int[] dataB = {
                0x00, // SPN least significant bit
                0x00, // SPN most significant bit
                0x00, // Failure mode indicator
                0x00, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0x00, // Lamp Status/State
        };
        Packet packetB = Packet.create(
                0,
                0x01,
                dataB);
        collectionB.add(packetB);
        assertFalse(CollectionUtils.areTwoCollectionsEqual(collectionA, collectionB));
    }

    @Test
    public void areTwoListsEqualDifferentTypes() {
        int[] dataA = {
                0x00, // SPN least significant bit
                0x00, // SPN most significant bit
                0x00, // Failure mode indicator
                0x00, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0x00, // Lamp Status/State
        };
        DM20MonitorPerformanceRatioPacket packetA = new DM20MonitorPerformanceRatioPacket(Packet.create(
                DM20MonitorPerformanceRatioPacket.PGN,
                0x00,
                dataA));
        Collection<DM20MonitorPerformanceRatioPacket> collectionA = new HashSet<>();
        collectionA.add(packetA);

        //  Verify inequality of a Collection<> and null
        assertFalse(CollectionUtils.areTwoCollectionsEqual(collectionA, null));

        int[] dataB = new int[] {
                0xF7, // Test Identifier
                0x22, // SPN, (bits 8-1) 8 least significant bits of SPN
                0x0D, // SPN, (bits 8-1) second byte of SPN (most significant at bit 8)
                0x1F, // SPN, (bits 8-6) 3 most significant bits(most significant at bit 8)
                //       FMI, (bits 5-1) FMI (most significant at bit 5)
                0xD0, // SLOT Identifier
                0x00, // SLOT Identifier
                0xB7, // Test Value
                0x03, // Test Value
                0xE8, // Test Limit Maximum
                0x03, // Test Limit Maximum
                0x20, // Test Limit Minimum
                0x03  //Test Limit Minimum
        };
        DM30ScaledTestResultsPacket packetB = new DM30ScaledTestResultsPacket(Packet.create(DM30ScaledTestResultsPacket.PGN,
                                                                                            0x00,
                                                                                            dataB));

        // Verify two equal Collections
        Collection<DM30ScaledTestResultsPacket> collectionB = new HashSet<>();
        collectionB.add(packetB);
        assertFalse(CollectionUtils.areTwoCollectionsEqual(collectionA, collectionB));

        //Verify inequality of one Collection<> and on List<>
        List<DM30ScaledTestResultsPacket> listC = new ArrayList<>();
        listC.add(packetB);
        assertTrue(CollectionUtils.areTwoCollectionsEqual(collectionB, listC));

        // Verify inequality of two different types of List<>
        List<DM20MonitorPerformanceRatioPacket> listD = new ArrayList<>();
        listD.add(packetA);
        assertFalse(CollectionUtils.areTwoCollectionsEqual(listC, listD));

        // Verify two equal List<>
        List<DM20MonitorPerformanceRatioPacket> listE = new ArrayList<>();
        listE.add(packetA);
        assertTrue(CollectionUtils.areTwoCollectionsEqual(listD, listE));
    }

    @Test
    public void testToIntArray() {

        byte[] input = new byte[255];
        int[] expected = new int[255];

        for (int i = 0; i < 255; i++) {
            input[i] = (byte) i;
            expected[i] = i;
        }

        int[] actual = CollectionUtils.toIntArray(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testToByteArray() {

        byte[] expected = new byte[255];
        int[] input = new int[255];

        for (int i = 0; i < 255; i++) {
            input[i] = i;
            expected[i] = (byte) i;
        }

        byte[] actual = CollectionUtils.toByteArray(input);
        assertArrayEquals(expected, actual);
    }
}