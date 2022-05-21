/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.model.FuelType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


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

    private String vin = "";

    public RequestResult<AddressClaimPacket> getAddressClaim() {
        if (addressClaim == null) {
            addressClaim = new RequestResult<>(false, Collections.emptyList());
        }
        return addressClaim;
    }

    public void setAddressClaim(RequestResult<AddressClaimPacket> addressClaim) {
        this.addressClaim = addressClaim;
    }

    public int getCalIds() {
        return calIds;
    }

    public void setCalIds(int calIds) {
        this.calIds = calIds;
    }

    public List<DM19CalibrationInformationPacket> getCalIdsFound() {
        return calIdsFound;
    }

    public void setCalIdsFound(List<DM19CalibrationInformationPacket> calIdsFound) {
        this.calIdsFound = calIdsFound;
    }

    public String getCertificationIntent() {
        return certificationIntent;
    }

    public void setCertificationIntent(String certificationIntent) {
        this.certificationIntent = certificationIntent;
    }

    public int getEmissionUnits() {
        return emissionUnits;
    }

    public void setEmissionUnits(int emissionUnits) {
        this.emissionUnits = emissionUnits;
    }

    public List<ComponentIdentificationPacket> getEmissionUnitsFound() {
        return emissionUnitsFound;
    }

    public void setEmissionUnitsFound(List<ComponentIdentificationPacket> emissionUnitsFound) {
        this.emissionUnitsFound = emissionUnitsFound;
    }

    public int getEngineModelYear() {
        return engineModelYear;
    }

    public void setEngineModelYear(int engineModelYear) {
        this.engineModelYear = engineModelYear;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public int getNumberOfTripsForFaultBImplant() {
        return numberOfTripsForFaultBImplant;
    }

    public void setNumberOfTripsForFaultBImplant(int numberOfTripsForFaultBImplant) {
        this.numberOfTripsForFaultBImplant = numberOfTripsForFaultBImplant;
    }

    public int getVehicleModelYear() {
        return vehicleModelYear;
    }

    public void setVehicleModelYear(int vehicleModelYear) {
        this.vehicleModelYear = vehicleModelYear;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        if (vin == null) {
            vin = "";
        }
        this.vin = vin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calIds,
                            calIdsFound,
                            certificationIntent,
                            emissionUnits,
                            emissionUnitsFound,
                            engineModelYear,
                            fuelType,
                            vehicleModelYear,
                            vin,
                            numberOfTripsForFaultBImplant);
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

    @SuppressFBWarnings(value = "CN_IDIOM_NO_SUPER_CALL", justification = "Calling super.clone() will cause a crash")
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
    public String toString() {
        return "User Data Entry: " + NL + NL
                + "Engine Model Emissions Year: " + engineModelYear + NL
                + "Number of Emissions ECUs Expected: " + emissionUnits + NL
                + "Number of CAL IDs Expected: " + calIds + NL
                + "Fuel Type: " + fuelType + NL
                + "Ignition Type: " + fuelType.ignitionType.name + NL
                + "Number of Trips for Fault B Implant: " + numberOfTripsForFaultBImplant + NL
                + NL
                + "Vehicle Information:" + NL
                + "VIN: " + vin + NL
                + "Vehicle MY: " + vehicleModelYear + NL
                + "Engine MY: " + engineModelYear + NL
                + "Cert. Engine Family: " + certificationIntent + NL
                + "Number of OBD ECUs Found: " + emissionUnitsFound.size() + NL
                + emissionUnitsFound.stream()
                                    .map(m -> "     Make: " + m.getMake() + ", Model: " + m.getModel() + ", Serial: "
                                            + m.getSerialNumber())
                                    .collect(Collectors.joining(NL))
                + NL
                + "Number of CAL IDs Found: "
                + calIdsFound.stream().mapToLong(dm19 -> dm19.getCalibrationInformation().size()).sum() + NL
                + calIdsFound.stream()
                             .map(DM19CalibrationInformationPacket::toString)
                             .flatMap(String::lines)
                             .map(s -> "     " + s)
                             .collect(Collectors.joining(NL))
                + NL;
    }

}
