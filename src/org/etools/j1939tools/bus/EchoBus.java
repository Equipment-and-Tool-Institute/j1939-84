/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Class used for testing that will not communicate with an actual vehicle.
 * Rather all {@link Packet}s sent will be echoed back in the queue
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public class EchoBus implements Bus {
    private final int address;

    private final MultiQueue<Packet> queue;

    /**
     * Constructor
     *
     * @param address
     *                    the address for this connector on the bus
     */
    public EchoBus(int address) {
        this(address, new MultiQueue<>());
    }

    /**
     * Constructor exposed for testing
     *
     * @param address
     *                    the address for this connector on the bus
     * @param queue
     *                    the {@link MultiQueue} to use to back the bus
     */
    public EchoBus(int address, MultiQueue<Packet> queue) {
        this.address = address;
        this.queue = queue;
    }

    @Override
    public void close() {
        queue.close();
    }

    @Override
    public Stream<Packet> duplicate(Stream<Packet> stream, int time, TimeUnit unit) {
        return queue.duplicate(stream, time, unit);
    }

    @Override
    public int getAddress() {
        return address;
    }

    @Override
    public int getConnectionSpeed() throws BusException {
        throw new BusException("Could not be determined");
    }

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) {
        return queue.stream(timeout, unit);
    }

    /**
     * Reset stream timeout for stream created with bus.read(). To be used in a
     * stream call like peek, map or forEach.
     */
    @Override
    public void resetTimeout(Stream<Packet> stream, int time, TimeUnit unit) {
        queue.resetTimeout(stream, time, unit);
    }

    @Override
    public Packet send(Packet p) {
        queue.add(p);
        return p;
    }

    @Override
    public boolean imposterDetected() {
        return false;
    }
}
