/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ActionOutcome that = (ActionOutcome) o;
        return getMessage().equals(that.getMessage()) && getOutcome() == that.getOutcome();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessage(), getOutcome());
    }
}
