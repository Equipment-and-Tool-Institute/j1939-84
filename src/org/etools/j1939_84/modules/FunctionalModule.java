/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.model.RequestResult;

import net.solidDesign.j1939.CommunicationsListener;

/**
 * Super class for all Functional Modules
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public abstract class FunctionalModule {

    private J1939 j1939;

    protected J1939 getJ1939() {
        return j1939;
    }

    public void setJ1939(J1939 j1939) {
        this.j1939 = j1939;
    }

    protected String getDate() {
        return getDateTimeModule().getDate();
    }

    protected String getTime() {
        return getDateTimeModule().getTime();
    }

    protected DateTimeModule getDateTimeModule() {
        return DateTimeModule.getInstance();
    }

    protected <T extends GenericPacket> RequestResult<T> requestDMPackets(String dmName,
                                                                          Class<T> clazz,
                                                                          int address,
                                                                          CommunicationsListener listener) {
        if (address == J1939.GLOBAL_ADDR) {
            String title = "Global " + dmName + " Request";
            return getJ1939().requestGlobal(title, clazz, listener);
        } else {
            String title = "Destination Specific " + dmName + " Request to " + Lookup.getAddressName(address);
            return getJ1939().requestDS(title, clazz, address, listener).requestResult();
        }
    }

}
