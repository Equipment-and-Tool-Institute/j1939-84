/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.utils.IndexGenerator;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class PartResult implements IResult {

    private final String index;
    private final String name;
    private Outcome outcome;
    private final int partNumber;
    private final Map<Integer, StepResult> stepResults = new HashMap<>();

    public PartResult(int partNumber, String name) {
        this.partNumber = partNumber;
        this.name = name;
        index = IndexGenerator.instance().index();

        for (int i = 1; i < 30; i++) {
            String stepName = Lookup.getStepName(partNumber, i);
            if ("Unknown".equalsIgnoreCase(stepName)) {
                break;
            } else {
                stepResults.put(i, new StepResult(partNumber, i, stepName));
            }
        }

    }

    public void addResult(StepResult stepResult) {
        stepResults.put(stepResult.getStepNumber(), stepResult);
    }

    /**
     * @return the index
     */
    @Override
    public String getIndex() {
        return index;
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
