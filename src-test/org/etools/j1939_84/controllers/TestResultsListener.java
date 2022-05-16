/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;

/**
 * Helper class used as a {@link ResultsListener} for testing
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class TestResultsListener implements ResultsListener {

    private final List<String> messages = new ArrayList<>();
    private final ResultsListener mockListener;
    private final List<String> results = new ArrayList<>();
    private final List<ActionOutcome> outcomes = new ArrayList<>();
    private boolean complete = false;
    private boolean success;

    public TestResultsListener() {
        this(null);
    }

    public TestResultsListener(ResultsListener mockListener) {
        this.mockListener = mockListener;
    }

    @Override
    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
        outcomes.add(new ActionOutcome(outcome, message));
        mockListener.addOutcome(partNumber, stepNumber, outcome, message);
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

    public String getMessages() {
        return String.join(NL, messages);
    }

    public String getResults() {
        StringBuilder sb = new StringBuilder();
        results.forEach(t -> {
            sb.append(t).append(NL);
        });
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

    public List<ActionOutcome> getOutcomes() {
        return outcomes;
    }

    public String printOutcomes() {
        StringBuilder sb = new StringBuilder();
        for (ActionOutcome outcome : outcomes) {
            sb.append("verify(mockListener).addOutcome(1, 26, ")
              .append(outcome.getOutcome())
              .append(", \"")
              .append(outcome.getMessage())
              .append("\");")
              .append(NL);
        }
        return sb.toString();
    }

}
