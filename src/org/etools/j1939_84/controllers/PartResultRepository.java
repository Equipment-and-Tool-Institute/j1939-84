/*
 * Copyright (c) 2020. Electronic Tools Institute
 */

package org.etools.j1939_84.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.StepResult;

public class PartResultRepository implements ResultsListener {

    private static PartResultRepository instance;
    private final Map<Integer, PartResult> partResultsMap = new HashMap<>();
    private final PartResultFactory partResultFactory;

    private PartResultRepository() {
        partResultFactory = new PartResultFactory();
    }

    public static PartResultRepository getInstance() {
        if (instance == null) {
            instance = new PartResultRepository();
        }
        return instance;
    }

    /** Only used by tests. */
    public static void setInstance(PartResultRepository instance) {
        PartResultRepository.instance = instance == null ? new PartResultRepository() : instance;
    }

    public PartResult getPartResult(int partNumber) {
        PartResult partResult = partResultsMap.get(partNumber);
        if (partResult == null) {
            partResult = partResultFactory.create(partNumber);
            partResultsMap.put(partNumber, partResult);
        }
        return partResult;
    }

    public List<PartResult> getPartResults() {
        List<Integer> keys = new ArrayList<>(partResultsMap.keySet());
        Collections.sort(keys);

        List<PartResult> results = new ArrayList<>();
        for (int key : keys) {
            results.add(partResultsMap.get(key));
        }
        return results;
    }

    public StepResult getStepResult(int partNumber, int stepNumber) {
        return getPartResult(partNumber).getStepResult(stepNumber);
    }

    public void setStepResult(int partNumber, StepResult stepResult) {
        getPartResult(partNumber).addResult(stepResult);
    }

    @Override
    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
        ActionOutcome actionOutcome = new ActionOutcome(outcome, message);
        boolean isAdded = getStepResult(partNumber, stepNumber).addResult(actionOutcome);
        if (isAdded) {
            // Write the value to the logs, so it's intermixed with the packet data
            J1939_84.getLogger().log(Level.INFO, actionOutcome.toString());
        }
    }
}
