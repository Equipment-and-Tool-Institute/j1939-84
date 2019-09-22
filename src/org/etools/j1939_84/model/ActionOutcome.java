/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ActionOutcome {

    private final String message;
    private final Outcome outcome;

    public ActionOutcome(Outcome outcome, String message) {
        this.outcome = outcome;
        this.message = message;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the outcome
     */
    public Outcome getOutcome() {
        return outcome;
    }
}
