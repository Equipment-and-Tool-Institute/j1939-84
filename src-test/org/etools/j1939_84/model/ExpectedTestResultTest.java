/**
 *
 */
package org.etools.j1939_84.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import net.soliddesign.j1939tools.j1939.packets.ScaledTestResult;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ExpectedTestResultTest {

    private static ScaledTestResult scaledTestResult(int spn, int fmi) {
        ScaledTestResult mock = mock(ScaledTestResult.class);
        when(mock.getSpn()).thenReturn(spn);
        when(mock.getFmi()).thenReturn(fmi);
        return mock;
    }

    @Test
    public void testMatches() {
        ExpectedTestResult instance = new ExpectedTestResult(123, 18);
        ScaledTestResult rightSpnFmi = scaledTestResult(123, 18);
        ScaledTestResult wrongSpn = scaledTestResult(1, 18);
        ScaledTestResult wrongFmi = scaledTestResult(123, 1);

        assertEquals(true, instance.matches(rightSpnFmi));
        assertEquals(false, instance.matches(wrongSpn));
        assertEquals(false, instance.matches(wrongFmi));
    }
}
