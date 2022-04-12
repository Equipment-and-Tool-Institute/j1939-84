/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.model;

import java.util.Objects;

import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;

public class ComponentIdentification {

    private final String make;
    private final String model;
    private final String serialNumber;
    private final String unitNumber;

    public ComponentIdentification(ComponentIdentificationPacket packet) {
        this.make = packet.getMake();
        this.model = packet.getModel();
        this.serialNumber = packet.getSerialNumber();
        this.unitNumber = packet.getUnitNumber();
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(make, model, serialNumber, unitNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ComponentIdentification)) {
            return false;
        }
        ComponentIdentification that = (ComponentIdentification) obj;
        return Objects.equals(this.getMake(), that.getMake())
                && Objects.equals(this.getModel(), that.getModel())
                && Objects.equals(this.getSerialNumber(), that.getSerialNumber())
                && Objects.equals(this.getUnitNumber(), that.getUnitNumber());
    }

    @Override
    public String toString() {
        return "ComponentIdentification{" +
                "make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", unitNumber='" + unitNumber + '\'' +
                '}';
    }
}
