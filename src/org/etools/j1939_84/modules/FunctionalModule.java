/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * Super class for all Functional Modules
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public abstract class FunctionalModule {

    public static final String TIMEOUT_MESSAGE = "Error: Timeout - No Response.";

    private J1939 j1939;

    /**
     * Constructor
     *
     * @param dateTimeModule
     *            the {@link DateTimeModule} that generates the date/time
     */
    protected FunctionalModule() {
    }

    /**
     * Helper method to generate a report
     *
     * @param <T>
     *            the class of the Packet that will received
     * @param listener
     *            the {@link ResultsListener} that will be given the results
     * @param title
     *            the Title of the report section
     * @param clazz
     *            the Class of a ParsedPacket that's expected to be returned
     *            from the vehicle
     * @param request
     *            the {@link Packet} that will be sent to solicit responses from
     *            the vehicle modules
     *
     * @return the List of Packets that were received
     */
    protected <T extends ParsedPacket> RequestResult<T> generateReport(
            ResultsListener listener,
            String title,
            Class<T> clazz,
            Packet request) {
        RequestResult<T> results = getJ1939().requestResult(title, listener, clazz, request);
        listener.onResult(results.getEither().stream().map(e -> e.resolve().toString()).collect(Collectors.toList()));
        return results;
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

    protected <T extends ParsedPacket> BusResult<T> getPacketDS(String title,
            int pgn,
            Class<T> clazz,
            ResultsListener listener,
            boolean fullString,
            int address) {
        Packet request = getJ1939().createRequestPacket(pgn, address);

        BusResult<T> result = getJ1939().requestDS(title, listener, clazz, request);

        // FIXME is this right?
        if (fullString) {
            result.getPacket().ifPresent(p -> listener.onResult(p.toString()));
        }
        return result;
    }

    protected Function<ParsedPacket, String> getPacketMapperFunction() {
        return t -> t.getPacket().toTimeString() + NL + t.toString();
    }

    /**
     * Helper method to request packets from the vehicle
     *
     * @param <T>
     *            The class of packets that will be returned
     * @param title
     *            the section title for inclusion in report
     * @param pgn
     *            the PGN that's being requested
     * @param clazz
     *            the {@link Class} of packet that will be returned
     * @param listener
     *            the {@link ResultsListener} that will be notified of the
     *            traffic
     * @param fullString
     *            true to include the full string of the results in the report;
     *            false to only include the returned raw packet in the report
     * @return the List of packets returned
     */
    protected <T extends ParsedPacket> RequestResult<T> getPacketsFromGlobal(String title,
            int pgn,
            Class<T> clazz,
            ResultsListener listener,
            boolean fullString) {
        Packet request = getJ1939().createRequestPacket(pgn, J1939.GLOBAL_ADDR);

        RequestResult<T> result = getJ1939().requestGlobal(title, listener, clazz, request);

        for (Either<T, AcknowledgmentPacket> packet : result.getEither()) {
            ParsedPacket pp = packet.resolve();
            if (fullString) {
                listener.onResult(pp.toString());
            }
        }
        return result;
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
     *            the {@link J1939} to set
     */
    public void setJ1939(J1939 j1939) {
        this.j1939 = j1939;
    }
}
