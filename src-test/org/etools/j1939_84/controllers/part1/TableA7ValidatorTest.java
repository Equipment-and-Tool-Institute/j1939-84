/**
 *
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.Outcome;
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
        testResults.add(scaledTestResult(3639, 18));
        testResults.add(scaledTestResult(3713, 31));
        testResults.add(scaledTestResult(5319, 7));
        testResults.add(scaledTestResult(5319, 31));
        testResults.add(scaledTestResult(3226, 16));
        testResults.add(scaledTestResult(3226, 20));
        return testResults;
    }

    private static void remove(Collection<ScaledTestResult> testResults, int spn, int fmi) {
        Iterator<ScaledTestResult> iterator = testResults.iterator();
        while (iterator.hasNext()) {
            ScaledTestResult testResult = iterator.next();
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

    private TableA7Validator instance;

    @Mock
    private ResultsListener listener;

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
        verify(listener)
                .addOutcome(1, 12, Outcome.FAIL, "Fuel system pressure control low is missing required Test Result");
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

        Collection<ScaledTestResult> duplicates = instance.hasDuplicates(results);
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
        Collection<ScaledTestResult> duplicates = instance.hasDuplicates(results);
        assertTrue(duplicates.isEmpty());

    }

}
