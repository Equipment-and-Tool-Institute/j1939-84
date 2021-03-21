/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui;

import java.io.File;
import java.util.List;

import javax.swing.JDialog;

import org.etools.j1939_84.bus.Adapter;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.ReportFileModule;

/**
 * The interfaces for the UserInterface
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public interface UserInterfaceContract {

    /**
     * The interface for a UI Presenter
     *
     */
    interface Presenter {

        /**
         * Disconnects the vehicle communications adapter
         */
        void disconnect();

        /**
         * Returns the Adapters that are available for communicating with the
         * vehicle
         *
         * @return List
         */
        List<Adapter> getAdapters();

        /**
         * Returns a new instance of J1939 for communicating with the vehicle
         *
         * @return {@link J1939}
         */
        J1939 getNewJ1939();

        /**
         * Returns the {@link ReportFileModule} used to verify the report file
         *
         * @return {@link ReportFileModule}
         */
        ReportFileModule getReportFileModule();

        /**
         * Returns the Vehicle Identification Number
         *
         * @return {@link String}
         */
        String getVin();

        /**
         * Called when the user has selected an adapter
         *
         * @param selectedAdapterName
         *                                the name of the selected adapter
         */
        void onAdapterComboBoxItemSelected(String selectedAdapterName);

        /**
         * Called when the use has selected a report file
         *
         * @param file
         *                 the file to use for the report
         */
        void onFileChosen(File file);

        /**
         * Called when the {@link UserInterfaceView} Help Button is clicked
         */
        void onHelpButtonClicked();

        /**
         * Called when the {@link UserInterfaceView} Read Vehicle Information Button
         * is clicked
         */
        void onReadVehicleInfoButtonClicked();

        /**
         * Called when the Select File Button has been clicked
         */
        void onSelectFileButtonClicked();

        /**
         * Called when the Start Button has been clicked
         */
        void onStartButtonClicked();

        /**
         * Called when the {@link UserInterfaceView} Stop Button is clicked
         */
        void onStopButtonClicked();

    }

    /**
     * The interface for the Graphical User Interface
     *
     */
    interface View {

        /**
         * Adds the given result to the report text area
         *
         * @param result
         *                   the result to add
         */
        void appendResults(String result);

        /**
         * Displays a {@link JDialog}
         *
         * @param message
         *                    the text of the dialog
         * @param title
         *                    the title of the dialog
         * @param type
         *                    the type of the dialog
         * @param modal
         *                    true to wait for the user to respond; false to "fire and
         *                    forget"
         */
        void displayDialog(String message, String title, int type, boolean modal);

        void displayDialog(String message, String title, int type, boolean modal, QuestionListener questionListener);

        /**
         * Shows the File Chooser
         */
        void displayFileChooser();

        void displayForm(VehicleInformationListener listener, J1939 j1939);

        /**
         * Enables or disables the Adapter Selector Combo Box
         *
         * @param enabled
         *                    true to enable the control, false to disable the control
         */
        void setAdapterComboBoxEnabled(boolean enabled);

        /**
         * Sets the text in the Engine Calibrations Field
         *
         * @param text
         *                 the text to set
         */
        void setEngineCals(String text);

        /**
         * Sets the text that is displayed on the progress bar
         *
         * @param text
         *                 the text to display
         */
        void setProgressBarText(String text);

        /**
         * Sets the value on the progress bar
         *
         * @param min
         *                  the minimum value of the bar
         * @param max
         *                  the maximum value of the bar
         * @param value
         *                  the value of the bar
         */
        void setProgressBarValue(int min, int max, int value);

        /**
         * Enables or disables the Read Vehicle Information Button
         *
         * @param enabled
         *                    true to enable the control, false to disable the control
         */
        void setReadVehicleInfoButtonEnabled(boolean enabled);

        /**
         * Enables or disables the Select File Button
         *
         * @param enabled
         *                    true to enable the control, false to disable the control
         */
        void setSelectFileButtonEnabled(boolean enabled);

        /**
         * Sets the text on the Select File Button
         *
         * @param text
         *                 the text to set
         */
        void setSelectFileButtonText(String text);

        void setStartButtonEnabled(boolean enabled);

        /**
         * Enables or disables the Stop Button
         *
         * @param enabled
         *                    true to enable the control, false to disable the control
         */
        void setStopButtonEnabled(boolean enabled);

        /**
         * Sets the text on the Vehicle Identification Number Field
         *
         * @param vin
         *                the VIN to set
         */
        void setVin(String vin);

    }
}
