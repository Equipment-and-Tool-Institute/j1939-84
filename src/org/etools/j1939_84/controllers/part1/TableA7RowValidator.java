/**
 *
 */
package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.Iterator;

import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.model.ExpectedTestResult;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TableA7RowValidator {

    public boolean isValid(Collection<ScaledTestResult> scaledTestResults,
            Collection<ExpectedTestResult> expectedTestResults,
            int minimumContains) {

        int matches = 0;
        Iterator<ExpectedTestResult> ei = expectedTestResults.iterator();
        while (ei.hasNext()) {
            ExpectedTestResult expectedTestResult = ei.next();
            Iterator<ScaledTestResult> si = scaledTestResults.iterator();
            while (si.hasNext()) {
                ScaledTestResult scaledTestResult = si.next();
                if (expectedTestResult.matches(scaledTestResult)) {
                    if (++matches >= minimumContains) {
                        return true;
                    }
                }
            }
        }
        return matches >= minimumContains;
    }

}
