/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.List;

import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;

/**
 * The Interface for an listener that is notified when a {@link Controller} has
 * something to report
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public interface ResultsListener {

	/**
	 * The different types of messages which can be displayed to the user
	 */
	public enum MessageType {
		// The values correspond to JOptionPane Types
		ERROR(2), INFO(1), PLAIN(-1), QUESTION(3), WARNING(2);

		private final int value;

		private MessageType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * Called when the {@link Controller} has completed
	 *
	 * @param success
	 *                true if the completion was normal; false if the process was
	 *                Stopped
	 */
	void onComplete(boolean success);

	/**
	 * Called when a dialog needs to be displayed to the user
	 *
	 * @param message
	 *                the message to display
	 * @param title
	 *                the title of the dialog
	 * @param type
	 *                the type of dialog to display (Error, Warning, Info, etc)
	 */
	void onMessage(String message, String title, MessageType type);

	/**
	 * Called when the {@link Controller} has progressed
	 *
	 * @param currentStep
	 *                    the current step in the process
	 * @param totalSteps
	 *                    the total number of steps in the process
	 * @param message
	 *                    the message about the current step
	 */
	void onProgress(int currentStep, int totalSteps, String message);

	/**
	 * Called when the {@link Controller} has progressed but only updates the
	 * message
	 *
	 * @param message
	 *                the message about the current step
	 */
	void onProgress(String message);

	/**
	 * Called when the {@link Controller} has results to report
	 *
	 * @param results
	 *                the results
	 */
	void onResult(List<String> results);

	/**
	 * Called when the {@link Controller} has a result to report
	 *
	 * @param result
	 *               the result
	 */
	void onResult(String result);

	/**
	 * Called when a dialog needs to be displayed to the user that will force
	 * the application to pause until the user responds.
	 *
	 * @param message
	 *                the message to display
	 * @param title
	 *                the title of the dialog
	 * @param type
	 *                the type of dialog to display (Error, Warning, Info, etc)
	 */
	void onUrgentMessage(String message, String title, MessageType type);

	/**
	 * Called to gather {@link VehicleInformation} from the user. This will display
	 * a form used to gather that information
	 *
	 * @param listener the {@link VehicleInformationListener} that will be called
	 *                 once the user has entered the {@link VehicleInformation}
	 */
	void onVehicleInformationNeeded(VehicleInformationListener listener);
}
