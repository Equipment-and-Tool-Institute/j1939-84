/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests the {@link RequestResult} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestResultTest {

    @Mock
    private AcknowledgmentPacket ackPacket;

    private RequestResult<?> instance;

    @Mock
    private ParsedPacket packet;

    @Before
    public void setUp() {

        instance = new RequestResult<>(false, Collections.singletonList(packet), Collections.singletonList(ackPacket));
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#equals(java.lang.Object)}.
     */
    @Test
    public void testEquals() {
        RequestResult<?> expected = new RequestResult<>(false, Collections.singletonList(packet),
                Collections.singletonList(ackPacket));
        assertTrue(instance.equals(expected));

        RequestResult<?> expected2 = new RequestResult<>(true, Collections.emptyList(),
                Collections.singletonList(ackPacket));
        assertFalse(instance.equals(expected2));

        RequestResult<?> expected3 = new RequestResult<>(false, Collections.emptyList(),
                Collections.singletonList(ackPacket));
        assertFalse(instance.equals(expected3));

        RequestResult<?> expected4 = new RequestResult<>(false, Collections.singletonList(packet),
                Collections.emptyList());
        assertFalse(instance.equals(expected4));

        assertTrue(instance.equals(instance));
        assertFalse(instance.equals(new Object()));

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#getAcks()}.
     */
    @Test
    public void testGetAcks() {
        assertEquals(Collections.singletonList(ackPacket), instance.getAcks());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#getEither()}.
     */
    @Test
    public void testGetEither() {
        // TODO Implement this test;
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#getPackets()}.
     */
    @Test
    public void testGetPackets() {
        assertEquals(Collections.singletonList(packet), instance.getPackets());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#hashCode()}.
     */
    @Test
    public void testHashCode() {
        RequestResult<?> instance1 = new RequestResult<>(false, Collections.singletonList(packet),
                Collections.singletonList(ackPacket));
        RequestResult<?> instance2 = new RequestResult<>(false, Collections.singletonList(packet),
                Collections.singletonList(ackPacket));
        assertTrue(instance1.hashCode() == instance2.hashCode());
        RequestResult<?> instance11 = new RequestResult<>(true, Collections.singletonList(packet),
                Collections.singletonList(ackPacket));
        RequestResult<?> instance22 = new RequestResult<>(false, Collections.singletonList(packet),
                Collections.singletonList(ackPacket));
        assertTrue(instance11.hashCode() != instance22.hashCode());

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#isRetryUsed()}.
     */
    @Test
    public void testIsRetryUsed() {
        assertEquals(false, instance.isRetryUsed());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#RequestResult(boolean, java.util.List)}.
     */
    @Test
    public void testRequestResultBooleanListOfEitherOfTAcknowledgmentPacket() {
        // TODO Implement this test;
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#RequestResult(boolean, java.util.List, java.util.List)}.
     */
    @Test
    public void testRequestResultBooleanListOfTListOfAcknowledgmentPacket() {
        // TODO Implement this test;
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.model.RequestResult#toString()}.
     */
    @Test
    public void testToString() {
        StringBuilder expected = new StringBuilder("RequestResult");
        expected.append(NL)
                .append("Retry  used : false")
                .append(NL)
                .append("Response packets :")
                .append(NL)
                .append("Source address : 0 returned packet")
                .append(NL)
                .append("Ack packets :")
                .append(NL)
                .append("Source address : 0 returned ackPacket");
        assertEquals(expected.toString(), instance.toString());

        RequestResult<?> instance1 = new RequestResult<>(false, Collections.emptyList(), Collections.emptyList());
        StringBuilder expected1 = new StringBuilder("RequestResult");
        expected1.append(NL)
                .append("Retry  used : false")
                .append(NL)
                .append("Response packets :")
                .append(NL)
                .append("No packets returned")
                .append(NL)
                .append("Ack packets :")
                .append(NL)
                .append("No acks returned");
        assertEquals(expected1.toString(), instance1.toString());

        boolean thrown = false;
        try {
            RequestResult<?> instance2 = new RequestResult<>(false, null, null);
            assertEquals(false, instance2.isRetryUsed());
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

}
