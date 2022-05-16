/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableA1ValidatorProvidedNotSupportedTest {

    private DataRepository dataRepository;
    private TableA1Validator instance;
    @Mock
    private J1939DaRepository j1939DaRepository;
    @Mock
    private ResultsListener mockListener;
    private TestResultsListener listener;
    @Mock
    private TableA1ValueValidator valueValidator;

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        TableA1Repository tableA1Repository = new TableA1Repository();
        instance = new TableA1Validator(valueValidator, dataRepository, j1939DaRepository, tableA1Repository, 1, 26);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(valueValidator, j1939DaRepository, mockListener);
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsForNonObdModule() {
        var packet = new GenericPacket(Packet.create(0xFEFA, 0x33, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));
        instance.reportProvidedButNotSupportedSPNs(packet, listener, "6.99.99");

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2012DSL() {
        executeTest(FuelType.DSL, 2012);

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2012GAS() {
        executeTest(FuelType.GAS, 2012);

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2013DSL() {
        executeTest(FuelType.DSL, 2013);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3226 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5578 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5313 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5314 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3719 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3700 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3609 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3610 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5454 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5827 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3251 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 157 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 164 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 94 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2016DSL() {
        executeTest(FuelType.DSL, 2016);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3226 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6895 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7333 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5578 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5313 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5314 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3719 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5466 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3700 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3609 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3610 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5454 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5827 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3515 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3516 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3518 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7346 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3251 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3031 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 157 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 164 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 514 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2978 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 94 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 38 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 96 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2022DSL() {
        executeTest(FuelType.DSL, 2022);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3226 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12750 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12751 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6895 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7333 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5578 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5313 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5314 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3719 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5466 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3700 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3609 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3610 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5454 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5827 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3515 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3516 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3518 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7346 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3251 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3031 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 166 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 157 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 164 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 514 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2978 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 94 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 38 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 96 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024DSL() {
        executeTest(FuelType.DSL, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2659 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12758 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3220 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3226 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3230 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4331 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4334 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6595 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4348 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6593 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12750 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12751 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12752 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12753 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12744 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12743 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12749 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 6894 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6895 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7333 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5578 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 5503 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5313 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5314 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4360 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4363 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3719 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5466 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3700 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3609 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3610 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 5444 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5454 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5827 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3515 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3516 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3518 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7346 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3479 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3480 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3481 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3251 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3031 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2630 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 166 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 917 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 157 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 164 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 514 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2978 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 245 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 74 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 94 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 101 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 38 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 96 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2013GAS() {
        executeTest(FuelType.GAS, 2013);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3217 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3227 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3464 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4236 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4237 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4240 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3249 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3245 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3241 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 51 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024GAS() {
        executeTest(FuelType.GAS, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2659 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3217 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3227 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3464 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12744 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 6894 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6895 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7333 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4236 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4237 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4240 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3249 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3245 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3241 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 166 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 917 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 514 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2978 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 245 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 51 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 38 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 96 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2023BATT() {
        executeTest(FuelType.BATT_ELEC, 2023);

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024BATT() {
        executeTest(FuelType.BATT_ELEC, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5919 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5920 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 8086 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7315 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024HYB_DSL() {
        executeTest(FuelType.HYB_DSL, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2659 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12758 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3220 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3226 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3230 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4331 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4334 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6595 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4348 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6593 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5919 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5920 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 8086 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12750 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12751 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12752 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12753 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12744 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12743 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12749 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 6894 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6895 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7333 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7315 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7896 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5578 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 5503 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5313 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5314 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4360 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4363 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3719 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5466 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3700 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3609 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3610 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 5444 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5454 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5827 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3515 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3516 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3518 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7346 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3479 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3480 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3481 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3251 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 3031 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2630 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 166 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 917 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 157 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 164 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 514 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2978 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 245 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 74 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 94 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 101 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 38 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 96 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024HYB_GAS() {
        executeTest(FuelType.HYB_GAS, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 91 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 92 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 190 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 512 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 513 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 132 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2659 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3217 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3227 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3464 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 723 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4201 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6393 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5919 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5920 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 8086 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 12744 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 6894 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 6895 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7333 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7315 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 7896 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1600 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4236 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4237 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 4240 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4076 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 4193 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 27 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3249 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3245 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3241 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5829 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 5837 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 3563 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 2791 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1637 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1413 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1433 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1436 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 1127 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 166 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 917 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 235 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 514 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 2978 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 245 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 539 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 540 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 541 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 542 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 543 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 544 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 247 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 248 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 110 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 175 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 84 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.7 Provided SPN 51 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 183 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 108 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 102 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.7 Provided SPN 106 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 158 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 168 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 38 is not indicated as supported by Engine #1 (0)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.7 Provided SPN 96 is not indicated as supported by Engine #1 (0)");
        //@formatter:on
    }

    private void executeTest(FuelType fuelType, int engineModelYear) {
        int address = 0;
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(address);
        dataRepository.putObdModule(obdModuleInformation);

        var vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(engineModelYear);
        vehicleInfo.setFuelType(fuelType);
        dataRepository.setVehicleInformation(vehicleInfo);

        TableA1ValidatorTest.createPackets(address).forEach(packet -> {
            instance.reportProvidedButNotSupportedSPNs(packet, listener, "S");
        });
    }

}
