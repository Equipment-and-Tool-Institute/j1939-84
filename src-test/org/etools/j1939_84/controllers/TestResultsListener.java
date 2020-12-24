/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;

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

    private final List<String> milestones = new ArrayList<>();

    private final ResultsListener mockListener;

    private final List<String> results = new ArrayList<>();

    private boolean success;

    public TestResultsListener() {
        this(null);
    }

    public TestResultsListener(ResultsListener mockListener) {
        this.mockListener = mockListener;
    }

    @Override
    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
        mockListener.addOutcome(partNumber, stepNumber, outcome, message);
    }

    @Override
    public void beginPart(PartResult partResult) {
        milestones.add("Begin Part: " + partResult);
    }

    @Override
    public void beginStep(StepResult stepResult) {
        milestones.add("Begin Step: " + stepResult);
    }

    @Override
    public void endPart(PartResult partResult) {
        milestones.add("End Part: " + partResult);
    }

    @Override
    public void endStep(StepResult stepResult) {
        milestones.add("End Step: " + stepResult);
    }

    public String getMessages() {
        return String.join(NL, messages);
    }

    public String getMilestones() {
        return String.join(NL, milestones);
    }

    public String getResults() {
        StringBuilder sb = new StringBuilder();
        results.forEach(t -> sb.append(t).append(NL));
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
        // Capture that the user was displayed
        mockListener.onMessage(message, title, type);
    }

    @Override
    public void onProgress(int currentStep, int totalSteps, String message) {
        // FIXME This needs put back after Stepping if fixed.
        // if (currentStep < lastStep) {
        // fail("Steps went backwards");
        // } else if (currentStep != lastStep + 1) {
        // fail("Steps skipped from " + lastStep + " to " + currentStep);
        // } else if (currentStep > totalSteps) {
        // fail("Steps exceed maximum");
        // }

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
        mockListener.onUrgentMessage(message, title, type);
    }

    @Override
    public void onUrgentMessage(String message, String title, MessageType type, QuestionListener listener) {
        mockListener.onUrgentMessage(message, title, type, listener);
    }

    @Override
    public void onVehicleInformationNeeded(VehicleInformationListener listener) {
        mockListener.onVehicleInformationNeeded(listener);
    }

    @Override
    public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
        mockListener.onVehicleInformationReceived(vehicleInformation);

    }

}
