/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static java.util.Map.Entry.comparingByKey;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INCOMPLETE;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.PASS;
import static org.etools.j1939_84.model.Outcome.WARN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class PartResult implements IResult {

    private static final StepResultFactory stepResultFactory = new StepResultFactory();

    private final String name;
    private Outcome outcome;
    private final int partNumber;

    private final Map<Integer, StepResult> stepResults = new HashMap<>();

    public PartResult(int partNumber, String name) {

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
            if (hasOutcome(FAIL)) {
                outcome = FAIL;
            } else if (hasOutcome(WARN)) {
                outcome = WARN;
            } else if (hasOutcome(INFO)) {
                outcome = INFO;
            } else if (hasOutcome(INCOMPLETE)) {
                outcome = INCOMPLETE;
            } else if (hasOutcome(ABORT)) {
                outcome = ABORT;
            } else {
                outcome = PASS;
            }
        }
        return outcome;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public StepResult getStepResult(int stepNumber) {
        return stepResults.get(stepNumber);
    }

    public List<StepResult> getStepResults() {
        return stepResults.entrySet()
                          .stream()
                          .sorted(comparingByKey())
                          .map(Map.Entry::getValue)
                          .collect(Collectors.toList());
    }

    private boolean hasOutcome(Outcome expectedOutcome) {
        return stepResults.values().stream().map(StepResult::getOutcome).anyMatch(o -> o == expectedOutcome);
    }

    @Override
    public String toString() {
        return getName();
    }

}
