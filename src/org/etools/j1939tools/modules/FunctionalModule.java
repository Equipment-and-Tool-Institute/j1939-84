/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.modules;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.GenericPacket;

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

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Not a concern in desktop app.")
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
            return getJ1939().requestGlobal(dmName, clazz, listener);
        } else {
            return getJ1939().requestDS(dmName, clazz, address, listener).requestResult();
        }
    }

}
