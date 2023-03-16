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
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableA1ValidatorDuplicateSPNsTest {

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
    public void testReportDuplicateSPNsFor2012GAS() {
        executeTest(FuelType.GAS, 2012);

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportDuplicateSPNsFor2012DSL() {
        executeTest(FuelType.DSL, 2012);

        assertEquals(List.of(), listener.getOutcomes());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportDuplicateSPNsFor2013DSL() {
        executeTest(FuelType.DSL, 2013);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 94 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 157 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 164 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3226 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3251 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3609 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3610 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3700 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3719 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5313 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5314 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5454 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5578 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5827 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2013GAS() {
        executeTest(FuelType.GAS, 2013);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 51 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3217 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3227 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3241 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3245 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3249 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3464 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4236 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4237 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4240 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2016DSL() {
        executeTest(FuelType.DSL, 2016);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 38 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 94 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 96 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 157 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 164 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3031 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3226 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3251 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3515 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3516 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3518 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3609 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3610 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3700 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3719 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5313 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5314 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5454 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5466 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5578 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5827 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6895 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7333 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7346 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2022DSL() {
        executeTest(FuelType.DSL, 2022);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 38 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 94 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 96 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 157 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 164 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 166 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3031 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3226 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3251 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3515 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3516 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3518 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3609 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3610 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3700 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3719 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5313 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5314 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5454 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5466 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5578 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5827 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6895 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7333 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7346 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12750 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12751 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2023BATT() {
        executeTest(FuelType.BATT_ELEC, 2023);

        System.out.println(listener.printOutcomes());
        //@formatter:off

        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2024BATT() {
        executeTest(FuelType.BATT_ELEC, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5919 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5920 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7315 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 8086 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2024DSL() {
        executeTest(FuelType.DSL, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 38 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 74 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 94 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 96 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 101 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 157 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 164 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 166 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 245 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 917 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2630 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2659 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3031 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3220 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3226 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3230 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3251 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3479 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3480 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3481 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3515 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3516 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3518 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3609 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3610 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3700 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3719 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4331 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4334 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4348 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4360 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4363 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5313 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5314 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5444 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5454 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5466 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5503 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5578 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5827 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6593 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6595 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6894 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6895 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7333 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7346 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12743 provided by more than one ECU");
        // initially a typo
        // verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12744 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12749 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12750 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12751 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12752 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12753 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12758 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2024GAS() {
        executeTest(FuelType.GAS, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 38 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 51 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 96 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 166 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 245 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 917 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2659 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3217 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3227 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3241 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3245 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3249 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3464 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4236 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4237 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4240 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6894 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6895 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7333 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12744 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2024HYB_DSL() {
        executeTest(FuelType.HYB_DSL, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 38 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 74 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 94 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 96 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 101 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 157 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 164 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 166 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 245 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 917 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2630 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2659 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3031 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3220 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3226 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3230 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3251 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3479 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3480 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3481 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3515 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3516 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3518 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3609 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3610 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3700 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3719 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4331 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4334 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4348 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4360 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4363 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5313 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5314 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5444 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5454 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5466 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5503 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5578 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5827 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5919 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5920 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6593 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6595 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6894 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6895 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7315 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7333 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7346 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7896 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 8086 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12743 provided by more than one ECU");
        // initially typo
        // verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12744 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12749 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12750 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12751 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12752 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12753 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12758 provided by more than one ECU");
        //@formatter:on
    }

    @Test
    public void testReportDuplicateSPNsFor2024HYB_GAS() {
        executeTest(FuelType.HYB_GAS, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 27 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 38 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 51 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 84 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 91 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 92 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 96 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.5 SPN 102 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 106 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 108 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 110 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 132 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 166 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 175 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 183 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 190 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 235 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 245 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 247 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 248 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 512 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 513 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 544 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 723 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.5 SPN 917 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1127 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1413 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1433 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1436 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1600 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 1637 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2659 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 2791 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3217 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3227 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3241 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3245 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3249 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3464 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 3563 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4076 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4193 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4201 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4202 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4236 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4237 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 4240 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5829 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5837 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5919 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 5920 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6393 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6894 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 6895 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7315 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7333 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 7896 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 8086 provided by more than one ECU");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.5 SPN 12744 provided by more than one ECU");
        //@formatter:on
    }

    private void executeTest(FuelType fuelType, int engineModelYear) {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        var vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(engineModelYear);
        vehicleInfo.setFuelType(fuelType);
        dataRepository.setVehicleInformation(vehicleInfo);

        var packets = TableA1ValidatorTest.createPackets(0);
        packets.addAll(TableA1ValidatorTest.createPackets(3));
        instance.reportDuplicateSPNs(packets, listener, "S");
    }

}
