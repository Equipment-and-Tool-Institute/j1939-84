/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.model;

public enum KeyState {

    KEY_OFF("Key OFF", false, false),
    KEY_ON_ENGINE_OFF("Key ON/Engine OFF", true, false),
    KEY_ON_ENGINE_RUNNING("Key ON/Engine RUNNING", true, true),
    UNKNOWN("UNKNOWN", false, false);

    public final String name;
    public final boolean isKeyOn;
    public final boolean isEngineOn;

    KeyState(String name, boolean isKeyOn, boolean isEngineOn) {
        this.name = name;
        this.isKeyOn = isKeyOn;
        this.isEngineOn = isEngineOn;
    }

    @Override
    public String toString() {
        return name;
    }
}
