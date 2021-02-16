/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class ActionOutcome {

    private final String message;
    private final Outcome outcome;

    public ActionOutcome(Outcome outcome, String message) {
        this.outcome = outcome;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    @Override
    public String toString() {
        return outcome + ": " + message;
    }
}
