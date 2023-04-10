/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */

package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939tools.j1939.model.FuelType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SupportedSpnModuleTest {
    @Mock
    private ResultsListener mockListener;

    private TestResultsListener listener;

    private SupportedSpnModule instance;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);

        instance = new SupportedSpnModule();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockListener);
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2013CIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.DSL, 2013));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3226" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3700" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5466" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3609, 3610, 3251" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5827, 5454" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3516, 3518, 7346" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 164, 5313, 5314, 5578" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2013CIPassing() {
        List<Integer> spns = List.of(1413,
                                     512,
                                     102,
                                     541,
                                     2791,
                                     5829,
                                     540,
                                     543,
                                     235,
                                     5837,
                                     3609,
                                     84,
                                     5466,
                                     248,
                                     3226,
                                     5827,
                                     158,
                                     190,
                                     27,
                                     542,
                                     108,
                                     3700,
                                     94,
                                     3516,
                                     247,
                                     91,
                                     544,
                                     110,
                                     183,
                                     513,
                                     539,
                                     92);
        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.DSL, 2013));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2016CIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.DSL, 2016));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 514" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2978" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3226" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3700" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5466" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3031, 3515" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6895" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 7333" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3609, 3610, 3251" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5827, 5454" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3516, 3518, 7346" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 164, 5313, 5314, 5578" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2016CIPassing() {
        var spns = List.of(27,
                           84,
                           91,
                           92,
                           108,
                           235,
                           247,
                           248,
                           158,
                           512,
                           513,
                           514,
                           539,
                           540,
                           541,
                           542,
                           543,
                           544,
                           183,
                           2791,
                           2978,
                           3226,
                           3700,
                           1413,
                           102,
                           5466,
                           5829,
                           5837,
                           3031,
                           6895,
                           7333,
                           190,
                           110,
                           3609,
                           5827,
                           3516,
                           94);
        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.DSL, 2016));
        assertEquals("", listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2022CIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.DSL, 2022));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 166" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 514" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2978" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3226" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3700" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5466" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3031, 3515" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6895" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 7333" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3609, 3610, 3251" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5827, 5454" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3516, 3518, 7346" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 164, 5313, 5314, 5578" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2022CIPassing() {
        var spns = List.of(27,
                           84,
                           91,
                           92,
                           108,
                           235,
                           247,
                           248,
                           158,
                           166,
                           512,
                           513,
                           514,
                           539,
                           540,
                           541,
                           542,
                           543,
                           544,
                           183,
                           2791,
                           2978,
                           3226,
                           3700,
                           1413,
                           102,
                           5466,
                           5829,
                           5837,
                           3031,
                           6895,
                           7333,
                           190,
                           110,
                           3609,
                           5827,
                           3516,
                           94);
        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.DSL, 2022));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2024CIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.DSL, 2024));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 166" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 514" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2978" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3226" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3700" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5466" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3031, 3515" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6894" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6895" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 7333" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3609, 3610, 3251" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5827, 5454" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 12748" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3516, 3518, 7346" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 164, 5313, 5314, 5578" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2024CIPassing() {
        var spns = List.of(27,
                           84,
                           91,
                           92,
                           108,
                           166,
                           235,
                           247,
                           248,
                           158,
                           512,
                           513,
                           514,
                           539,
                           540,
                           541,
                           542,
                           543,
                           544,
                           183,
                           2791,
                           2978,
                           3226,
                           3700,
                           1413,
                           102,
                           5466,
                           5829,
                           5837,
                           3031,
                           6895,
                           7333,
                           190,
                           110,
                           3609,
                           5827,
                           3516,
                           94,
                           6894,
                           12748);
        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.DSL, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2013SIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.GAS, 2013));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 51" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3217" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3227" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3241" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3249" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3464" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4236" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4237" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4240" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 5313, 5578" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 12744" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2013SIPassing() {
        List<Integer> spns = List.of(27,
                                     51,
                                     84,
                                     91,
                                     92,
                                     108,
                                     235,
                                     247,
                                     248,
                                     158,
                                     512,
                                     513,
                                     539,
                                     540,
                                     541,
                                     542,
                                     543,
                                     544,
                                     183,
                                     2791,
                                     3217,
                                     3227,
                                     3241,
                                     3249,
                                     3464,
                                     4236,
                                     4237,
                                     4240,
                                     1413,
                                     102,
                                     5829,
                                     5837,
                                     190,
                                     110,
                                     94,
                                     12744);
        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.GAS, 2013));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2016SIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.GAS, 2016));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 51" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 514" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2978" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3217" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3227" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3241" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3249" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3464" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4236" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4237" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4240" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6895" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 7333" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 5313, 5578" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 12744" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2016SIPassing() {
        List<Integer> spns = List.of(27,
                                     51,
                                     84,
                                     91,
                                     92,
                                     108,
                                     235,
                                     247,
                                     248,
                                     158,
                                     512,
                                     513,
                                     514,
                                     539,
                                     540,
                                     541,
                                     542,
                                     543,
                                     544,
                                     183,
                                     2791,
                                     2978,
                                     3217,
                                     3227,
                                     3241,
                                     3249,
                                     3464,
                                     4236,
                                     4237,
                                     4240,
                                     1413,
                                     102,
                                     5829,
                                     5837,
                                     6895,
                                     7333,
                                     190,
                                     110,
                                     94,
                                     12744);
        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.GAS, 2016));

        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2022SIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.GAS, 2022));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 51" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 166" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 514" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2978" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3217" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3227" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3241" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3249" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3464" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4236" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4237" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4240" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6895" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 7333" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 5313, 5578" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 12744" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2022SIPassing() {
        List<Integer> spns = List.of(27,
                                     51,
                                     84,
                                     91,
                                     92,
                                     108,
                                     166,
                                     235,
                                     247,
                                     248,
                                     158,
                                     512,
                                     513,
                                     514,
                                     539,
                                     540,
                                     541,
                                     542,
                                     543,
                                     544,
                                     183,
                                     2791,
                                     2978,
                                     3217,
                                     3227,
                                     3241,
                                     3249,
                                     3464,
                                     4236,
                                     4237,
                                     4240,
                                     1413,
                                     102,
                                     5829,
                                     5837,
                                     6894,
                                     6895,
                                     7333,
                                     190,
                                     110,
                                     94,
                                     12744);

        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.GAS, 2022));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testvalidateRequiredDataStreamSpns2024SIFailures() {
        assertFalse(instance.validateRequiredDataStreamSpns(listener, List.of(), FuelType.GAS, 2024));
        String expected = "";
        expected += "Required Data Stream SPNs are not supported. SPNs: 27" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 51" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 84" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 91" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 108" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 166" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 235" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 247" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 248" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 158, 168" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 514" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 539" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 540" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 541" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 542" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 543" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 544" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 183, 1600" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2791" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 2978" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3217" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3227" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3241" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3249" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 3464" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4236" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4237" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 4240" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 1413, 1433, 1436" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 102, 106, 1127, 3563" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5829" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 5837" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6894" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 6895" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 7333" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 94, 157, 5313, 5578" + NL;
        expected += "Required Data Stream SPNs are not supported. SPNs: 12744" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateDesiredDataStreamSpns2024CIPassing() {
        var spns = List.of(190,
                           5827,
                           132,
                           157,
                           5313,
                           175,
                           // 12744,
                           4348,
                           6593,
                           12749,
                           4363,
                           3230,
                           3220,
                           3481,
                           5503,
                           12743,
                           3479,
                           3480,
                           4334,
                           2630,
                           12758,
                           5444,
                           3516,
                           3518,
                           7346,
                           96,
                           166,
                           12750,
                           12751,
                           4360,
                           6595,
                           12752,
                           12753,
                           245,
                           917,
                           2659);
        assertTrue(instance.validateDesiredDataStreamSpns(listener, spns, FuelType.DSL, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateRequiredDataStreamSpns2024SIPassing() {
        List<Integer> spns = List.of(27,
                                     51,
                                     84,
                                     91,
                                     92,
                                     108,
                                     166,
                                     235,
                                     247,
                                     248,
                                     158,
                                     512,
                                     513,
                                     514,
                                     539,
                                     540,
                                     541,
                                     542,
                                     543,
                                     544,
                                     183,
                                     2791,
                                     2978,
                                     3217,
                                     3227,
                                     3241,
                                     3249,
                                     3464,
                                     4236,
                                     4237,
                                     4240,
                                     1413,
                                     102,
                                     5829,
                                     5837,
                                     190,
                                     110,
                                     94,
                                     6894,
                                     6895,
                                     7333,
                                     12744);
        assertTrue(instance.validateRequiredDataStreamSpns(listener, spns, FuelType.GAS, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateFreezeFrameSpnsAllFailures() {
        assertFalse(instance.validateFreezeFrameSpns(listener, List.of()));

        String expected = "";
        expected += "Required Freeze Frame SPNs are not supported. SPNs: 92" + NL;
        expected += "Required Freeze Frame SPNs are not supported. SPNs: 110, 1637, 4076, 4193" + NL;
        expected += "Required Freeze Frame SPNs are not supported. SPNs: 190, 4201, 723, 4202" + NL;
        expected += "Required Freeze Frame SPNs are not supported. SPNs: 512" + NL;
        expected += "Required Freeze Frame SPNs are not supported. SPNs: 513" + NL;
        expected += "Required Freeze Frame SPNs are not supported. SPNs: 3301" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateFreezeFrameSpnsPassing() {
        List<Integer> spns = List.of(92, 110, 190, 512, 513, 3301);
        assertTrue(instance.validateFreezeFrameSpns(listener, spns));
        assertEquals("", listener.getResults());
    }

    @Test
    public void testValidateDesiredfDataStreamSpns2024SIPassing() {
        List<Integer> spns = List.of(190,
                                     5827,
                                     132,
                                     157,
                                     5313,
                                     175,
                                     3516,
                                     3518,
                                     7346,
                                     96,
                                     166,
                                     12750,
                                     12751,
                                     4360,
                                     4331,
                                     6595,
                                     12752,
                                     12753,
                                     245,
                                     917,
                                     2659);
        assertTrue(instance.validateDesiredDataStreamSpns(listener, spns, FuelType.GAS, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateDesiredfDataStreamSpns2024XevPassing() {
        List<Integer> spns = List.of(190,
                                     5827,
                                     132,
                                     157,
                                     5313,
                                     175,
                                     7315,
                                     8086,
                                     5919,
                                     5920,
                                     3516,
                                     3518,
                                     7346,
                                     96,
                                     166,
                                     12750,
                                     12751,
                                     4360,
                                     4331,
                                     6595,
                                     12752,
                                     12753,
                                     245,
                                     917,
                                     2659);
        assertTrue(instance.validateDesiredDataStreamSpns(listener, spns, FuelType.BATT_ELEC, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateDesiredfDataStreamSpns2024HevPassing() {
        List<Integer> spns = List.of(190,
                                     5827,
                                     132,
                                     157,
                                     5313,
                                     175,
                                     7896,
                                     3516,
                                     3518,
                                     7346,
                                     96,
                                     166,
                                     12750,
                                     12751,
                                     4360,
                                     4331,
                                     6595,
                                     12752,
                                     12753,
                                     245,
                                     917,
                                     2659);
        assertTrue(instance.validateDesiredDataStreamSpns(listener, spns, FuelType.HYB_GAS, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateDesiredfDataStreamSpns2024HevFailing() {
        List<Integer> spns = List.of(190,
                                     5827,
                                     132,
                                     157,
                                     5313,
                                     175,
                                     3516,
                                     3518,
                                     7346,
                                     96,
                                     166,
                                     12750,
                                     12751,
                                     4360,
                                     4331,
                                     12752,
                                     12753,
                                     245,
                                     917,
                                     2659,
                                     7896);
        assertTrue(instance.validateDesiredDataStreamSpns(listener, spns, FuelType.HYB_GAS, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateNoticedDataStreamSpns2024SIFailing() {
        List<Integer> spns = List.of(101,
                                     74);
        assertFalse(instance.validateNoticedDataStreamSpns(listener, spns, FuelType.DSL, 2024));
        String expected = "Required Data Stream SPNs are not supported. SPNs: 110" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testValidateNoticedDataStreamSpns2024SIPassing() {
        List<Integer> spns = List.of(110,
                                     101,
                                     74);
        assertTrue(instance.validateNoticedDataStreamSpns(listener, spns, FuelType.DSL, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testIsMoreThanOneSpnReportedInfoPassing() {
        List<Integer> spns = List.of(110,
                                     101,
                                     74);
        assertFalse(instance.isMoreThanOneSpnReportedInfo(listener, spns, FuelType.DSL, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testIsMoreThanOneSpnReportedInfoFailing() {
        List<Integer> spns = List.of(110,
                                     101,
                                     74,
                                     4348,
                                     6593);
        assertTrue(instance.isMoreThanOneSpnReportedInfo(listener, spns, FuelType.DSL, 2024));
        String expected = "More than one of the SPNs are reported as supported. SPNs: 4348, 6593" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testIsMoreThanOneSpnReportedWarningPassing() {
        List<Integer> spns = List.of(132);
        assertFalse(instance.isMoreThanOneSpnReportedInfo(listener, spns, FuelType.DSL, 2024));
        String expected = "";
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testIsMoreThanOneSpnReportedWarningFailing() {
        List<Integer> spns = List.of(132,
                                     6393);
        assertTrue(instance.isMoreThanOneSpnReportedWarning(listener, spns, FuelType.DSL, 2024));
        String expected = "More than one of the SPNs are reported as supported. SPNs: 132, 6393" + NL;
        assertEquals(expected, listener.getResults());
    }
}
