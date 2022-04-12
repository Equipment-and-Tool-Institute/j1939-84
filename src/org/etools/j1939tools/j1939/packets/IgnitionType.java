/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

/**
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public enum IgnitionType {
    COMPRESSION("Compression"), SPARK("Spark"), UNKNOWN("Unknown");

    public final String name;

    IgnitionType(String name) {
        this.name = name;
    }
}
