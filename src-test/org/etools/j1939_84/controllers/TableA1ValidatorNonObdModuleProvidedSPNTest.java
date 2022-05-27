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
import java.util.stream.Collectors;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class TableA1ValidatorNonObdModuleProvidedSPNTest {

    private DataRepository dataRepository;
    private TableA1Validator instance;
    @Mock
    private J1939DaRepository j1939DaRepository;
    @Mock
    private ResultsListener mockListener;
    private TestResultsListener listener;
    @Mock
    private TableA1ValueValidator valueValidator;

    private static DM24SPNSupportPacket dm24;

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
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3226 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5578 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5313 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5314 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3719 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3700 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3609 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3610 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5454 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5827 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3251 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 157 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 164 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 94 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2016DSL() {
        executeTest(FuelType.DSL, 2016);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3226 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6895 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 7333 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5578 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5313 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5314 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3719 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5466 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3700 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3609 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3610 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5454 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5827 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3515 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3516 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3518 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7346 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3251 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3031 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 157 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 164 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 514 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2978 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 94 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 38 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 96 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2022DSL() {
        executeTest(FuelType.DSL, 2022);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3226 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12750 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12751 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6895 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 7333 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5578 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5313 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5314 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3719 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5466 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3700 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3609 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3610 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5454 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5827 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3515 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3516 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3518 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7346 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3251 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3031 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 166 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 157 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 164 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 514 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2978 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 94 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 38 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 96 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024DSL() {
        executeTest(FuelType.DSL, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2659 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12758 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3220 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3226 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3230 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4331 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4334 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6595 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4348 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6593 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12750 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12751 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12752 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12753 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12744 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12743 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12749 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6894 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6895 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 7333 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5578 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5503 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5313 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5314 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4360 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4363 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3719 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5466 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3700 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3609 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3610 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5444 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5454 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5827 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3515 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3516 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3518 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7346 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3479 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3480 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3481 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3251 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3031 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2630 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 166 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 917 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 157 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 164 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 514 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2978 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 245 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 74 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 94 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 101 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 38 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 96 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2013GAS() {
        executeTest(FuelType.GAS, 2013);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3217 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3227 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3464 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4236 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4237 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4240 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3249 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3245 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3241 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 51 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024GAS() {
        executeTest(FuelType.GAS, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2659 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3217 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3227 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3464 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12744 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6894 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6895 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 7333 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4236 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4237 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4240 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3249 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3245 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3241 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 166 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 917 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 514 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2978 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 245 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 51 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 38 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 96 provided by non-OBD ECU Body Controller (33)");
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
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 5919 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 5920 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 8086 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7315 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024HYB_DSL() {
        executeTest(FuelType.HYB_DSL, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2659 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12758 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3220 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3226 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3230 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4331 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4334 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6595 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4348 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6593 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 5919 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 5920 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 8086 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12750 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12751 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12752 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12753 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12744 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12743 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12749 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6894 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6895 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 7333 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7315 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7896 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5578 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5503 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5313 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5314 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4360 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4363 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3719 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5466 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3700 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3609 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3610 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5444 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5454 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5827 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3515 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3516 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3518 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7346 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3479 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3480 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3481 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3251 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 3031 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2630 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 166 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 917 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 157 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 164 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 514 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2978 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 245 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 74 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 94 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 101 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 38 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 96 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    @Test
    public void testReportProvidedButNotSupportedSPNsFor2024HYB_GAS() {
        executeTest(FuelType.HYB_GAS, 2024);

        System.out.println(listener.printOutcomes());
        //@formatter:off
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 91 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 92 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 190 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 512 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 513 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 132 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2659 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3217 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3227 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3464 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 723 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4201 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4202 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6393 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 5919 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 5920 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 8086 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 12744 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6894 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 6895 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 7333 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7315 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 7896 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1600 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4236 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4237 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4240 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4076 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 4193 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 27 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3249 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3245 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3241 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5829 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 5837 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 3563 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2791 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1637 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1413 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1433 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 1436 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 1127 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 166 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 917 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 235 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 514 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 2978 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 245 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 188 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 539 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 540 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 541 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 542 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 543 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 544 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 247 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 248 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 110 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 175 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 84 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 51 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 183 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 108 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, WARN, "S - N.6 SPN 102 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, FAIL, "S - N.6 SPN 106 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 158 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 168 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 38 provided by non-OBD ECU Body Controller (33)");
        verify(mockListener).addOutcome(1, 26, INFO, "S - N.6 SPN 96 provided by non-OBD ECU Body Controller (33)");
        //@formatter:on
    }

    private void executeTest(FuelType fuelType, int engineModelYear) {
        var obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(getDM24(), 1);
        dataRepository.putObdModule(obdModuleInformation);

        var vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(engineModelYear);
        vehicleInfo.setFuelType(fuelType);
        dataRepository.setVehicleInformation(vehicleInfo);

        TableA1ValidatorTest.createPackets(33).forEach(packet -> {
            instance.reportNonObdModuleProvidedSPNs(packet, listener, "S");
        });
    }

    private static DM24SPNSupportPacket getDM24() {
        if (dm24 == null) {
            var supportedSpns = J1939DaRepository.getInstance()
                                                 .getSpnDefinitions()
                                                 .keySet()
                                                 .stream()
                                                 .map(s -> SupportedSPN.create(s,
                                                                               true,
                                                                               true,
                                                                               true,
                                                                               true,
                                                                               1))
                                                 .collect(Collectors.toList());

            dm24 = DM24SPNSupportPacket.create(0, supportedSpns);
        }
        return dm24;
    }
}
