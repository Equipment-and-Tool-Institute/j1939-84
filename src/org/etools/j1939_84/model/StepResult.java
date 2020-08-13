/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class StepResult implements IResult {

    private final String name;
    private Outcome outcome;
    private final int partNumber;
    private final List<ActionOutcome> results = new ArrayList<>();
    private final int stepNumber;

    public StepResult(int partNumber, int stepNumber, String name) {
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
        this.name = name;
    }

    public void addResult(ActionOutcome actionOutcome) {
        results.add(actionOutcome);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Outcome getOutcome() {
        if (results.isEmpty()) {
            return Outcome.INCOMPLETE;
        }

        if (outcome == null) {
            if (hasOutcome(Outcome.FAIL)) {
                outcome = Outcome.FAIL;
            } else if (hasOutcome(Outcome.WARN)) {
                outcome = Outcome.WARN;
            } else {
                outcome = Outcome.PASS;
            }
        }
        return outcome;
    }

    public List<ActionOutcome> getOutcomes() {
        return results;
    }

    /**
     * @return the stepNumber
     */
    public int getStepNumber() {
        return stepNumber;
    }

    /**
     * @return
     */
    private boolean hasOutcome(Outcome expectedOutcome) {
        return results.stream().anyMatch(r -> r.getOutcome() == expectedOutcome);
    }

    @Override
    public String toString() {
        return "Step " + partNumber + "." + stepNumber + ". " + getName();
    }

}
