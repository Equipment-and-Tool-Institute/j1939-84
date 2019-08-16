/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import org.etools.j1939_84.model.FuelType;

/**
 * There contractual interface between the Presenter and the View of the
 * {@link VehicleInformationDialog}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public interface VehicleInformationContract {

	public interface Presenter {

		/**
		 * Called to initialize the presenter. This should be called when the dialog is
		 * created/displayed
		 */
		void initialize();

		/**
		 * Called when the Cancel Button is clicked
		 */
		void onCancelButtonClicked();

		/**
		 * Called when the text in the Certification Intent changes
		 *
		 * @param certification the new value
		 */
		void onCertificationChanged(String certification);

		/**
		 * Called when the dialog has been closed
		 */
		void onDialogClosed();

		/**
		 * Called when the Emissions Unit count changes
		 *
		 * @param count the new value
		 */
		void onEmissionUnitsChanged(int count);

		/**
		 * Called when the Engine Model Year changes
		 *
		 * @param modelYear the new value
		 */
		void onEngineModelYearChanged(int modelYear);

		/**
		 * Called when the Fuel Type changes
		 *
		 * @param fuelType the new value
		 */
		void onFuelTypeChanged(FuelType fuelType);

		/**
		 * Called when the OK button is clicked
		 */
		void onOkButtonClicked();

		/**
		 * Called when the Vehicle Model Year changes
		 *
		 * @param modelYear the new value
		 */
		void onVehicleModelYearChanged(int modelYear);

		/**
		 * Called when the VIN changes
		 *
		 * @param vin the new value
		 */
		void onVinChanged(String vin);

	}

	public interface View {

		/**
		 * Sets the Number of Emissions Units on the Vehicle
		 *
		 * @param count the number of Emissions Units on the Vehicle
		 */
		void setEmissionUnits(int count);

		/**
		 * Sets the Engine Model Year
		 *
		 * @param modelYear the Engine Model year to set
		 */
		void setEngineModelYear(int modelYear);

		/**
		 * Sets the Fuel Type
		 *
		 * @param fuelType the Fuel Type to set
		 */
		void setFuelType(FuelType fuelType);

		/**
		 * Enables and Disables the Ok Button
		 *
		 * @param isEnabled true to enable the button
		 */
		void setOkButtonEnabled(boolean isEnabled);

		/**
		 * Sets the Vehicle Model Year
		 *
		 * @param modelYear the Vehicle Model Year to set
		 */
		void setVehicleModelYear(int modelYear);

		/**
		 * Indicates if the Vehicle Model Year is valid
		 *
		 * @param isValid true indicates the Vehicle Model Year is valid
		 */
		void setVehicleModelYearValid(boolean isValid);

		/**
		 * Sets the VIN
		 *
		 * @param vin the VIN to set
		 */
		void setVin(String vin);

		/**
		 * Indicates if the VIN is valid
		 *
		 * @param isValid true indicate the VIN is valid
		 */
		void setVinValid(boolean isValid);

		/**
		 * Called to show or hide the dialog
		 *
		 * @param isVisible true to show the dialog; false to hide the dialog;
		 */
		void setVisible(boolean isVisible);
	}
}
