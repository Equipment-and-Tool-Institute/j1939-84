/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import org.etools.j1939_84.model.Outcome;

/**
 * @author Marianne Schaefer marianne.m.schaefer@gmail.com
 *
 */
public abstract class Validator {

    /**
     * @param partNumber
     * @param stepNumber
     * @param outcome
     * @param message
     * @param listener
     */
    protected static void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message,
            ResultsListener listener) {
        listener.addOutcome(partNumber, stepNumber, outcome, message);
        listener.onProgress(outcome.toString() + ": " + message);
    }

}
