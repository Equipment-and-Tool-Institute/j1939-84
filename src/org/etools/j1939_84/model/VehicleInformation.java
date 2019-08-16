/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import java.util.Objects;

/**
 * The Vehicle Information which is required in Part 1.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VehicleInformation {

	private String certificationIntent;
	private int emissionUnits;
	private int engineModelYear;
	private FuelType fuelType;
	private int vehicleModelYear;
	private String vin;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VehicleInformation)) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		VehicleInformation that = (VehicleInformation) obj;

		return Objects.equals(certificationIntent, that.certificationIntent) && emissionUnits == that.emissionUnits
				&& engineModelYear == that.engineModelYear && fuelType == that.fuelType
				&& vehicleModelYear == that.vehicleModelYear && Objects.equals(vin, that.vin);
	}

	public String getCertificationIntent() {
		return certificationIntent;
	}

	public int getEmissionUnits() {
		return emissionUnits;
	}

	public int getEngineModelYear() {
		return engineModelYear;
	}

	public FuelType getFuelType() {
		return fuelType;
	}

	public int getVehicleModelYear() {
		return vehicleModelYear;
	}

	public String getVin() {
		return vin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(certificationIntent, emissionUnits, engineModelYear, fuelType, vehicleModelYear, vin);
	}

	public void setCertificationIntent(String certificationIntent) {
		this.certificationIntent = certificationIntent;
	}

	public void setEmissionUnits(int emissionUnits) {
		this.emissionUnits = emissionUnits;
	}

	public void setEngineModelYear(int engineModelYear) {
		this.engineModelYear = engineModelYear;
	}

	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	public void setVehicleModelYear(int vehicleModelYear) {
		this.vehicleModelYear = vehicleModelYear;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	@Override
	public String toString() {
		String result = "VehicleInformation:\n";
		result += "Certification: " + certificationIntent + "\n";
		result += "Emissions: " + emissionUnits + "\n";
		result += "Engine MY: " + engineModelYear + "\n";
		result += "FuelType: " + fuelType + "\n";
		result += "Vehicle MY: " + vehicleModelYear + "\n";
		result += "VIN: " + vin;
		return result;
	}

}
