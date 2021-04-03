/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import java.util.Objects;

import org.etools.j1939_84.controllers.Controller.Ending;

public class CompleteEvent implements Event {

    private final Ending ending;

    public CompleteEvent(Ending ending) {
        this.ending = ending;
    }

    public Ending getEnding() {
        return ending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompleteEvent that = (CompleteEvent) o;
        return getEnding() == that.getEnding();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnding());
    }
}
