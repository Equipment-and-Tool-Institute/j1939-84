/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class PartResult implements IResult {

    private final String name;
    private Outcome outcome;
    private final int partNumber;

    private final Map<Integer, StepResult> stepResults = new HashMap<>();

    public PartResult(int partNumber, String name) {
        StepResultFactory stepResultFactory = new StepResultFactory();
        this.partNumber = partNumber;
        this.name = name;

        for (int i = 1; i < 30; i++) {
            StepResult stepResult = stepResultFactory.create(partNumber, i);
            if (stepResult == null) {
                break;
            } else {
                stepResults.put(i, stepResult);
            }
        }
    }

    public void addResult(StepResult stepResult) {
        stepResults.put(stepResult.getStepNumber(), stepResult);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Outcome getOutcome() {
        if (outcome == null) {
            if (hasOutcome(Outcome.FAIL)) {
                outcome = Outcome.FAIL;
            } else if (hasOutcome(Outcome.WARN)) {
                outcome = Outcome.WARN;
            } else if (hasOutcome(Outcome.INCOMPLETE)) {
                outcome = Outcome.INCOMPLETE;
            } else {
                outcome = Outcome.PASS;
            }
        }
        return outcome;
    }

    /**
     * @return the partNumber
     */
    public int getPartNumber() {
        return partNumber;
    }

    public StepResult getStepResult(int stepNumber) {
        return stepResults.get(stepNumber);
    }

    public List<StepResult> getStepResults() {
        List<Integer> keys = new ArrayList<>(stepResults.keySet());
        Collections.sort(keys);

        List<StepResult> results = new ArrayList<>();
        for (int key : keys) {
            results.add(stepResults.get(key));
        }
        return results;
    }

    /**
     * @return
     */
    private boolean hasOutcome(Outcome expectedOutcome) {
        return stepResults.values().stream().anyMatch(r -> r.getOutcome() == expectedOutcome);
    }

    @Override
    public String toString() {
        return getName();
    }

}
