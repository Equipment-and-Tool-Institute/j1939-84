/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket.PGN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link SectionA6Validator}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class SectionA6ValidatorTest {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 2;

    private DataRepository dataRepository;

    private SectionA6Validator instance;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private TableA6Validator tableA6Validator;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        dataRepository = DataRepository.newInstance();
        instance = new SectionA6Validator(dataRepository, tableA6Validator, PART_NUMBER, STEP_NUMBER);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockListener, tableA6Validator);
    }

    @Test
    public void testMoreFails() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        var dm5_0 = new DM5DiagnosticReadinessPacket(Packet.create(PGN, 0, 0x11, 0x22, 0x14, 0x88, 0, 0, 0, 0));
        var dm_17 = new DM5DiagnosticReadinessPacket(Packet.create(PGN, 0x17, 1, 0x14, 0x22, 0, 0, 0, 0, 0));
        var dm5_21 = new DM5DiagnosticReadinessPacket(Packet.create(PGN, 0x21, 0x10, 0x23, 0x13, 0, 0, 0, 0, 0));

        instance.verify(listener, "6.1.2.3.a", RequestResult.of(dm5_0, dm_17, dm5_21), false);

        verify(tableA6Validator).verify(eq(listener), any(), eq("6.1.2.3.a (A6.2.c)"), eq(false));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Engine #1 (0) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Engine #1 (0) did not report 0 for reserved bits");

    }

    @Test
    public void testNoObdResponse() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        instance.verify(listener, "6.1.2.3.a", RequestResult.of(), false);

        verify(tableA6Validator).verify(eq(listener), any(), eq("6.1.2.3.a (A6.2.c)"), eq(false));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.a) - OBD ECU Engine #1 (0) did not provide a response to Global query");

    }

    @Test
    public void testSupportedSystem() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        Packet packet0 = Packet.create(PGN, 0, 0x11, 0x22, 0x22, 0xEE, 0, 0, 0, 0);
        var dm5_0 = new DM5DiagnosticReadinessPacket(packet0);

        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        Packet packet17 = Packet.create(PGN, 0x17, 1, 0x14, 0x14, 0xEE, 0, 0, 0, 0);
        var dm5_17 = new DM5DiagnosticReadinessPacket(packet17);

        dataRepository.putObdModule(new OBDModuleInformation(0x21));
        Packet packet21 = Packet.create(PGN, 0x21, 0x10, 0x23, 0x23, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        var dm5_21 = new DM5DiagnosticReadinessPacket(packet21);

        dataRepository.putObdModule(new OBDModuleInformation(0x22));
        Packet packet22 = Packet.create(PGN, 0x22, 0x10, 0x23, 0x23, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        var dm5_22 = new DM5DiagnosticReadinessPacket(packet22);

        instance.verify(listener, "6.1.2.3.a", RequestResult.of(dm5_0, dm5_17, dm5_21, dm5_22), false);

        verify(tableA6Validator).verify(eq(listener), any(), eq("6.1.2.3.a (A6.2.c)"), eq(false));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Engine #1 (0) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Instrument Cluster #1 (23) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Body Controller (33) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Auxiliary Valve Control or Engine Air System Valve Control (34) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Engine #1 (0) did not report 0 for reserved bits");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Instrument Cluster #1 (23) did not report 0 for reserved bits");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Body Controller (33) did not report 0 for reserved bits");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Auxiliary Valve Control or Engine Air System Valve Control (34) did not report 0 for reserved bits");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - A/C system refrigerant is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Boost pressure control sys is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Catalyst is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Cold start aid system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Diesel Particulate Filter is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - EGR/VVT system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Evaporative system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Exhaust Gas Sensor is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Exhaust Gas Sensor heater is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Fuel System is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Heated catalyst is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Misfire is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - NMHC converting catalyst is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - NOx catalyst/adsorber is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Secondary air system is supported by more than one OBD ECU");
    }

    @Test
    public void testVerify() {

        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        Packet packet0 = Packet.create(PGN, 0, 0x11, 0x22, 0x22, 0, 0, 0, 0, 0);
        var dm5_0 = new DM5DiagnosticReadinessPacket(packet0);
        Packet packet17 = Packet.create(PGN, 0x17, 1, 0x14, 0x14, 0x77, 0xFF, 0xE0, 0xFF, 0xE0);
        var dm5_17 = new DM5DiagnosticReadinessPacket(packet17);
        Packet packet21 = Packet.create(PGN, 0x21, 0x10, 0x23, 0x23, 0, 0, 0, 0, 0);
        var dm5_21 = new DM5DiagnosticReadinessPacket(packet21);

        instance.verify(listener, "6.1.2.3.a", RequestResult.of(dm5_0, dm5_17, dm5_21), false);

        verify(tableA6Validator).verify(eq(listener), any(), eq("6.1.2.3.a (A6.2.c)"), eq(false));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Instrument Cluster #1 (23) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Instrument Cluster #1 (23) did not report 0 for reserved bits");
    }

    @Test
    public void testVerifyError() {

        Packet packet0 = Packet.create(PGN, 0, 0x11, 0x22, 0x13, 0x44, 0x55, 0x66, 0x77, 0x88);
        var dm5_0 = new DM5DiagnosticReadinessPacket(packet0);

        Packet packet17 = Packet.create(PGN, 0x17, 1, 0x02, 0x14, 0x04, 0x05, 0x06, 0x07, 0x08);
        var dm5_17 = new DM5DiagnosticReadinessPacket(packet17);

        Packet packet21 = Packet.create(PGN, 0x21, 0x10, 0x20, 0, 0x40, 0x50, 0x60, 0x70, 0x80);
        var dm5_21 = new DM5DiagnosticReadinessPacket(packet21);

        RequestResult<DM5DiagnosticReadinessPacket> response = RequestResult.of(dm5_0, dm5_17, dm5_21);
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        instance.verify(listener, "6.1.2.3.a", response, false);

        verify(tableA6Validator).verify(eq(listener), any(), eq("6.1.2.3.a (A6.2.c)"), eq(false));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Engine #1 (0) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.b) - Body Controller (33) did not report supported and complete for comprehensive components support and status");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.c) - Engine #1 (0) did not 'complete/not supported' for the unsupported monitor Exhaust Gas Sensor");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.c) - Engine #1 (0) did not 'complete/not supported' for the unsupported monitor Heated catalyst");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.c) - Engine #1 (0) did not 'complete/not supported' for the unsupported monitor NOx catalyst/adsorber");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.c) - Instrument Cluster #1 (23) did not 'complete/not supported' for the unsupported monitor Heated catalyst");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.c) - Instrument Cluster #1 (23) did not 'complete/not supported' for the unsupported monitor NOx catalyst/adsorber");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.c) - Body Controller (33) did not 'complete/not supported' for the unsupported monitor Comprehensive component");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.c) - Body Controller (33) did not 'complete/not supported' for the unsupported monitor Exhaust Gas Sensor");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Engine #1 (0) did not report 0 for reserved bits");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a (A6.1.d) - Body Controller (33) did not report 0 for reserved bits");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - A/C system refrigerant is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Boost pressure control sys is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Catalyst is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Diesel Particulate Filter is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Evaporative system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.2.d) - Exhaust Gas Sensor heater is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a (A6.3.a) - Non-OBD ECU Body Controller (33) responded");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "All the monitor status and support bits from Body Controller (33) are not all binary zeros or all binary ones");
    }

}
