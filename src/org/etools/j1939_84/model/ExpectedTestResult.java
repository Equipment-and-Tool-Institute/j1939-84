/**
 *
 */
package org.etools.j1939_84.model;

import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ExpectedTestResult {

    private final int fmi;
    private final int spn;

    public ExpectedTestResult(int spn, int fmi) {
        this.spn = spn;
        this.fmi = fmi;
    }

    public boolean matches(ScaledTestResult scaledTestResult) {
        return spn == scaledTestResult.getSpn() && fmi == scaledTestResult.getFmi();
    }
}
