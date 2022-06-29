/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part02Step07Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part02Step07ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 7;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part02Step07Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals(PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testHappyPathNoFailures() {
        var module0 = new OBDModuleInformation(0, 0);
        module0.set(ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0"), 1);
        dataRepository.putObdModule(module0);
        var packet0 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet0));

        var module1 = new OBDModuleInformation(1);
        module1.set(ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1"), 1);
        dataRepository.putObdModule(module1);
        var packet1 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet1));

        var packet00 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        var packet11 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet00,
                                                                                                               packet11));
        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForNoSupportDS() {
        var module0 = new OBDModuleInformation(0, 0);
        module0.set(ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0"), 1);
        dataRepository.putObdModule(module0);
        var packet0 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet0));

        var module1 = new OBDModuleInformation(1);
        module1.set(ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1"), 1);
        dataRepository.putObdModule(module1);
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class))).thenReturn(BusResult.empty());

        var packet00 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        var packet11 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet00,
                                                                                                         packet11));
        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.a - Engine #2 (1) did not support PGN 65259 with the engine running");
    }

    @Test
    public void testFailureForDifferencePart1Part2() {
        var module0 = new OBDModuleInformation(0, 0);
        module0.set(ComponentIdentificationPacket.create(0, "make", "model", "serialNumber", "unit"), 1);
        dataRepository.putObdModule(module0);
        var packet0 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet0));

        var module1 = new OBDModuleInformation(1);
        module1.set(ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1"), 1);
        dataRepository.putObdModule(module1);
        var packet1 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet1));

        var packet00 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        var packet11 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet00,
                                                                                                 packet11));
        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.b - Engine #1 (0) reported difference between the part2 response and the part 1 response");
    }

    @Test
    public void testFailureForNoFunction0Response() {
        var module0 = new OBDModuleInformation(0, 0);
        module0.set(ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0"), 1);
        dataRepository.putObdModule(module0);
        var packet0 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet0));

        var module1 = new OBDModuleInformation(1);
        module1.set(ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1"), 1);
        dataRepository.putObdModule(module1);
        var packet1 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(1),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet1));

        var packet11 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet11));
        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.4.a - There is no positive response from Engine #1 (0)");
    }

    @Test
    public void testFailureForNoFunction0ResponseWithoutFunction0Module() {
        var module0 = new OBDModuleInformation(0);
        module0.set(ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0"), 1);
        dataRepository.putObdModule(module0);
        var packet0 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet0));

        var module1 = new OBDModuleInformation(1);
        module1.set(ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1"), 1);
        dataRepository.putObdModule(module1);
        var packet1 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet1));

        var packet00 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        var packet11 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet00, packet11));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.4.a - There is no positive response from Unknown (-1)");
    }

    @Test
    public void testFailureForDifferenceDsAndGlobal() {
        var module0 = new OBDModuleInformation(0, 0);
        module0.set(ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0"), 1);
        dataRepository.putObdModule(module0);
        var packet0 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet0));// when(communicationsModule.requestComponentIdentification(any(),
                                                                                                                        // eq(0))).thenReturn(BusResult.of(packet0));

        var module1 = new OBDModuleInformation(1);
        module1.set(ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1"), 1);
        dataRepository.putObdModule(module1);
        var packet1 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet1));

        var packet00 = ComponentIdentificationPacket.create(0, "make", "model", "serialNumber", "unit");
        var packet11 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet00, packet11));
        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.4.b - Global response does not match the destination specific response from Engine #1 (0)");
    }

    @Test
    public void testWarningForNoSupport() {
        var module0 = new OBDModuleInformation(0, 0);
        module0.set(ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0"), 1);
        dataRepository.putObdModule(module0);
        var packet0 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet0));

        var module1 = new OBDModuleInformation(1);
        module1.set(ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1"), 1);
        dataRepository.putObdModule(module1);
        var packet1 = ComponentIdentificationPacket.create(1, "make1", "model1", "serialNumber1", "unit1");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet1));

        var packet00 = ComponentIdentificationPacket.create(0, "make0", "model0", "serialNumber0", "unit0");
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet00));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.2.7.5.a - Engine #2 (1) did not support PGN 65259 with the engine running");
    }
}
