/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The Interface for a vehicle communications bus
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public interface Bus {

    static Predicate<Packet> interruptFilter(Predicate<Packet> p) {
        return MultiQueue.interruptFilter(p);
    }

    /**
     * Returns the source address used by the tool for communications
     *
     * @return int
     */
    int getAddress();

    /**
     * Returns the speed of the bus
     *
     * @return the speed of the bus
     * @throws BusException
     *             if the speed cannot be determined
     */
    int getConnectionSpeed() throws BusException;

    /**
     * Reads {@link Packet}s from the bus
     *
     * @param timeout
     *            the amount of time to read packets
     * @param unit
     *            the {@link TimeUnit} for the amount of time
     * @return a {@link Stream} of {@link Packet}s
     * @throws BusException
     *             if there is a problem reading packets
     */
    Stream<Packet> read(long timeout, TimeUnit unit) throws BusException;

    /**
     * Sends a {@link Packet} to the vehicle communications bus
     *
     * @param packet
     *            the {@link Packet} to send
     * @throws BusException
     *             if there is a problem sending the packet
     */
    void send(Packet packet) throws BusException;

}
