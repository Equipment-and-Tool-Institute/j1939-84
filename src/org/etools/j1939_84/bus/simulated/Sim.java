/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.simulated;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Packet;

/**
 * Used to simulate responses from vehicle modules
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public class Sim implements AutoCloseable {

    /**
     * The communications bus
     */
    private final Bus bus;

    /**
     * The executor
     */
    private final ScheduledExecutorService exec = new ScheduledThreadPoolExecutor(2);

    /**
     * The collection of responses
     */
    private final Collection<Function<Packet, Boolean>> responses = new ConcurrentLinkedQueue<>();

    public Sim(Bus bus) throws BusException {
        this.bus = bus;
        Stream<Packet> stream = bus.read(365, TimeUnit.DAYS);
        exec.submit(() -> {
            stream.forEach(packet -> {
                responses.stream().forEach(c -> c.apply(packet));
            });
        });
    }

    @Override
    public void close() {
        exec.shutdown();
    }

    /**
     * Sends a response every time
     *
     * @param predicate
     *                  the {@link Predicate} used to determine if the
     *                  {@link Packet}
     *                  should be sent
     * @param supplier
     *                  the {@link Supplier} of the {@link Packet}
     * @return this
     */
    public Sim response(Predicate<Packet> predicate, Supplier<Packet> supplier) {
        responses.add(packet -> {
            if (predicate.test(packet)) {
                send(supplier);
            }
            return false;
        });
        return this;
    }

    /**
     * Schedules a {@link Packet} to be sent periodically
     *
     * @param period
     *                 how often the {@link Packet} should be sent
     * @param delay
     *                 how long to wait until sending the first {@link Packet}
     * @param unit
     *                 the {@link TimeUnit} for the delay and period
     * @param supplier
     *                 the {@link Supplier} of the {@link Packet} to send
     * @return this
     */
    public Sim schedule(int period, int delay, TimeUnit unit, Supplier<Packet> supplier) {
        exec.scheduleAtFixedRate(() -> {
            send(supplier);
        }, delay, period, unit);
        return this;
    }

    /**
     * Sends a {@link Packet} from the given {@link Supplier} catching any
     * exceptions
     *
     * @param supplier
     *                 the {@link Supplier} for the {@link Packet}
     */
    private void send(Supplier<Packet> supplier) {
        try {
            bus.send(supplier.get());
        } catch (BusException e) {
            J1939_84.getLogger().log(Level.SEVERE, "Error sending", e);
        }
    }

}
