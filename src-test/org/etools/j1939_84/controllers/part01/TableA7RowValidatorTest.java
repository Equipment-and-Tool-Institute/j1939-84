/**
 *
 */
package org.etools.j1939_84.controllers.part01;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.model.ExpectedTestResult;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TableA7RowValidatorTest {

    private TableA7RowValidator instance;

    private static ExpectedTestResult expectedTestResult(int spn, int fmi) {
        return new ExpectedTestResult(spn, fmi);
    }

    private static ScaledTestResult scaledTestResult(int spn, int fmi) {
        ScaledTestResult mock = mock(ScaledTestResult.class);
        when(mock.getSpn()).thenReturn(spn);
        when(mock.getFmi()).thenReturn(fmi);
        return mock;
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        instance = new TableA7RowValidator();
    }

    @Test
    public void testInvalidEmptyList() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));
        expectedTestResults.add(expectedTestResult(164, 18));
        expectedTestResults.add(expectedTestResult(3055, 18));

        assertEquals(false, instance.isValid(actualTestResults, expectedTestResults, 1));
    }

    @Test
    public void testInvalidEmptyLists() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        assertEquals(false, instance.isValid(actualTestResults, expectedTestResults, 1));
    }

    @Test
    public void testInvalidMin1() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        actualTestResults.add(scaledTestResult(123, 11));

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));
        expectedTestResults.add(expectedTestResult(164, 18));
        expectedTestResults.add(expectedTestResult(3055, 18));

        assertEquals(false, instance.isValid(actualTestResults, expectedTestResults, 1));
    }

    @Test
    public void testInvalidMin2() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        actualTestResults.add(scaledTestResult(123, 11));
        actualTestResults.add(scaledTestResult(157, 18));

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));
        expectedTestResults.add(expectedTestResult(164, 18));
        expectedTestResults.add(expectedTestResult(3055, 18));

        assertEquals(false, instance.isValid(actualTestResults, expectedTestResults, 2));
    }

    @Test
    public void testInValidMin4() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        actualTestResults.add(scaledTestResult(157, 18));
        actualTestResults.add(scaledTestResult(164, 18));
        actualTestResults.add(scaledTestResult(3055, 18));

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));
        expectedTestResults.add(expectedTestResult(164, 18));
        expectedTestResults.add(expectedTestResult(3055, 18));

        assertEquals(false, instance.isValid(actualTestResults, expectedTestResults, 4));
    }

    @Test
    public void testValidEmptyLists() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        assertEquals(true, instance.isValid(actualTestResults, expectedTestResults, 0));
    }

    @Test
    public void testValidMin1() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        actualTestResults.add(scaledTestResult(123, 11));
        actualTestResults.add(scaledTestResult(157, 18));
        actualTestResults.add(scaledTestResult(164, 18));
        actualTestResults.add(scaledTestResult(3055, 18));

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));
        expectedTestResults.add(expectedTestResult(164, 18));
        expectedTestResults.add(expectedTestResult(3055, 18));

        assertEquals(true, instance.isValid(actualTestResults, expectedTestResults, 1));
    }

    @Test
    public void testValidMin1Has1() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        actualTestResults.add(scaledTestResult(157, 18));

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));

        assertEquals(true, instance.isValid(actualTestResults, expectedTestResults, 1));
    }

    @Test
    public void testValidMin2() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        actualTestResults.add(scaledTestResult(123, 11));
        actualTestResults.add(scaledTestResult(157, 18));
        actualTestResults.add(scaledTestResult(164, 18));
        actualTestResults.add(scaledTestResult(3055, 18));

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));
        expectedTestResults.add(expectedTestResult(164, 18));
        expectedTestResults.add(expectedTestResult(3055, 18));

        assertEquals(true, instance.isValid(actualTestResults, expectedTestResults, 2));
    }

    @Test
    public void testValidMin3() {
        Collection<ScaledTestResult> actualTestResults = new ArrayList<>();
        actualTestResults.add(scaledTestResult(123, 11));
        actualTestResults.add(scaledTestResult(157, 18));
        actualTestResults.add(scaledTestResult(164, 18));
        actualTestResults.add(scaledTestResult(3055, 18));

        Collection<ExpectedTestResult> expectedTestResults = new ArrayList<>();
        expectedTestResults.add(expectedTestResult(157, 18));
        expectedTestResults.add(expectedTestResult(164, 18));
        expectedTestResults.add(expectedTestResult(3055, 18));

        assertEquals(true, instance.isValid(actualTestResults, expectedTestResults, 3));
    }

}
