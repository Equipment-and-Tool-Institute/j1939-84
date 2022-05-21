/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the {@link RP1210Bus} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class RP1210BusTest {

    private static final int ADDRESS = 0xA5;
    private static final byte[] ADDRESS_CLAIM_PARAMS = new byte[] { (byte) ADDRESS, 0, 0, (byte) 0xE0, (byte) 0xFF, 0,
            (byte) 0x81, 0, 0, 0 };

    private final ArgumentCaptor<Runnable> rp1210Captor = ArgumentCaptor.forClass(Runnable.class);

    private final ArgumentCaptor<Runnable> decodingCaptor = ArgumentCaptor.forClass(Runnable.class);

    private Adapter adapter;

    @Mock
    private ExecutorService decodingExecutor;

    @Mock
    private ExecutorService rp1210Executor;

    private RP1210Bus instance;

    @Mock
    private Logger logger;

    @Mock
    private MultiQueue<Packet> queue;

    @Mock
    private RP1210Library rp1210Library;

    private void createInstance() throws BusException {
        instance = new RP1210Bus(rp1210Library,
                                 decodingExecutor,
                                 rp1210Executor,
                                 queue,
                                 adapter,
                                 "J1939:Baud=Auto",
                                 ADDRESS,
                                 true,
                                 logger);
    }

    @Before
    public void setUp() throws Exception {
        adapter = new Adapter("Testing Adapter", "TST_ADPTR", (short) 42);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(rp1210Library, decodingExecutor, rp1210Executor, queue, logger);
    }

    private void startInstance() throws Exception {
        when(rp1210Library.RP1210_ClientConnect(0, (short) 42, "J1939:Baud=Auto", 0, 0, (short) 1))
                                                                                                   .thenReturn((short) 1);
        when(rp1210Library.RP1210_SendCommand(eq((short) 19),
                                              eq((short) 1),
                                              aryEq(ADDRESS_CLAIM_PARAMS),
                                              eq((short) 10))).thenReturn((short) 0);
        when(rp1210Library.RP1210_SendCommand(eq((short) 16),
                                              eq((short) 1),
                                              aryEq(new byte[] { (byte) 1 }),
                                              eq((short) 1))).thenReturn((short) 0);
        when(rp1210Library.RP1210_SendCommand(eq((short) 3),
                                              eq((short) 1),
                                              aryEq(new byte[] {}),
                                              eq((short) 0))).thenReturn((short) 0);

        when(decodingExecutor.submit(decodingCaptor.capture())).thenReturn(null);
        when(rp1210Executor.submit(rp1210Captor.capture())).thenReturn(null);

        createInstance();

        verify(rp1210Library).RP1210_ClientConnect(0, adapter.getDeviceId(), "J1939:Baud=Auto", 0, 0, (short) 1);
        verify(rp1210Library).RP1210_SendCommand(eq((short) 19),
                                                 eq((short) 1),
                                                 aryEq(ADDRESS_CLAIM_PARAMS),
                                                 eq((short) 10));
        verify(rp1210Library).RP1210_SendCommand(eq((short) 16),
                                                 eq((short) 1),
                                                 aryEq(new byte[] { (byte) 1 }),
                                                 eq((short) 1));
        verify(rp1210Library).RP1210_SendCommand(eq((short) 3),
                                                 eq((short) 1),
                                                 aryEq(new byte[] {}),
                                                 eq((short) 0));
        verify(rp1210Executor).submit(any(Runnable.class));
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testConstructorAddressClaimFails() throws Exception {
        when(rp1210Library.RP1210_ClientConnect(0, (short) 42, "J1939:Baud=Auto", 0, 0, (short) 1))
                                                                                                   .thenReturn((short) 1);

        when(rp1210Library.RP1210_SendCommand(eq((short) 19),
                                              eq((short) 1),
                                              aryEq(ADDRESS_CLAIM_PARAMS),
                                              eq((short) 10))).thenReturn((short) -99);
        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 99), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Testing Failure".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });
        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) 0);
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);

        try {
            createInstance();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Failed to configure adapter.", e.getMessage());
            assertEquals("Error (99): Testing Failure", e.getCause().getMessage());
        }

        verify(rp1210Library).RP1210_ClientConnect(0, adapter.getDeviceId(), "J1939:Baud=Auto", 0, 0, (short) 1);
        verify(rp1210Library).RP1210_SendCommand(eq((short) 19),
                                                 eq((short) 1),
                                                 aryEq(ADDRESS_CLAIM_PARAMS),
                                                 eq((short) 10));
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 99), any());
        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Executor).shutdown();
    }

    @Test
    public void testConstructorConnectFails() {
        when(rp1210Library.RP1210_ClientConnect(0, (short) 42, "J1939:Baud=Auto", 0, 0, (short) 1))
                                                                                                   .thenReturn((short) 134);
        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 134), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Device Not Connected".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });

        try {
            createInstance();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Error (134): Device Not Connected", e.getMessage());
        }

        verify(rp1210Library).RP1210_ClientConnect(0, adapter.getDeviceId(), "J1939:Baud=Auto", 0, 0, (short) 1);
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 134), any());
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testConstructorEchoFails() throws Exception {
        when(rp1210Library.RP1210_ClientConnect(0, (short) 42, "J1939:Baud=Auto", 0, 0, (short) 1))
                                                                                                   .thenReturn((short) 1);

        when(rp1210Library.RP1210_SendCommand(eq((short) 19),
                                              eq((short) 1),
                                              aryEq(ADDRESS_CLAIM_PARAMS),
                                              eq((short) 10))).thenReturn((short) 0);
        when(rp1210Library.RP1210_SendCommand(eq((short) 16),
                                              eq((short) 1),
                                              aryEq(new byte[] { (byte) 1 }),
                                              eq((short) 1))).thenReturn((short) -99);
        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 99), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Testing Failure".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });
        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) 0);
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);

        try {
            createInstance();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Failed to configure adapter.", e.getMessage());
            assertEquals("Error (99): Testing Failure", e.getCause().getMessage());
        }

        verify(rp1210Library).RP1210_ClientConnect(0, adapter.getDeviceId(), "J1939:Baud=Auto", 0, 0, (short) 1);
        verify(rp1210Library).RP1210_SendCommand(eq((short) 19),
                                                 eq((short) 1),
                                                 aryEq(ADDRESS_CLAIM_PARAMS),
                                                 eq((short) 10));
        verify(rp1210Library).RP1210_SendCommand(eq((short) 16),
                                                 eq((short) 1),
                                                 aryEq(new byte[] { (byte) 1 }),
                                                 eq((short) 1));
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 99), any());
        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Executor).shutdown();
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testConstructorFilterFails() throws Exception {
        when(rp1210Library.RP1210_ClientConnect(0, (short) 42, "J1939:Baud=Auto", 0, 0, (short) 1))
                                                                                                   .thenReturn((short) 1);

        when(rp1210Library.RP1210_SendCommand(eq((short) 19),
                                              eq((short) 1),
                                              aryEq(ADDRESS_CLAIM_PARAMS),
                                              eq((short) 10))).thenReturn((short) 0);
        when(rp1210Library.RP1210_SendCommand(eq((short) 16),
                                              eq((short) 1),
                                              aryEq(new byte[] { (byte) 1 }),
                                              eq((short) 1))).thenReturn((short) 0);
        when(rp1210Library.RP1210_SendCommand(eq((short) 3),
                                              eq((short) 1),
                                              aryEq(new byte[] {}),
                                              eq((short) 0))).thenReturn((short) -99);

        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 99), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Testing Failure".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });
        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) 0);
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);

        try {
            createInstance();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Failed to configure adapter.", e.getMessage());
            assertEquals("Error (99): Testing Failure", e.getCause().getMessage());
        }

        verify(rp1210Library).RP1210_ClientConnect(0, adapter.getDeviceId(), "J1939:Baud=Auto", 0, 0, (short) 1);
        verify(rp1210Library).RP1210_SendCommand(eq((short) 19),
                                                 eq((short) 1),
                                                 aryEq(ADDRESS_CLAIM_PARAMS),
                                                 eq((short) 10));
        verify(rp1210Library).RP1210_SendCommand(eq((short) 16),
                                                 eq((short) 1),
                                                 aryEq(new byte[] { (byte) 1 }),
                                                 eq((short) 1));
        verify(rp1210Library).RP1210_SendCommand(eq((short) 3),
                                                 eq((short) 1),
                                                 aryEq(new byte[] {}),
                                                 eq((short) 0));
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 99), any());
        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Executor).shutdown();
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testConstructorStopFails() {
        when(rp1210Library.RP1210_ClientConnect(0, (short) 42, "J1939:Baud=Auto", 0, 0, (short) 1))
                                                                                                   .thenReturn((short) 1);

        when(rp1210Library.RP1210_SendCommand(eq((short) 19),
                                              eq((short) 1),
                                              aryEq(ADDRESS_CLAIM_PARAMS),
                                              eq((short) 10))).thenReturn((short) 0);
        when(rp1210Library.RP1210_SendCommand(eq((short) 16),
                                              eq((short) 1),
                                              aryEq(new byte[] { (byte) 1 }),
                                              eq((short) 1))).thenReturn((short) 0);
        when(rp1210Library.RP1210_SendCommand(eq((short) 3),
                                              eq((short) 1),
                                              aryEq(new byte[] {}),
                                              eq((short) 0))).thenReturn((short) -99);

        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 99), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Testing Failure".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });
        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        RejectedExecutionException expectedCause = new RejectedExecutionException();
        when(rp1210Executor.submit(submitCaptor.capture())).thenThrow(expectedCause);

        try {
            createInstance();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Failed to stop RP1210.", e.getMessage());
            assertEquals(expectedCause, e.getCause());
        }

        verify(rp1210Library).RP1210_ClientConnect(0, adapter.getDeviceId(), "J1939:Baud=Auto", 0, 0, (short) 1);
        verify(rp1210Library).RP1210_SendCommand(eq((short) 19),
                                                 eq((short) 1),
                                                 aryEq(ADDRESS_CLAIM_PARAMS),
                                                 eq((short) 10));
        verify(rp1210Library).RP1210_SendCommand(eq((short) 16),
                                                 eq((short) 1),
                                                 aryEq(new byte[] { (byte) 1 }),
                                                 eq((short) 1));
        verify(rp1210Library).RP1210_SendCommand(eq((short) 3),
                                                 eq((short) 1),
                                                 aryEq(new byte[] {}),
                                                 eq((short) 0));
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 99), any());
        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Executor).shutdown();
    }

    @Test
    public void testGetAddress() throws Exception {
        startInstance();
        assertEquals(ADDRESS, instance.getAddress());
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testGetConnectionSpeed() throws Exception {
        when(rp1210Library.RP1210_SendCommand(eq((short) 45), eq((short) 1), any(), eq((short) 128))).then(arg0 -> {
            byte[] bytes = arg0.getArgument(2);
            byte[] value = "500000".getBytes(UTF_8);
            System.arraycopy(value, 0, bytes, 0, value.length);
            return null;
        });

        ArgumentCaptor<Callable<Integer>> submitCaptor = ArgumentCaptor.forClass(Callable.class);
        Future<Integer> future = mock(Future.class);
        when(future.get()).thenReturn(500000);
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);

        startInstance();

        assertEquals(500000, instance.getConnectionSpeed());

        Callable<Integer> callable = submitCaptor.getValue();
        callable.call();
        verify(rp1210Executor).submit(any(Callable.class));

        verify(rp1210Library).RP1210_SendCommand(eq((short) 45), eq((short) 1), any(), eq((short) 128));
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testNoEchoFails() throws Exception {
        Packet packet = Packet.create(0x1234, 0x56, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE);
        byte[] encodedPacket = new byte[] { (byte) 0x34, (byte) 0x12, (byte) 0x00, (byte) 0x06, (byte) 0x56,
                (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD,
                (byte) 0xEE };

        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) 0);
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);

        startInstance();
        try {
            assertNull(instance.send(packet));
        } catch (BusException e) {
            assertEquals("Failed to send: 18123456 [8] 77 88 99 AA BB CC DD EE", e.getMessage());
        }
        Callable<Short> callable = submitCaptor.getValue();
        callable.call();

        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Library).RP1210_SendMessage(eq((short) 1),
                                                 aryEq(encodedPacket),
                                                 eq((short) 14),
                                                 eq((short) 0),
                                                 eq((short) 0));
        verify(queue).stream(anyLong(), any());
    }

    @Test
    public void testPoll() throws Exception {
        Packet packet = Packet.create(0x1234, 0x56, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE);
        byte[] encodedPacket = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x34, (byte) 0x12, (byte) 0x00,
                (byte) 0x06, (byte) 0x56, (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
                (byte) 0xCC, (byte) 0xDD, (byte) 0xEE };
        when(rp1210Library.RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 32), eq((short) 0)))
                                                                                                               .thenAnswer(arg0 -> {
                                                                                                                   byte[] data = arg0.getArgument(1);
                                                                                                                   System.arraycopy(encodedPacket,
                                                                                                                                    0,
                                                                                                                                    data,
                                                                                                                                    0,
                                                                                                                                    encodedPacket.length);
                                                                                                                   return (short) encodedPacket.length;
                                                                                                               })
                                                                                                               .thenReturn((short) 0);

        startInstance();
        rp1210Captor.getAllValues().forEach(Runnable::run);
        decodingCaptor.getAllValues().forEach(Runnable::run);

        ArgumentCaptor<Packet> packetCaptor = ArgumentCaptor.forClass(Packet.class);
        verify(queue).add(packetCaptor.capture());

        Packet actual = packetCaptor.getValue();
        assertEquals(packet, actual);

        verify(logger).log(eq(Level.INFO), anyString());
        verify(rp1210Library, atLeast(2)).RP1210_ReadMessage(eq((short) 1),
                                                             any(byte[].class),
                                                             eq((short) 32),
                                                             eq((short) 0));
        verify(decodingExecutor).submit(any(Runnable.class));
        verify(rp1210Executor, times(2)).submit(any(Runnable.class));
    }

    @Test
    public void testPollFails() throws Exception {
        when(rp1210Library.RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 32), eq((short) 0)))
                                                                                                               .thenReturn((short) -99);

        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 99), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Testing Failure".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });

        startInstance();
        Runnable runnable = rp1210Captor.getValue();
        runnable.run();

        verify(queue, never()).add(any(Packet.class));
        verify(rp1210Library).RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 32), eq((short) 0));
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 99), any());
        verify(logger).log(eq(Level.SEVERE), eq("Failed to read RP1210"), any(BusException.class));
    }

    @Test
    public void testPollTransmitted() throws Exception {
        Packet packet = Packet.create(0x06,
                                      0x1234,
                                      0x56,
                                      true,
                                      (byte) 0x77,
                                      (byte) 0x88,
                                      (byte) 0x99,
                                      (byte) 0xAA,
                                      (byte) 0xBB,
                                      (byte) 0xCC,
                                      (byte) 0xDD,
                                      (byte) 0xEE);
        byte[] encodedPacket = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0x34, (byte) 0x12, (byte) 0x00,
                (byte) 0x06, (byte) 0x56, (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
                (byte) 0xCC, (byte) 0xDD, (byte) 0xEE };
        when(rp1210Library.RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 32), eq((short) 0)))
                                                                                                               .thenAnswer(arg0 -> {
                                                                                                                   byte[] data = arg0.getArgument(1);
                                                                                                                   System.arraycopy(encodedPacket,
                                                                                                                                    0,
                                                                                                                                    data,
                                                                                                                                    0,
                                                                                                                                    encodedPacket.length);
                                                                                                                   return (short) encodedPacket.length;
                                                                                                               })
                                                                                                               .thenReturn((short) 0);

        startInstance();
        Runnable runnable = rp1210Captor.getValue();
        runnable.run();

        decodingCaptor.getValue().run();

        ArgumentCaptor<Packet> packetCaptor = ArgumentCaptor.forClass(Packet.class);
        verify(queue).add(packetCaptor.capture());
        Packet actual = packetCaptor.getValue();

        assertEquals(packet, actual);
        verify(logger).log(eq(Level.INFO), anyString());
        verify(rp1210Library, atLeast(2)).RP1210_ReadMessage(eq((short) 1),
                                                             any(byte[].class),
                                                             eq((short) 32),
                                                             eq((short) 0));
        verify(rp1210Executor, times(2)).submit(any(Runnable.class));
        verify(decodingExecutor).submit(any(Runnable.class));
    }

    @Test
    public void testRead() throws Exception {
        startInstance();
        Stream<Packet> stream = Stream.empty();
        when(queue.stream(1250, TimeUnit.MILLISECONDS)).thenReturn(stream);
        assertSame(stream, instance.read(1250, TimeUnit.MILLISECONDS));
        verify(queue).stream(1250, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testReadWithArgs() throws Exception {
        startInstance();
        Stream<Packet> stream = Stream.empty();
        when(queue.stream(99, TimeUnit.NANOSECONDS)).thenReturn(stream);
        assertSame(stream, instance.read(99, TimeUnit.NANOSECONDS));
        verify(queue).stream(99, TimeUnit.NANOSECONDS);
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testSend() throws Exception {
        Packet packet = Packet.create(0x1234, 0x56, true, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE);
        byte[] encodedPacket = new byte[] { (byte) 0x34, (byte) 0x12, (byte) 0x00, (byte) 0x06, (byte) 0x56,
                (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD,
                (byte) 0xEE };
        ArgumentCaptor<Callable<Optional<String>>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Optional<String>> future = mock(Future.class);
        when(future.get()).thenReturn(Optional.empty());
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);
        // implement echo
        when(queue.stream(ArgumentMatchers.anyLong(), ArgumentMatchers.any())).thenReturn(Stream.of(packet));

        startInstance();
        instance.send(packet);

        Callable<Optional<String>> callable = submitCaptor.getValue();
        assertEquals(callable.call(), Optional.empty());

        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Library).RP1210_SendMessage(eq((short) 1),
                                                 aryEq(encodedPacket),
                                                 eq((short) 14),
                                                 eq((short) 0),
                                                 eq((short) 0));
        verify(queue).stream(anyLong(), any());
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testSendFails() throws Exception {
        Packet packet = Packet.create(0x1234, 0x56, true, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE);
        byte[] encodedPacket = new byte[] { (byte) 0x34, (byte) 0x12, (byte) 0x00, (byte) 0x06, (byte) 0x56,
                (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD,
                (byte) 0xEE };

        when(rp1210Library.RP1210_SendMessage((short) 1,
                                              encodedPacket,
                                              (short) encodedPacket.length,
                                              (short) 0,
                                              (short) 0))
                                                         .thenReturn((short) -99);

        ArgumentCaptor<Callable<Optional<String>>> submitCaptor = ArgumentCaptor.forClass(Callable.class);
        Future<Optional<String>> future = mock(Future.class);
        when(future.get()).thenReturn(Optional.of("Failed to send: 18123456 [8] 77 88 99 AA BB CC DD EE (TX)"));
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);

        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 99), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Testing Failure".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });

        startInstance();
        try {
            instance.send(packet);
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Failed to send: 18123456 [8] 77 88 99 AA BB CC DD EE (TX)", e.getMessage());
        }
        Callable<Optional<String>> callable = submitCaptor.getValue();
        assertEquals(Optional.of("Error (99): Testing Failure"), callable.call());

        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Library).RP1210_SendMessage(eq((short) 1),
                                                 aryEq(encodedPacket),
                                                 eq((short) 14),
                                                 eq((short) 0),
                                                 eq((short) 0));
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 99), any());
        verify(queue).stream(anyLong(), any());
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testStop() throws Exception {
        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) -99);
        when(rp1210Executor.submit(submitCaptor.capture())).thenReturn(future);

        startInstance();
        instance.stop();

        Callable<Short> callable = submitCaptor.getValue();
        callable.call();

        verify(rp1210Library).RP1210_ClientDisconnect((short) 1);
        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Executor).shutdown();

    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void testStopFails() throws Exception {

        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        RejectedExecutionException expectedCause = new RejectedExecutionException();
        when(rp1210Executor.submit(submitCaptor.capture())).thenThrow(expectedCause);

        startInstance();
        try {
            instance.stop();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Failed to stop RP1210.", e.getMessage());
            assertEquals(expectedCause, e.getCause());
        }

        verify(rp1210Executor).submit(any(Callable.class));
        verify(rp1210Executor).shutdown();
    }

    /** Traffic generator to verify that packet order is preserved. Requires adapter to run. */
    public static void main(String... args) throws Exception {
        Adapter adapter = new Adapter("Nexiq USBLink 2", "NULN2R32", (short) 1);
        final int TOOL = 0xFA;
        try (RP1210Bus bus = new RP1210Bus(adapter, "J1939:Baud=Auto", TOOL, true)) {

            Stream<Packet> in = bus.read(1, TimeUnit.DAYS);
            // log any out of order
            new Thread(new Runnable() {
                long last;
                Packet lastPacket;

                @Override
                public void run() {
                    in.filter(p -> p.getSource() == TOOL)
                      .forEach(p -> {
                          long n = new BigInteger(p.getBytes()).longValue();
                          if (n - last <= 0) {
                              System.err.println("XXX " + lastPacket + " -> " + p);
                          }
                          last = n;
                          lastPacket = p;
                      });
                }
            }, "order validation").start();

            BigInteger number = new BigInteger("1000000000000000", 16);
            //noinspection InfiniteLoopStatement
            while (true) {
                bus.sendRaw(RP1210Bus.encode(Packet.create(0x18FFFF, TOOL, number.toByteArray())));
                number = number.add(BigInteger.ONE);
            }
        }
    }
}
