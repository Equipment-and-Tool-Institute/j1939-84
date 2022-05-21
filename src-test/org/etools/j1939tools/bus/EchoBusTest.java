/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the {@link EchoBus} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EchoBusTest {

    private static final int ADDR = 0xA5;

    private EchoBus instance;
    @Mock
    private MultiQueue<Packet> queue;

    @Test
    public void echoTest() throws BusException {
        try (Bus bus = new EchoBus(0xF9)) {
            Stream<Packet> stream1 = bus.read(1250, TimeUnit.MILLISECONDS);
            Stream<Packet> stream2 = bus.read(1250, TimeUnit.MILLISECONDS);
            final int count = 20;
            for (int id = 0; id < count; id++) {
                bus.send(Packet.create(0xFF00 | id, 0xF9, 1, 2, 3, id));
            }

            Packet r = stream1
                              // .peek(p -> System.err.println("filter:"+p))
                              .filter(x -> x.getId(0xFFFF) == 0xFF07)
                              .findFirst()
                              .get();
            assertEquals(0xFF07, r.getId(0xFFFF));
            assertEquals(count,
                         stream2.limit(count) // otherwise, stream will end with
                                              // timeout
                                // .peek(p -> System.err.println("count:" + p))
                                .count());
        }
    }

    @Before
    public void setUp() throws Exception {
        instance = new EchoBus(0xA5, queue);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(queue);
    }

    @Test
    public void testGetAddress() {
        assertEquals(ADDR, instance.getAddress());
    }

    @Test
    public void testGetConnectionSpeed() throws Exception {
        try {
            instance.getConnectionSpeed();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Could not be determined", e.getMessage());
        }
    }

    @Test
    public void testRead() throws Exception {
        instance.read(1250, TimeUnit.MILLISECONDS);
        verify(queue).stream(1250, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReadWithArg() {
        instance.read(5, TimeUnit.DAYS);
        verify(queue).stream(5, TimeUnit.DAYS);
    }

    @Test
    public void testSend() {
        Packet packet = Packet.create(123, ADDR, 11, 22, 33, 44, 55);
        instance.send(packet);
        verify(queue).add(packet);
    }

    @Test
    public void testSendGetsRead() throws Exception {
        instance = new EchoBus(ADDR);
        Stream<Packet> results = instance.read(1250, TimeUnit.MILLISECONDS);
        Packet packet = Packet.create(123, ADDR, 11, 22, 33, 44, 55);
        instance.send(packet);
        List<Packet> packets = results.collect(Collectors.toList());
        assertEquals(1, packets.size());
        assertSame(packet, packets.get(0));
    }

}
