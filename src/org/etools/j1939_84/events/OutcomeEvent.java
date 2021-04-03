/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import java.util.Objects;

import org.etools.j1939_84.model.Outcome;

public class OutcomeEvent implements Event {

    private final int partNumber;
    private final int stepNumber;
    private final Outcome outcome;
    private final String message;

    public OutcomeEvent(int partNumber, int stepNumber, Outcome outcome, String message) {
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
        this.outcome = outcome;
        this.message = message;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutcomeEvent that = (OutcomeEvent) o;
        return getPartNumber() == that.getPartNumber()
                && getStepNumber() == that.getStepNumber()
                && getOutcome() == that.getOutcome()
                && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPartNumber(), getStepNumber(), getOutcome(), getMessage());
    }
}
