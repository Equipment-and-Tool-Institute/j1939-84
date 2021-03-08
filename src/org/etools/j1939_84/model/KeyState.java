/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.model;

public enum KeyState {

    KEY_OFF("Key OFF"),
    KEY_ON_ENGINE_OFF("Key ON/Engine OFF"),
    KEY_ON_ENGINE_RUNNING("Key ON/Engine RUNNING"),
    UNKNOWN("Unknown");

    public final String name;

    KeyState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
