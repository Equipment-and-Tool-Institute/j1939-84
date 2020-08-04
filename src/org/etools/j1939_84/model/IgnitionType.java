/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

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
