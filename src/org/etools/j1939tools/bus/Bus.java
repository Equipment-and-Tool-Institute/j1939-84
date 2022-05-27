/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The Interface for a vehicle communications bus
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public interface Bus extends AutoCloseable {

    /**
     * close() can be used to interrupt all streams using this bus.
     */
    @Override
    void close();

    Stream<Packet> duplicate(Stream<Packet> stream, int time, TimeUnit unit);

    /**
     * Returns the source address used by the tool for communications
     *
     * @return int
     */
    int getAddress();

    /**
     * Returns the speed of the bus
     *
     * @return              the speed of the bus
     *
     * @throws BusException
     *                          if the speed cannot be determined
     */
    int getConnectionSpeed() throws BusException;

    @SuppressFBWarnings(value = { "UW_UNCOND_WAIT", "WA_NOT_IN_LOOP" }, justification = "Wait for stream open.")
    default AutoCloseable log(Function<Packet, String> prefix) throws BusException {
        Stream<Packet> stream = read(999, TimeUnit.DAYS);
        new Thread(() -> stream.forEach(p -> {
            System.err.println(prefix.apply(p));
        })).start();
        return () -> stream.close();
    }

    /**
     * Reads {@link Packet}s from the bus
     *
     * @param  timeout
     *                          the amount of time to read packets
     *
     * @param  unit
     *                          the {@link TimeUnit} for the amount of time
     *
     * @return              a {@link Stream} of {@link Packet}
     *
     * @throws BusException
     *                          if there is a problem reading packets
     */
    Stream<Packet> read(long timeout, TimeUnit unit) throws BusException;

    /**
     * Reset stream timeout for stream created with bus.read(). To be used in a
     * stream call like peek, map or forEach.
     *
     * @param stream
     *                   for which to reset timeout
     *
     * @param time
     *                   new timeout value
     *
     * @param unit
     *                   the {@link TimeUnit} for the amount of time
     */
    void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit);

    /**
     * Sends a {@link Packet} to the vehicle communications bus
     *
     * @param  packet
     *                          the {@link Packet} to send
     *
     * @throws BusException
     *                          if there is a problem sending the packet
     * @return              The echo of the packet sent. Useful for the timestamp.
     */
    Packet send(Packet packet) throws BusException;

    /**
     * 
     * @return if another module is detected on the CAN bus using this address.
     */
    boolean imposterDetected();

    /**
     * Get non-TP bus.
     */
    default Bus getRawBus() {
        return this;
    }
}
