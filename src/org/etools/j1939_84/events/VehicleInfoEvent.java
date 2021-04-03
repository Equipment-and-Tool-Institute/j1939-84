/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import java.util.Objects;

import org.etools.j1939_84.model.VehicleInformation;

public class VehicleInfoEvent implements Event {

    private final VehicleInformation vehicleInformation;

    public VehicleInfoEvent(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }

    public VehicleInformation getVehicleInformation() {
        return vehicleInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VehicleInfoEvent that = (VehicleInfoEvent) o;
        return Objects.equals(getVehicleInformation(), that.getVehicleInformation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVehicleInformation());
    }
}
