/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TableA7ValidatorTest {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 12;
    private TableA7Validator instance;
    @Mock
    private ResultsListener listener;

    private static Collection<ScaledTestResult> getCompressionIgnitionTestResults() {
        List<ScaledTestResult> testResults = new ArrayList<>();
        testResults.add(scaledTestResult(157, 18));
        testResults.add(scaledTestResult(164, 18));
        testResults.add(scaledTestResult(3055, 18));
        testResults.add(scaledTestResult(157, 16));
        testResults.add(scaledTestResult(164, 16));
        testResults.add(scaledTestResult(3055, 16));
        testResults.add(scaledTestResult(651, 7));
        testResults.add(scaledTestResult(651, 16));
        testResults.add(scaledTestResult(5358, 16));
        testResults.add(scaledTestResult(1413, 16));
        testResults.add(scaledTestResult(651, 7));
        testResults.add(scaledTestResult(651, 18));
        testResults.add(scaledTestResult(5358, 16));
        testResults.add(scaledTestResult(1413, 16));
        testResults.add(scaledTestResult(1323, 31));
        testResults.add(scaledTestResult(1324, 31));
        testResults.add(scaledTestResult(1325, 31));
        testResults.add(scaledTestResult(1326, 31));
        testResults.add(scaledTestResult(3058, 18));
        testResults.add(scaledTestResult(2659, 18));
        testResults.add(scaledTestResult(411, 18));
        testResults.add(scaledTestResult(3058, 16));
        testResults.add(scaledTestResult(2659, 0));
        testResults.add(scaledTestResult(2659, 16));
        testResults.add(scaledTestResult(411, 16));
        testResults.add(scaledTestResult(4752, 1));
        testResults.add(scaledTestResult(4752, 18));
        testResults.add(scaledTestResult(102, 17));
        testResults.add(scaledTestResult(102, 18));
        testResults.add(scaledTestResult(1127, 18));
        testResults.add(scaledTestResult(3563, 18));
        testResults.add(scaledTestResult(4817, 18));
        testResults.add(scaledTestResult(102, 16));
        testResults.add(scaledTestResult(1127, 16));
        testResults.add(scaledTestResult(3563, 16));
        testResults.add(scaledTestResult(4817, 16));
        testResults.add(scaledTestResult(2630, 16));
        testResults.add(scaledTestResult(105, 16));
        testResults.add(scaledTestResult(1636, 16));
        testResults.add(scaledTestResult(5285, 18));
        testResults.add(scaledTestResult(5018, 18));
        testResults.add(scaledTestResult(5298, 18));
        testResults.add(scaledTestResult(5300, 31));
        testResults.add(scaledTestResult(4364, 17));
        testResults.add(scaledTestResult(4364, 18));
        testResults.add(scaledTestResult(4364, 31));
        testResults.add(scaledTestResult(3361, 7));
        testResults.add(scaledTestResult(4331, 15));
        testResults.add(scaledTestResult(4331, 18));
        testResults.add(scaledTestResult(4334, 18));
        testResults.add(scaledTestResult(3251, 2));
        testResults.add(scaledTestResult(3936, 2));
        testResults.add(scaledTestResult(3936, 16));
        testResults.add(scaledTestResult(3936, 18));
        testResults.add(scaledTestResult(3713, 31));
        testResults.add(scaledTestResult(5319, 7));
        testResults.add(scaledTestResult(5319, 31));
        testResults.add(scaledTestResult(3226, 16));
        testResults.add(scaledTestResult(3226, 20));
        return testResults;
    }

    private static Collection<ScaledTestResult> getSparkIgnitionTestResults() {
        List<ScaledTestResult> testResults = new ArrayList<>();
        testResults.add(scaledTestResult(1323, 31));
        testResults.add(scaledTestResult(1324, 31));
        testResults.add(scaledTestResult(1325, 31));
        testResults.add(scaledTestResult(1326, 31));
        testResults.add(scaledTestResult(6652, 18));
        testResults.add(scaledTestResult(3217, 18));
        testResults.add(scaledTestResult(3217, 31));
        testResults.add(scaledTestResult(3227, 1));
        testResults.add(scaledTestResult(3227, 16));
        testResults.add(scaledTestResult(3222, 1));
        testResults.add(scaledTestResult(3222, 2));
        testResults.add(scaledTestResult(3232, 2));
        testResults.add(scaledTestResult(4364, 31));
        testResults.add(scaledTestResult(6575, 2));
        testResults.add(scaledTestResult(7835, 20));
        return testResults;
    }

    @SuppressWarnings("SameParameterValue")
    private static void remove(Collection<ScaledTestResult> testResults, int spn, int fmi) {
        for (ScaledTestResult testResult : testResults) {
            if (testResult.getSpn() == spn && testResult.getFmi() == fmi) {
                testResults.remove(testResult);
                break;
            }
        }
    }

    private static ScaledTestResult scaledTestResult(int spn, int fmi) {
        ScaledTestResult mock = mock(ScaledTestResult.class);
        when(mock.getSpn()).thenReturn(spn);
        when(mock.getFmi()).thenReturn(fmi);
        return mock;
    }

    @Before
    public void setUp() {
        instance = new TableA7Validator();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testCompressionIgnitionMissingFuelSystemPressureControlLow() {
        Collection<ScaledTestResult> testResults = getCompressionIgnitionTestResults();
        remove(testResults, 157, 18);
        remove(testResults, 164, 18);
        remove(testResults, 3055, 18);
        instance.validateForCompressionIgnition(testResults, new TestResultsListener(listener));

        verify(listener).addOutcome(PART_NUMBER,
                                    STEP_NUMBER,
                                    FAIL,
                                    "6.1.12.2.a (A7.2.a) - Fuel system pressure control low is missing required Test Result, 1 of: 157:18, 164:18, 3055:18");
    }

    @Test
    public void testValidateForSparkIgnitionValid() {
        Collection<ScaledTestResult> testResults = getSparkIgnitionTestResults();
        instance.validateForSparkIgnition(testResults, new TestResultsListener(listener));
    }

    @Test
    public void testValidateForSparkIgnitionMisfire() {
        Collection<ScaledTestResult> testResults = getSparkIgnitionTestResults();
        remove(testResults, 1323, 31);
        instance.validateForSparkIgnition(testResults, new TestResultsListener(listener));

        verify(listener).addOutcome(PART_NUMBER,
                                    STEP_NUMBER,
                                    FAIL,
                                    "6.1.12.2.a (A7.2.a) - % of misfire is missing required Test Result, 1 of: 1323:16, 1323:31");
    }

    @Test
    public void testCompressionIgnitionValid() {
        instance.validateForCompressionIgnition(getCompressionIgnitionTestResults(), new TestResultsListener(listener));
        // Nothing (bad) happens
    }

    @Test
    public void testHasDuplicates() {
        Collection<ScaledTestResult> results = new ArrayList<>();
        results.add(scaledTestResult(123, 14));
        results.add(scaledTestResult(345, 18));
        results.add(scaledTestResult(345, 18));

        Collection<ScaledTestResult> duplicates = instance.findDuplicates(results);
        assertEquals(1, duplicates.size());

        ScaledTestResult duplicate = duplicates.iterator().next();
        assertEquals(18, duplicate.getFmi());
        assertEquals(345, duplicate.getSpn());
    }

    @Test
    public void testHasNoDuplicates() {
        Collection<ScaledTestResult> results = new ArrayList<>();
        results.add(scaledTestResult(123, 14));
        results.add(scaledTestResult(124, 14));
        results.add(scaledTestResult(345, 18));
        results.add(scaledTestResult(345, 17));
        results.add(scaledTestResult(678, 1));
        Collection<ScaledTestResult> duplicates = instance.findDuplicates(results);
        assertTrue(duplicates.isEmpty());
    }

}
