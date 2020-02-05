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

    public StepResult create(int partNumber, int stepNumber) {
        String stepName = Lookup.getStepName(partNumber, stepNumber);
        if (!stepName.equalsIgnoreCase("Unknown")) {
            return new StepResult(partNumber, stepNumber, stepName);
        } else {
            return null;
        }
    }
}
