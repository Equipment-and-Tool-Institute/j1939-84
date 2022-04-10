/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.List;

import javax.swing.JOptionPane;

import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939tools.CommunicationsListener;

/**
 * The Interface for an listener that is notified when a {@link Controller} has
 * something to report
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public interface ResultsListener extends CommunicationsListener {
    ResultsListener NOOP = new ResultsListener() {
    };

    default void addOutcome(int partNumber, int stepNumber, Outcome outcome, String message) {
    }

    /**
     * Called when the {@link Controller} has completed
     *
     * @param success
     *                    true if the completion was normal; false if the process was
     *                    Stopped
     */
    default void onComplete(boolean success) {
    }

    /**
     * Called when a dialog needs to be displayed to the user
     *
     * @param message
     *                    the message to display
     * @param title
     *                    the title of the dialog
     * @param type
     *                    the type of dialog to display (Error, Warning, Info, etc)
     */
    default void onMessage(String message, String title, MessageType type) {
    }

    /**
     * Called when the {@link Controller} has progressed
     *
     * @param currentStep
     *                        the current step in the process
     * @param totalSteps
     *                        the total number of steps in the process
     * @param message
     *                        the message about the current step
     */
    default void onProgress(int currentStep, int totalSteps, String message) {
    }

    /**
     * Called when the {@link Controller} has progressed but only updates the
     * message
     *
     * @param message
     *                    the message about the current step
     */
    default void onProgress(String message) {
    }

    /**
     * Called when the {@link Controller} has results to report
     *
     * @param results
     *                    the results
     */
    default void onResult(List<String> results) {
        results.stream().forEach(result -> {
            onResult(result);
        });
    }

    /**
     * Called when the {@link Controller} has a result to report
     *
     * @param result
     *                   the result
     */
    @Override
    default void onResult(String result) {
    }

    /**
     * Called when a dialog needs to be displayed to the user that will force
     * the application to pause until the user responds.
     *
     * @param message
     *                    the message to display
     * @param title
     *                    the title of the dialog
     * @param type
     *                    the type of dialog to display (Error, Warning, Info, etc)
     */
    default void onUrgentMessage(String message, String title, MessageType type) {
    }

    /**
     * Called to gather {@link QuestionListener} from the user. This will
     * display a form used to gather that information
     *
     * @param message
     *                     the message to display
     * @param title
     *                     the title of the dialog
     * @param type
     *                     the type of dialog to display (Error, Warning, Info, etc)
     * @param listener
     *                     the {@link QuestionListener} that will be called
     *                     once the user has entered the {@link QuestionListener}
     */
    default void onUrgentMessage(String message, String title, MessageType type, QuestionListener listener) {
    }

    /**
     * Called to gather {@link VehicleInformation} from the user. This will
     * display a form used to gather that information
     *
     * @param listener
     *                     the {@link VehicleInformationListener} that will be called
     *                     once the user has entered the {@link VehicleInformation}
     */
    default void onVehicleInformationNeeded(VehicleInformationListener listener) {
    }

    default void onVehicleInformationReceived(VehicleInformation vehicleInformation) {
    }

    /**
     * The different types of messages which can be displayed to the user
     */
    enum MessageType {
        // The values correspond to JOptionPane Types

        ERROR(JOptionPane.OK_CANCEL_OPTION),
        INFO(JOptionPane.INFORMATION_MESSAGE),
        PLAIN(JOptionPane.PLAIN_MESSAGE),
        QUESTION(JOptionPane.YES_NO_OPTION),
        WARNING(JOptionPane.OK_CANCEL_OPTION);

        private final int value;

        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
