/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.FreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableA2ValueValidatorTest {

    @Mock
    private ResultsListener mockListener;

    private TestResultsListener listener;

    private TableA2ValueValidator instance;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        instance = new TableA2ValueValidator(1, 2);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockListener);
    }

    @Test
    public void reportWarningsNoMessages() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(123, 14, 1, 1);

        List<Spn> spns = List.of(Spn.create(92, 1),
                                 Spn.create(110, 7),
                                 Spn.create(1637, 8),
                                 Spn.create(4076, 109),
                                 Spn.create(4193, 110),
                                 Spn.create(190, 301),
                                 Spn.create(4201, 1024),
                                 Spn.create(723, 327),
                                 Spn.create(4202, 2024),
                                 Spn.create(512, 0),
                                 Spn.create(513, 1),
                                 Spn.create(3301, 1),
                                 Spn.create(514,1));
        FreezeFrame freezeFrame = new FreezeFrame(dtc, spns);
        freezeFrame.setSPNs(spns);

        instance.reportWarnings(freezeFrame, listener, "6.1.2");

        assertEquals("", listener.getResults());
    }

    @Test
    public void reportWarningsAllFailed() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(123, 14, 1, 1);
        List<Spn> spns = List.of(Spn.create(92, 0),
                                 Spn.create(110, 6),
                                 Spn.create(1637, 6),
                                 Spn.create(4076, 111),
                                 Spn.create(4193, 111),
                                 Spn.create(190, 301),
                                 Spn.create(4201, 299),
                                 Spn.create(723, 1),
                                 Spn.create(4202, 0),
                                 Spn.create(512, -1),
                                 Spn.create(513, 0),
                                 Spn.create(3301, 0));
        FreezeFrame freezeFrame = new FreezeFrame(dtc, spns);
        freezeFrame.setSPNs(spns);

        instance.reportWarnings(freezeFrame, listener, "6.1.2");

        String expected = "";
        expected += "WARN: SPN   110, Engine Coolant Temperature: 6.000000 °C is < 7 C or > 110 C" + NL;
        expected += "WARN: SPN  1637, Engine Coolant Temperature (High Resolution): 6.000000 °C is < 7 C or > 110 C" + NL;
        expected += "WARN: SPN  4076, Engine Coolant Temperature 2: 111.000000 °C is < 7 C or > 110 C" + NL;
        expected += "WARN: SPN  4193, Engine Coolant Pump Outlet Temperature: 111.000000 °C is < 7 C or > 110 C" + NL;
        expected += "WARN: SPN  4201, Engine Speed 1: 299.000000 rpm is <= 300 rpm" + NL;
        expected += "WARN: SPN   723, Engine Speed 2: 1.000000 rpm is <= 300 rpm" + NL;
        expected += "WARN: SPN  4202, Engine Speed 3: 0.000000 rpm is <= 300 rpm" + NL;
        expected += "WARN: SPN    92, Engine Percent Load At Current Speed: 0.000000 % is <= 0% with rpm > 300" + NL;
        expected += "WARN: SPN   512, Driver's Demand Engine - Percent Torque: -1.000000 % is < 0% with rpm > 300" + NL;
        expected += "WARN: SPN   513, Actual Engine - Percent Torque: 0.000000 % is <= 0% with rpm > 300" + NL;
        expected += "WARN: SPN  3301, Time Since Engine Start: 0.000000 is = 0 seconds with rpm > 300" + NL;

        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN   110, Engine Coolant Temperature: 6.000000 °C is < 7 C or > 110 C");
        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN  1637, Engine Coolant Temperature (High Resolution): 6.000000 °C is < 7 C or > 110 C");
        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN  4076, Engine Coolant Temperature 2: 111.000000 °C is < 7 C or > 110 C");
        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN  4193, Engine Coolant Pump Outlet Temperature: 111.000000 °C is < 7 C or > 110 C");
        verify(mockListener).addOutcome(1, 2, WARN, "6.1.2 - SPN  4201, Engine Speed 1: 299.000000 rpm is <= 300 rpm");
        verify(mockListener).addOutcome(1, 2, WARN, "6.1.2 - SPN   723, Engine Speed 2: 1.000000 rpm is <= 300 rpm");
        verify(mockListener).addOutcome(1, 2, WARN, "6.1.2 - SPN  4202, Engine Speed 3: 0.000000 rpm is <= 300 rpm");
        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN    92, Engine Percent Load At Current Speed: 0.000000 % is <= 0% with rpm > 300");
        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN   512, Driver's Demand Engine - Percent Torque: -1.000000 % is < 0% with rpm > 300");
        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN   513, Actual Engine - Percent Torque: 0.000000 % is <= 0% with rpm > 300");
        verify(mockListener).addOutcome(1,
                                        2,
                                        WARN,
                                        "6.1.2 - SPN  3301, Time Since Engine Start: 0.000000 is = 0 seconds with rpm > 300");
    }

    @Test
    public void reportWarningsNoEngineSpeed() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(123, 14, 1, 1);

        List<Spn> spns = List.of(Spn.create(92, 0),
                                 Spn.create(110, 10),
                                 Spn.create(1637, 10),
                                 Spn.create(4076, 10),
                                 Spn.create(4193, 10),
                                 Spn.create(190, (0xFFFF * 0.125)),
                                 Spn.create(4201, (0xFF00 * 0.5)),
                                 Spn.create(723, (0xFEFF * 0.5)),
                                 Spn.create(4202, (0xFE00 * 0.5)),
                                 Spn.create(512, -1),
                                 Spn.create(513, 0),
                                 Spn.create(3301, 0));
        FreezeFrame freezeFrame = new FreezeFrame(dtc, spns);
        freezeFrame.setSPNs(spns);

        instance.reportWarnings(freezeFrame, listener, "6.1.2");

        assertEquals("Unable to confirm engine speed" + NL, listener.getResults());
    }

}