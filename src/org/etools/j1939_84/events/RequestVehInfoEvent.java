/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import java.util.Objects;

import org.etools.j1939_84.model.VehicleInformationListener;

public class RequestVehInfoEvent implements Event {

    private final VehicleInformationListener listener;

    public RequestVehInfoEvent(VehicleInformationListener listener) {
        this.listener = listener;
    }

    public VehicleInformationListener getListener() {
        return listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestVehInfoEvent that = (RequestVehInfoEvent) o;
        return Objects.equals(getListener(), that.getListener());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getListener());
    }
}
