/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.KeyState.UNKNOWN;
import static org.etools.j1939tools.j1939.J1939.ENGINE_ADDR;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.etools.j1939_84.model.KeyState;
import org.etools.j1939tools.bus.Either;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.EngineSpeedPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.FunctionalModule;

/**
 * {@link FunctionalModule} used to determine if the Engine is communicating
 * and/or running
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class EngineSpeedModule extends FunctionalModule {

    private static final double WMA_FACTOR = 64.0;

    private final AtomicLong timeAtSpeed = new AtomicLong(0);
    private final AtomicLong timeAtIdle = new AtomicLong(0);
    private final AtomicReference<Double> idleEngineSpeed = new AtomicReference<>(600.0);
    private final AtomicReference<Double> pedalPosition = new AtomicReference<>(0.0);

    private LocalDateTime lastTimestamp = null;

    private final AtomicReference<Double> averagedEngineSpeed = new AtomicReference<>(0.0);
    private final AtomicReference<Double> currentEngineSpeed = new AtomicReference<>(0.0);

    private EngineSpeedPacket getEngineSpeedPacket() {
        // The transmission rate changes based upon the engine speed. 100 ms is
        // the longest period between messages when the engine is off
        // BUT BAM may block bus, so increase timeout to 1.2 s to avoid BAM being mistaken for KEY_OFF
        return getJ1939().read(EngineSpeedPacket.class, ENGINE_ADDR, 1200, TimeUnit.MILLISECONDS)
                         .flatMap(e -> e.left)
                         .orElse(null);
    }

    public KeyState getKeyState() {
        EngineSpeedPacket packet = getEngineSpeedPacket();

        if (packet == null) {
            return KEY_OFF;
        } else if (packet.isError() || packet.isNotAvailable()) {
            return UNKNOWN;
        } else if (packet.getEngineSpeed() <= 300) {
            return KEY_ON_ENGINE_OFF;
        } else {
            return KEY_ON_ENGINE_RUNNING;
        }
    }

    public String getEngineSpeedAsString() {
        EngineSpeedPacket packet = getEngineSpeedPacket();
        if (packet == null) {
            return "Key Off";
        } else if (packet.isError()) {
            return "Error RPMs";
        } else if (packet.isNotAvailable()) {
            return "N/A RPMs";
        } else {
            return packet.getEngineSpeed() + " RPMs";
        }
    }

    public boolean isEngineAtIdle() {

        // N_SPN188 is the engine speed value for Point 1 in the Engine Configuration Message given in SPN 188.

        // LOWER_LIMIT = N_SPN188 – 100 RPM
        double lowerIdleLimit = idleEngineSpeed() - 100;
        // UPPER_LIMIT = N_SPN188 + 100 RPM
        double upperIdleLimit = idleEngineSpeed() + 100;

        // APS = SPN 91
        // AUX_APS = SPN 29 (if supported, substitute 0 for unsupported, and broadcast values of 0xFF or 0xFE)
        // (APS <= 0.4%) AND (AUX_APS <= 0.4%) //[No Pedal Demand]
        boolean noPedalDemand = pedalPosition() <= 0.4;

        // AND (LOWER_LIMIT <= N <= UPPER_LIMIT) [Engine Speed Within Boundaries]
        boolean currentInRange = lowerIdleLimit < currentEngineSpeed() && currentEngineSpeed() < upperIdleLimit;

        // AND (LOWER_LIMIT <= N_WMA(+) <= UPPER_LIMIT) [Weighted Average Engine Speed within boundaries]
        // Where N_WMA(+) = 1/64 * N(+) + 63/64 * N_WMA(-)
        boolean averageInRange = lowerIdleLimit < averagedEngineSpeed() && averagedEngineSpeed() < upperIdleLimit;

        // AND (N_SPN188 <= 850 RPM) [Engine Idle Speed lower than Upper Bound]
        boolean idleSpeedReasonable = idleEngineSpeed() <= 850;

        // Engine is at Idle when:
        return noPedalDemand && currentInRange && averageInRange && idleSpeedReasonable;
    }

    public void startMonitoringEngineSpeed(ExecutorService executor,
                                           Predicate<Either<GenericPacket, AcknowledgmentPacket>> stopPredicate) {
        lastTimestamp = null;
        timeAtSpeed.set(0);
        timeAtIdle.set(0);
        idleEngineSpeed.set(600.0);

        var engineSpeedPacket = getEngineSpeedPacket();
        if (engineSpeedPacket == null || engineSpeedPacket.isNotAvailable() || engineSpeedPacket.isError()) {
            currentEngineSpeed.set(0.0);
            averagedEngineSpeed.set(0.0);
        } else {
            currentEngineSpeed.set(engineSpeedPacket.getEngineSpeed());
            averagedEngineSpeed.set(engineSpeedPacket.getEngineSpeed());
        }

        executor.submit(() -> {
            getJ1939().readGenericPacket(stopPredicate)
                      .filter(p -> p.getSourceAddress() == ENGINE_ADDR)
                      .forEach(p -> {
                          int pgn = p.getPacket().getPgn();
                          switch (pgn) {
                              case 61444:
                                  processEngineSpeedPacket(p);
                                  break;
                              case 65251:
                                  processIdleSpeedPacket(p);
                                  break;
                              case 61443:
                                  processPedalPositionPacket(p);
                                  break;
                              default:
                                  break;
                          }
                      });
        });

    }

    private void processIdleSpeedPacket(GenericPacket packet) {
        packet.getSpnValue(188).ifPresent(idleEngineSpeed::set);
    }

    private void processPedalPositionPacket(GenericPacket packet) {
        Stream.concat(packet.getSpnValue(29).stream(), packet.getSpnValue(91).stream())
              .mapToDouble(v -> v)
              .max()
              .stream()
              .forEach(pedalPosition::set);
    }

    private void processEngineSpeedPacket(GenericPacket packet) {
        long millisBetweenPackets = calculateMillisBetweenPackets(packet);

        packet.getSpnValue(190).ifPresent(engineSpeed -> {
            currentEngineSpeed.set(engineSpeed);
            averagedEngineSpeed.set(engineSpeed / WMA_FACTOR
                    + averagedEngineSpeed.get() * (WMA_FACTOR - 1) / WMA_FACTOR);

            if (isEngineAtIdle()) {
                timeAtIdle.getAndAdd(millisBetweenPackets);
            } else if (engineSpeed >= 1150) {
                timeAtSpeed.getAndAdd(millisBetweenPackets);
            }
        });
    }

    private long calculateMillisBetweenPackets(GenericPacket packet) {
        LocalDateTime currentTimestamp = packet.getPacket().getTimestamp();
        long diffTime = 0;
        if (lastTimestamp != null) {
            diffTime = lastTimestamp.until(currentTimestamp, MILLIS);
        }
        lastTimestamp = currentTimestamp;
        return diffTime;
    }

    public long secondsAtSpeed() {
        return TimeUnit.MILLISECONDS.toSeconds(timeAtSpeed.get());
    }

    public long secondsAtIdle() {
        return TimeUnit.MILLISECONDS.toSeconds(timeAtIdle.get());
    }

    public double idleEngineSpeed() {
        return idleEngineSpeed.get();
    }

    public double averagedEngineSpeed() {
        return averagedEngineSpeed.get();
    }

    public double pedalPosition() {
        return pedalPosition.get();
    }

    public double currentEngineSpeed() {
        return currentEngineSpeed.get();
    }

}
