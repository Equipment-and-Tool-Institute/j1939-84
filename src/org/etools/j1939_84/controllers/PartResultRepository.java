/*
 * Copyright (c) 2020. Electronic Tools Institute
 */

package org.etools.j1939_84.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.StepResult;

public class PartResultRepository {

    private static PartResultRepository instance;
    private final Map<Integer, PartResult> partResultsMap = new HashMap<>();
    private final PartResultFactory partResultFactory;

    private PartResultRepository() {
        this.partResultFactory = new PartResultFactory();
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

    public List<StepResult> getStepResults(int partNumber) {
        return getPartResult(partNumber).getStepResults();
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

    public boolean partHasFailure(int partNumber) {
        return getStepResults(partNumber).stream().anyMatch(s -> s.getOutcome() == Outcome.FAIL);
    }

    public void addPartResult(int partNumber, PartResult partResult) {
        partResultsMap.put(partNumber, partResult);
    }

    public StepResult getStepResult(int partNumber, int stepNumber) {
        return getPartResult(partNumber).getStepResult(stepNumber);
    }

    public void setStepResult(int partNumber, StepResult stepResult) {
        getPartResult(partNumber).addResult(stepResult);
    }

}
