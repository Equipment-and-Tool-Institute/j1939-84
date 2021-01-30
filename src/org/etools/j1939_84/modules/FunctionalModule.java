/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * Super class for all Functional Modules
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public abstract class FunctionalModule {

    public static final String TIMEOUT_MESSAGE = "Error: Timeout - No Response.";

    private J1939 j1939;

    /**
     * Constructor
     */
    protected FunctionalModule() {
    }

    protected String getDate() {
        return getDateTimeModule().getDate();
    }

    /**
     * Returns the {@link DateTimeModule}
     *
     * @return {@link DateTimeModule}
     */
    protected DateTimeModule getDateTimeModule() {
        return DateTimeModule.getInstance();
    }

    /**
     * Returns the {@link J1939} used to communicate with vehicle
     *
     * @return {@link J1939}
     */
    protected J1939 getJ1939() {
        return j1939;
    }

    protected <T extends GenericPacket> RequestResult<T> requestDMPackets(String dmName,
                                                                          Class<T> clazz,
                                                                          int address,
                                                                          ResultsListener listener) {
        if (address == J1939.GLOBAL_ADDR) {
            String title = "Global " + dmName + " Request";
            return getJ1939().requestGlobal(title, clazz, listener);
        } else {
            String title = "Destination Specific " + dmName + " Request to " + Lookup.getAddressName(address);
            return getJ1939().requestDS(title, clazz, address, listener).requestResult();
        }
    }

    /**
     * Returns the Time formatted for the reports
     *
     * @return {@link String}
     */
    protected String getTime() {
        return getDateTimeModule().getTime();
    }

    /**
     * Sets the {@link J1939} that is used to communicate with the vehicle
     *
     * @param j1939
     *         the {@link J1939} to set
     */
    public void setJ1939(J1939 j1939) {
        this.j1939 = j1939;
    }
}
