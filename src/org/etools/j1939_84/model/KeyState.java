/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.model;

public enum KeyState {

    KEY_OFF_ENGINE_OFF("key off"),
    KEY_ON_ENGINE_OFF("key on/engine off"),
    KEY_ON_ENGINE_ON("key on/engine on");

    private final String name;

    KeyState(String name) {
        this.name = name;
    }

    private String getName() {
        return name;
    }
}
