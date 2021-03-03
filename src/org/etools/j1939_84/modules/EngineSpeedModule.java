/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.model.KeyState.KEY_OFF_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;

import java.util.concurrent.TimeUnit;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.EngineSpeedPacket;
import org.etools.j1939_84.model.KeyState;

/**
 * {@link FunctionalModule} used to determine if the Engine is communicating
 * and/or running
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class EngineSpeedModule extends FunctionalModule {

    public EngineSpeedModule() {
        super();
    }

    private EngineSpeedPacket getEngineSpeedPacket() {
        // The transmission rate changes based upon the engine speed. 100 ms is
        // the longest period between messages when the engine is off
        return getJ1939().read(EngineSpeedPacket.class, J1939.ENGINE_ADDR, 300, TimeUnit.MILLISECONDS)
                         .flatMap(e -> e.left)
                         .orElse(null);
    }

    /**
     * Returns true if the Engine Speed is returned from the engine indicating
     * the engine is communicating
     *
     * @return true if the engine is communicating; false if the engine is not
     *         communicating
     */
    private boolean isEngineCommunicating() {
        return getEngineSpeedPacket() != null;
    }

    /**
     * 
     * @return the current keystate of the vehicle based on requirements around
     *         engine communications and RPMs {@link KeyState}
     */
    public KeyState getKeyState() {
        if (!isEngineCommunicating()) {
            return KEY_OFF_ENGINE_OFF;
        }
        if (isEngineRunning()) {
            return KEY_ON_ENGINE_RUNNING;
        }
        return KEY_ON_ENGINE_OFF;
    }

    /**
     * Returns true if the Engine is communicating with an Engine Speed greater
     * than or equal to 300 RPM.
     *
     * @return true if the engine is not running; false otherwise
     */
    private boolean isEngineRunning() {
        EngineSpeedPacket packet = getEngineSpeedPacket();
        return !(packet == null || packet.isError() || packet.isNotAvailable() || packet.getEngineSpeed() <= 300);
    }

    /**
     * Returns the engine speed in RPMs.
     *
     * @return double representing the engine speed in RPMs
     */
    public Double getEngineSpeed() {
        EngineSpeedPacket packet = getEngineSpeedPacket();
        return packet == null ? null : packet.getEngineSpeed();
    }

    public String getEngineSpeedAsString() {
        Double engineSpeed = getEngineSpeed();
        return (engineSpeed == null ? "----" : engineSpeed) + " RPMs";
    }
}
