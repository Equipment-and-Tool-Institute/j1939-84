/**
 *
 */
package org.etools.j1939_84.model;

import java.util.Objects;

import net.solidDesign.j1939.packets.ScaledTestResult;

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

    public int getFmi() {
        return fmi;
    }

    public int getSpn() {
        return spn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSpn(), getFmi());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ExpectedTestResult)) {
            return false;
        }
        ExpectedTestResult that = (ExpectedTestResult) obj;
        return getSpn() == that.getSpn() && getFmi() == that.getFmi();
    }

    public boolean matches(ScaledTestResult scaledTestResult) {
        return getSpn() == scaledTestResult.getSpn() && getFmi() == scaledTestResult.getFmi();
    }
}
