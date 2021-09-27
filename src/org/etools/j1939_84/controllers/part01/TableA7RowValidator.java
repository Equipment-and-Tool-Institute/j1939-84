/*
 *
 */
package org.etools.j1939_84.controllers.part01;

import java.util.Collection;
import java.util.HashSet;

import net.solidDesign.j1939.packets.ScaledTestResult;
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
        for (ScaledTestResult scaledTestResult : new HashSet<>(scaledTestResults)) {
            for (ExpectedTestResult expectedTestResult : expectedTestResults) {
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
