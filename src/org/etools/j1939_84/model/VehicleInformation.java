/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;

/**
 * The Vehicle Information which is required in Part 1.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleInformation implements Cloneable {
    private RequestResult<AddressClaimPacket> addressClaim;

    private int calIds;

    private List<DM19CalibrationInformationPacket> calIdsFound = Collections.emptyList();

    private String certificationIntent;

    private int emissionUnits;

    private List<ComponentIdentificationPacket> emissionUnitsFound = Collections.emptyList();

    private int engineModelYear;

    private FuelType fuelType;

    private int numberOfTripsForFaultBImplant;

    private int vehicleModelYear;

    private String vin;

    @Override
    public VehicleInformation clone() {
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setAddressClaim(getAddressClaim());
        vehInfo.setCalIds(getCalIds());
        vehInfo.setCalIdsFound(getCalIdsFound());
        vehInfo.setCertificationIntent(getCertificationIntent());
        vehInfo.setEmissionUnits(getEmissionUnits());
        vehInfo.setEmissionUnitsFound(getEmissionUnitsFound());
        vehInfo.setEngineModelYear(getEngineModelYear());
        vehInfo.setFuelType(getFuelType());
        vehInfo.setNumberOfTripsForFaultBImplant(getNumberOfTripsForFaultBImplant());
        vehInfo.setVehicleModelYear(getVehicleModelYear());
        vehInfo.setVin(getVin());

        return vehInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VehicleInformation)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        VehicleInformation that = (VehicleInformation) obj;

        return Objects.equals(certificationIntent, that.certificationIntent)
                && calIds == that.calIds
                && Objects.equals(calIdsFound, that.calIdsFound)
                && emissionUnits == that.emissionUnits
                && Objects.equals(emissionUnitsFound, that.emissionUnitsFound)
                && engineModelYear == that.engineModelYear && fuelType == that.fuelType
                && vehicleModelYear == that.vehicleModelYear && Objects.equals(vin, that.vin)
                && numberOfTripsForFaultBImplant == that.numberOfTripsForFaultBImplant;
    }

    public RequestResult<AddressClaimPacket> getAddressClaim() {
        return addressClaim;
    }

    public int getCalIds() {
        return calIds;
    }

    public List<DM19CalibrationInformationPacket> getCalIdsFound() {
        return calIdsFound;
    }

    public String getCertificationIntent() {
        return certificationIntent;
    }

    public int getEmissionUnits() {
        return emissionUnits;
    }

    public List<ComponentIdentificationPacket> getEmissionUnitsFound() {
        return emissionUnitsFound;
    }

    public int getEngineModelYear() {
        return engineModelYear;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public int getNumberOfTripsForFaultBImplant() {
        return numberOfTripsForFaultBImplant;
    }

    public int getVehicleModelYear() {
        return vehicleModelYear;
    }

    public String getVin() {
        return vin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calIds, calIdsFound, certificationIntent, emissionUnits, emissionUnitsFound,
                engineModelYear, fuelType, vehicleModelYear, vin, numberOfTripsForFaultBImplant);
    }

    public void setAddressClaim(RequestResult<AddressClaimPacket> addressClaim) {
        this.addressClaim = addressClaim;
    }

    public void setCalIds(int calIds) {
        this.calIds = calIds;
    }

    public void setCalIdsFound(List<DM19CalibrationInformationPacket> calIdsFound) {
        this.calIdsFound = calIdsFound;
    }

    public void setCertificationIntent(String certificationIntent) {
        this.certificationIntent = certificationIntent;
    }

    public void setEmissionUnits(int emissionUnits) {
        this.emissionUnits = emissionUnits;
    }

    public void setEmissionUnitsFound(List<ComponentIdentificationPacket> emissionUnitsFound) {
        this.emissionUnitsFound = emissionUnitsFound;
    }

    public void setEngineModelYear(int engineModelYear) {
        this.engineModelYear = engineModelYear;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public void setNumberOfTripsForFaultBImplant(int numberOfTripsForFaultBImplant) {
        this.numberOfTripsForFaultBImplant = numberOfTripsForFaultBImplant;
    }

    public void setVehicleModelYear(int vehicleModelYear) {
        this.vehicleModelYear = vehicleModelYear;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    @Override
    public String toString() {
        return "User Data Entry: \n\n"
                + "Engine Model Emissions Year: " + engineModelYear + "\n"
                + "Number of Emissions ECUs Expected: " + emissionUnits + "\n"
                + "Number of CAL IDs Expected: " + calIds + "\n"
                + "Fuel Type: " + fuelType + "\n"
                + "Ignition Type: " + fuelType.ignitionType.name + "\n"
                + "Number of Trips for Fault B Implant: " + numberOfTripsForFaultBImplant + "\n\n"

                + "Vehicle Information:\n"
                + "VIN: " + vin + "\n"
                + "Vehicle MY: " + vehicleModelYear + "\n"
                + "Engine MY: " + engineModelYear + "\n"
                + "Cert. Engine Family: " + certificationIntent + "\n"
                + "Number of OBD ECUs Found: " + emissionUnitsFound.size() + "\n"
                + emissionUnitsFound.stream()
                        .map(m -> "     Make: " + m.getMake() + ", Model: " + m.getModel() + ", Serial: "
                                + m.getSerialNumber())
                        .collect(Collectors.joining("\n"))
                + "\n"
                + "Number of CAL IDs Found: " + calIdsFound.size() + "\n"
                + calIdsFound.stream()
                        .map(ci -> ci.toString())
                        .flatMap(s -> s.lines())
                        .map(s -> "     " + s)
                        .collect(Collectors.joining("\n"))
                + "\n";

    }

}
