/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.simulated;

import java.util.ArrayList;
import java.util.Collection;
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
    private final ScheduledExecutorService exec = new ScheduledThreadPoolExecutor(2, r -> new Thread(() -> {
        try {
            r.run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }, "Sim Thread"));

    /**
     * The collection of responses. If the response returns true, then don't try
     * other responses.
     */
    public final Collection<Function<Packet, Boolean>> responses = new ArrayList<>();

    public Sim(Bus bus) throws BusException {
        this.bus = bus;
        // stream is collected in the current thread to avoid missing any
        // packets during
        // the Thread startup.
        Stream<Packet> stream = bus.read(365, TimeUnit.DAYS);
        exec.submit(() -> stream.parallel().forEach(packet -> {
            for (var r : responses) {
                if (r.apply(packet)) {
                    return;
                }
            }
        }));
    }

    @Override
    public void close() {
        exec.shutdown();
    }

    /**
     * Sends a response every time
     *
     * @param  predicate
     *                       the {@link Predicate} used to determine if the {@link Packet}
     *                       should be sent
     * @param  supplier
     *                       the {@link Supplier} of the {@link Packet}
     * @return           this
     */
    public Sim response(Predicate<Packet> predicate, Function<Packet, Packet> supplier) {
        responses.add(request -> {
            if (predicate.test(request)) {
                Packet response = supplier.apply(request);
                send(response);
                // if request is not to broadcast, only accept first
                // response
                return response.getPgn() < 0xF000 && request.getDestination() != 0xFF;
            }
            return false;
        });
        return this;
    }

    /**
     * Same as response(Predicate, Function), but ignore the request when
     * constructing the response.
     */
    public Sim response(Predicate<Packet> predicate, Supplier<Packet> supplier) {
        return response(predicate, p -> supplier.get());
    }

    /**
     * Schedules a {@link Runnable} periodically
     *
     * @param  period
     *                    how often the {@link Runnable} should be run
     * @param  delay
     *                    how long to wait until running the first {@link Runnable}
     * @param  unit
     *                    the {@link TimeUnit} for the delay and period
     * @param  run
     *                    the {@link Runnable} to run
     * @return        this
     */
    public Sim schedule(int period, int delay, TimeUnit unit, Runnable run) {
        exec.scheduleAtFixedRate(run, delay, period, unit);
        return this;
    }

    /**
     * Schedules a {@link Packet} to be sent periodically
     *
     * @param  period
     *                      how often the {@link Packet} should be sent
     * @param  delay
     *                      how long to wait until sending the first {@link Packet}
     * @param  unit
     *                      the {@link TimeUnit} for the delay and period
     * @param  supplier
     *                      the {@link Supplier} of the {@link Packet} to send
     * @return          this
     */
    public Sim schedule(int period, int delay, TimeUnit unit, Supplier<Packet> supplier) {
        return schedule(period, delay, unit, () -> send(supplier.get()));
    }

    /**
     * Sends a {@link Packet} from the given {@link Supplier} catching any
     * exceptions. Should only be called from the exec.
     *
     * @param supplier
     *                     the {@link Supplier} for the {@link Packet}
     */
    private void send(Packet p) {
        try {
            bus.send(p);
        } catch (Throwable e) {
            J1939_84.getLogger().log(Level.SEVERE, "Error sending", e);
        }
    }

    /**
     * Sends a {@link Packet} now.
     *
     * @param packet
     *                   the {@link Packet}
     */
    public void sendNow(Packet packet) {
        exec.execute(() -> send(packet));
    }

}
