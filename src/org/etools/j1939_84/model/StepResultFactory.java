/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import org.etools.j1939_84.bus.j1939.Lookup;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class StepResultFactory {

    private static String name(int partNumber, int stepNumber) {
        return Lookup.getStepName(partNumber, stepNumber);
    }

    public StepResult create(int partNumber, int stepNumber) {
        return new StepResult(partNumber, stepNumber, name(partNumber, stepNumber));
    }
}
