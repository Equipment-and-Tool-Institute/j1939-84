/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The Interface for a vehicle communications bus
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public interface Bus extends AutoCloseable {

    static Consumer<Packet> log(Supplier<String> prefix) {
        return p -> System.err.println(prefix.get() + p);
    }

    /**
     * close() can be used to interrupt all streams using this bus.
     */
    @Override
    void close();

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
     *
     * @throws BusException if the speed cannot be determined
     */
    int getConnectionSpeed() throws BusException;

    /**
     * Reads {@link Packet}s from the bus
     *
     * @param timeout the amount of time to read packets
     *
     * @param unit    the {@link TimeUnit} for the amount of time
     *
     * @return a {@link Stream} of {@link Packet}
     *
     * @throws BusException if there is a problem reading packets
     */
    Stream<Packet> read(long timeout, TimeUnit unit) throws BusException;

    /**
     * Reset stream timeout for stream created with bus.read(). To be used in a
     * stream call like peek, map or forEach.
     *
     * @param stream for which to reset timeout
     *
     * @param time   new timeout value
     *
     * @param unit   the {@link TimeUnit} for the amount of time
     */
    void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit);

    /**
     * Sends a {@link Packet} to the vehicle communications bus
     *
     * @param packet the {@link Packet} to send
     * 
     * @throws BusException if there is a problem sending the packet
     */
    void send(Packet packet) throws BusException;
}
