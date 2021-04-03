/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import java.util.Objects;

public class ProgressEvent implements Event {

    private final int currentStep;
    private final int totalSteps;
    private final String message;

    public ProgressEvent(int currentStep, int totalSteps, String message) {
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        this.message = message;
    }

    public ProgressEvent(String message) {
        currentStep = -1;
        totalSteps = -1;
        this.message = message;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return totalSteps;
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
        ProgressEvent that = (ProgressEvent) o;
        return getCurrentStep() == that.getCurrentStep()
                && getTotalSteps() == that.getTotalSteps()
                && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrentStep(), getTotalSteps(), getMessage());
    }
}
