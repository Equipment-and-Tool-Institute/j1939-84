/*
 * Copyright (c) 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import org.etools.j1939_84.controllers.ResultsListener;


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
     *                               will be null if the cancelled/closed the dialog.
     */
    void onResult(VehicleInformation vehicleInformation);

    ResultsListener getResultsListener();
}
