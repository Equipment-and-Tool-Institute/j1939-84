/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;

/**
 * Helper class used as a {@link ResultsListener} for testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TestResultsListener implements ResultsListener {

    private boolean complete = false;

    private int lastStep = 0;

    private final List<String> messages = new ArrayList<>();

    private final List<String> results = new ArrayList<>();

    private boolean success;

    @Override
    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beginPart(PartResult partResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beginStep(StepResult stepResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void endPart(PartResult partResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void endStep(StepResult stepResult) {
        // TODO Auto-generated method stub

    }

    public String getMessages() {
        return messages.stream().collect(Collectors.joining(NL));
    }

    public String getResults() {
        StringBuilder sb = new StringBuilder();
        results.stream().forEachOrdered(t -> sb.append(t).append(NL));
        return sb.toString();
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isSuccess() {
        if (!complete) {
            throw new IllegalStateException("Complete was not received yet");
        }
        return success;
    }

    @Override
    public void onComplete(boolean success) {
        complete = true;
        this.success = success;
    }

    @Override
    public void onMessage(String message, String title, MessageType type) {
        fail("Method not implemented");
    }

    @Override
    public void onProgress(int currentStep, int totalSteps, String message) {
        if (currentStep < lastStep) {
            fail("Steps went backwards");
        } else if (currentStep != lastStep + 1) {
            fail("Steps skipped");
        } else if (currentStep > totalSteps) {
            fail("Steps exceed maximum");
        }

        lastStep = currentStep;
        messages.add(message);
    }

    @Override
    public void onProgress(String message) {
        messages.add(message);
    }

    @Override
    public void onResult(List<String> results) {
        this.results.addAll(results);
    }

    @Override
    public void onResult(String result) {
        results.add(result);
    }

    @Override
    public void onUrgentMessage(String message, String title, MessageType type) {
        fail("Method not implemented");
    }

    @Override
    public void onVehicleInformationNeeded(VehicleInformationListener listener) {
        fail("Method not implemented");
    }

    @Override
    public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
        // TODO Auto-generated method stub

    }

}
