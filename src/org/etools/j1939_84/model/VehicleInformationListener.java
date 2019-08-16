/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

/**
 * The Listener which is called once the user has entered
 * {@link VehicleInformation}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public interface VehicleInformationListener {

	/**
	 * Called once the user has entered all the information
	 *
	 * @param vehicleInformation the {@link VehicleInformation} from the user. This
	 *                           will be null if the cancelled/closed the dialog.
	 */
	void onResult(VehicleInformation vehicleInformation);
}
