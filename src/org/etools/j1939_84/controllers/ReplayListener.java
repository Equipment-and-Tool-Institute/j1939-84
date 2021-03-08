/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;

public class ReplayListener implements ResultsListener {

    private final List<String> results = new ArrayList<>();

    @Override
    public void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {

    }

    @Override
    public void beginPart(PartResult partResult) {

    }

    @Override
    public void beginStep(StepResult stepResult) {

    }

    @Override
    public void endPart(PartResult partResult) {

    }

    @Override
    public void endStep(StepResult stepResult) {

    }

    @Override
    public void onComplete(boolean success) {

    }

    @Override
    public void onMessage(String message, String title, MessageType type) {

    }

    @Override
    public void onProgress(int currentStep, int totalSteps, String message) {

    }

    @Override
    public void onProgress(String message) {

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

    }

    @Override
    public void onUrgentMessage(String message, String title, MessageType type, QuestionListener listener) {

    }

    @Override
    public void onVehicleInformationNeeded(VehicleInformationListener listener) {

    }

    @Override
    public void onVehicleInformationReceived(VehicleInformation vehicleInformation) {

    }

    public void reset() {
        results.clear();
    }

    public void replayResults(ResultsListener listener) {
        listener.onResult(results);
    }
}
