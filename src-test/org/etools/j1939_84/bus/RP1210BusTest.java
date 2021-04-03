/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.etools.j1939_84.controllers.Controller.Ending.ABORTED;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.etools.j1939_84.events.CompleteEvent;
import org.etools.j1939_84.events.EventBus;
import org.etools.j1939_84.events.ResultEvent;
import org.etools.j1939_84.events.UrgentEvent;
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
    private final ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    private Adapter adapter;
    @Mock
    private ScheduledExecutorService exec;
    private RP1210Bus instance;
    @Mock
    private Logger logger;
    @Mock
    private MultiQueue<Packet> queue;
    @Mock
    private RP1210Library rp1210Library;
    @Mock
    private EventBus eventBus;

    private void createInstance() throws BusException {
        instance = new RP1210Bus(rp1210Library,
                                 exec,
                                 queue,
                                 adapter,
                                 ADDRESS,
                                 true,
                                 (severity, string, e) -> logger.log(severity, string, e),
                                 eventBus);
    }

    @Before
    public void setUp() throws Exception {
        adapter = new Adapter("Testing Adapter", "TST_ADPTR", (short) 42);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(rp1210Library, exec, queue, logger, eventBus);
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

        when(exec.scheduleAtFixedRate(runnableCaptor.capture(), eq(1L), eq(1L), eq(TimeUnit.MILLISECONDS)))
                                                                                                           .thenReturn(null);

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
        verify(exec).scheduleAtFixedRate(any(Runnable.class), eq(1L), eq(1L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
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
        when(exec.submit(submitCaptor.capture())).thenReturn(future);

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
        verify(exec).submit(any(Callable.class));
        verify(exec).shutdown();
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
        when(exec.submit(submitCaptor.capture())).thenReturn(future);

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
        verify(exec).submit(any(Callable.class));
        verify(exec).shutdown();
    }

    @Test
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
        when(exec.submit(submitCaptor.capture())).thenReturn(future);

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
        verify(exec).submit(any(Callable.class));
        verify(exec).shutdown();
    }

    @Test
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
        when(exec.submit(submitCaptor.capture())).thenThrow(expectedCause);

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
        verify(exec).submit(any(Callable.class));
        verify(exec).shutdown();
    }

    @Test
    public void testGetAddress() throws Exception {
        startInstance();
        assertEquals(ADDRESS, instance.getAddress());
    }

    @Test
    public void testGetConnectionSpeed() throws Exception {
        when(rp1210Library.RP1210_SendCommand(eq((short) 45), eq((short) 1), any(), eq((short) 17))).then(arg0 -> {
            byte[] bytes = arg0.getArgument(2);
            byte[] value = "500000".getBytes(UTF_8);
            System.arraycopy(value, 0, bytes, 0, value.length);
            return null;
        });
        startInstance();
        assertEquals(500000, instance.getConnectionSpeed());
        verify(rp1210Library).RP1210_SendCommand(eq((short) 45), eq((short) 1), any(), eq((short) 17));
    }

    @Test
    public void testNoEchoFails() throws Exception {
        Packet packet = Packet.create(0x1234, 0x56, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE);
        byte[] encodedPacket = new byte[] { (byte) 0x34, (byte) 0x12, (byte) 0x00, (byte) 0x06, (byte) 0x56,
                (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD,
                (byte) 0xEE };

        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) 0);
        when(exec.submit(submitCaptor.capture())).thenReturn(future);

        startInstance();
        try {
            assertNull(instance.send(packet));
        } catch (BusException e) {
            assertEquals("Failed to send: 18123456 [8] 77 88 99 AA BB CC DD EE", e.getMessage());
        }
        Callable<Short> callable = submitCaptor.getValue();
        callable.call();

        verify(exec).submit(any(Callable.class));
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
        when(rp1210Library.RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 2048), eq((short) 0)))
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
        Runnable runnable = runnableCaptor.getValue();
        runnable.run();

        ArgumentCaptor<Packet> packetCaptor = ArgumentCaptor.forClass(Packet.class);
        verify(queue).add(packetCaptor.capture());
        Packet actual = packetCaptor.getValue();

        assertEquals(packet, actual);
        verify(logger).log(eq(Level.FINE), anyString(), (Throwable) any());
        verify(logger).log(eq(Level.INFO), anyString(), (Throwable) any());
        verify(rp1210Library, times(2)).RP1210_ReadMessage(eq((short) 1),
                                                           any(byte[].class),
                                                           eq((short) 2048),
                                                           eq((short) 0));
    }

    @Test
    public void testPollFails() throws Exception {
        when(rp1210Library.RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 2048), eq((short) 0)))
                                                                                                                 .thenReturn((short) -99);

        when(rp1210Library.RP1210_GetErrorMsg(eq((short) 99), any())).thenAnswer(arg0 -> {
            byte[] dest = arg0.getArgument(1);
            byte[] src = "Testing Failure".getBytes(UTF_8);
            System.arraycopy(src, 0, dest, 0, src.length);
            return (short) 0;
        });

        startInstance();
        Runnable runnable = runnableCaptor.getValue();
        runnable.run();

        verify(queue, never()).add(any(Packet.class));
        verify(rp1210Library).RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 2048), eq((short) 0));
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
        when(rp1210Library.RP1210_ReadMessage(eq((short) 1), any(byte[].class), eq((short) 2048), eq((short) 0)))
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
        Runnable runnable = runnableCaptor.getValue();
        runnable.run();

        ArgumentCaptor<Packet> packetCaptor = ArgumentCaptor.forClass(Packet.class);
        verify(queue).add(packetCaptor.capture());
        Packet actual = packetCaptor.getValue();

        assertEquals(packet, actual);
        verify(logger).log(eq(Level.FINE), anyString(), (Throwable) any());
        verify(logger).log(eq(Level.INFO), anyString(), (Throwable) any());
        verify(rp1210Library, times(2)).RP1210_ReadMessage(eq((short) 1),
                                                           any(byte[].class),
                                                           eq((short) 2048),
                                                           eq((short) 0));
    }

    @Test
    public void testPollWithImposter() throws Exception {
        Packet packet = Packet.create(0x06,
                                      0x1234,
                                      ADDRESS,
                                      false,
                                      (byte) 0x77,
                                      (byte) 0x88,
                                      (byte) 0x99,
                                      (byte) 0xAA,
                                      (byte) 0xBB,
                                      (byte) 0xCC,
                                      (byte) 0xDD,
                                      (byte) 0xEE);
        byte[] encodedPacket = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x34, (byte) 0x12, (byte) 0x00,
                (byte) 0x06, (byte) ADDRESS, (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA,
                (byte) 0xBB,
                (byte) 0xCC, (byte) 0xDD, (byte) 0xEE };
        when(rp1210Library.RP1210_ReadMessage(eq((short) 1),
                                              any(byte[].class),
                                              eq((short) 2048),
                                              eq((short) 0)))
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

        String uiMsg = "Unexpected Service Tool Message from SA 0xF9 observed. Test results uncertain. False failures are possible";

        doAnswer(invocationOnMock -> {
            UrgentEvent event = invocationOnMock.getArgument(0);
            event.getQuestionListener().answered(CANCEL);
            return null;
        }).when(eventBus).publish(any(UrgentEvent.class));

        startInstance();
        Runnable runnable = runnableCaptor.getValue();
        runnable.run();

        ArgumentCaptor<Packet> packetCaptor = ArgumentCaptor.forClass(Packet.class);
        verify(queue).add(packetCaptor.capture());
        Packet actual = packetCaptor.getValue();

        assertEquals(packet, actual);
        verify(logger).log(eq(Level.FINE), anyString(), (Throwable) any());
        verify(logger).log(eq(Level.INFO), anyString(), (Throwable) any());
        verify(rp1210Library, times(2)).RP1210_ReadMessage(eq((short) 1),
                                                           any(byte[].class),
                                                           eq((short) 2048),
                                                           eq((short) 0));
        verify(logger).log(Level.WARNING,
                           "Another ECU is using this address: 181234A5 [8] 77 88 99 AA BB CC DD EE",
                           (Throwable) null);

        verify(eventBus).publish(new ResultEvent("INVALID: " + uiMsg));
        verify(eventBus).publish(any(UrgentEvent.class));
        verify(eventBus).publish(new CompleteEvent(ABORTED));
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
    public void testSend() throws Exception {
        Packet packet = Packet.create(0x1234, 0x56, true, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE);
        byte[] encodedPacket = new byte[] { (byte) 0x34, (byte) 0x12, (byte) 0x00, (byte) 0x06, (byte) 0x56,
                (byte) 0x34, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD,
                (byte) 0xEE };
        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) 0);
        when(exec.submit(submitCaptor.capture())).thenReturn(future);
        // implement echo
        when(queue.stream(ArgumentMatchers.anyLong(), ArgumentMatchers.any())).thenReturn(Stream.of(packet));

        startInstance();
        instance.send(packet);

        Callable<Short> callable = submitCaptor.getValue();
        assertEquals((int) callable.call(), 0);

        verify(exec).submit(any(Callable.class));
        verify(rp1210Library).RP1210_SendMessage(eq((short) 1),
                                                 aryEq(encodedPacket),
                                                 eq((short) 14),
                                                 eq((short) 0),
                                                 eq((short) 0));
        verify(queue).stream(anyLong(), any());
    }

    @Test
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

        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);
        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) -99);
        when(exec.submit(submitCaptor.capture())).thenReturn(future);

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
            assertEquals("Error (99): Testing Failure", e.getMessage());
        }
        Callable<Short> callable = submitCaptor.getValue();
        assertEquals(-99, (long) callable.call());

        verify(exec).submit(any(Callable.class));
        verify(rp1210Library).RP1210_SendMessage(eq((short) 1),
                                                 aryEq(encodedPacket),
                                                 eq((short) 14),
                                                 eq((short) 0),
                                                 eq((short) 0));
        verify(rp1210Library).RP1210_GetErrorMsg(eq((short) 99), any());
        verify(queue).stream(anyLong(), any());
    }

    @Test
    public void testStop() throws Exception {
        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        Future<Short> future = mock(Future.class);
        when(future.get()).thenReturn((short) -99);
        when(exec.submit(submitCaptor.capture())).thenReturn(future);

        startInstance();
        instance.stop();

        Callable<Short> callable = submitCaptor.getValue();
        callable.call();

        verify(rp1210Library).RP1210_ClientDisconnect((short) 1);
        verify(exec).submit(any(Callable.class));
        verify(exec).shutdown();

    }

    @Test
    public void testStopFails() throws Exception {

        ArgumentCaptor<Callable<Short>> submitCaptor = ArgumentCaptor.forClass(Callable.class);

        RejectedExecutionException expectedCause = new RejectedExecutionException();
        when(exec.submit(submitCaptor.capture())).thenThrow(expectedCause);

        startInstance();
        try {
            instance.stop();
            fail("An exception should have been thrown");
        } catch (BusException e) {
            assertEquals("Failed to stop RP1210.", e.getMessage());
            assertEquals(expectedCause, e.getCause());
        }

        verify(exec).submit(any(Callable.class));
        verify(exec).shutdown();
    }

}
