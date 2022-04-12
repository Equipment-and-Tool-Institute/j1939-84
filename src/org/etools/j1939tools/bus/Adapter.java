/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import java.util.List;

/**
 * Represents a vehicle communications adapter
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class Adapter {

    /**
     * The ID of the Device from the INI file
     */
    private final short deviceId;

    /**
     * The name of the DLL/INI file for the adapter
     */
    private final String dllName;

    /**
     * The display name for the adapter
     */
    private final String name;

    private final List<String> connectionStrings;

    private final long timestampWeight;

    /**
     * Constructor used in tests.
     *
     * @param name
     *                     the display name for the adapter
     * @param dllName
     *                     the DLL/INI file for the adapter
     * @param deviceId
     *                     the device ID
     */
    public Adapter(String name, String dllName, short deviceId) {
        this(name, dllName, deviceId, 1, List.of("J1939:Baud=Auto", "J1939:Baud=500", "J1939:Baud=250"));
    }

    /**
     * Constructor
     *
     * @param name
     *                     the display name for the adapter
     * @param dllName
     *                     the DLL/INI file for the adapter
     * @param deviceId
     *                     the device ID
     */
    public Adapter(String name, String dllName, short deviceId, long timestampWeight, List<String> connectionStrings) {
        this.name = name;
        this.dllName = dllName;
        this.deviceId = deviceId;
        this.timestampWeight = timestampWeight;
        this.connectionStrings = connectionStrings;
    }

    public List<String> getConnectionStrings() {
        return connectionStrings;
    }

    /**
     * The ID of the Device
     *
     * @return the deviceId
     */
    public short getDeviceId() {
        return deviceId;
    }

    /**
     * The Name of the DLL
     *
     * @return the dllName
     */
    public String getDLLName() {
        return dllName;
    }

    /**
     * The display name for the Adapter
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    public long getTimestampWeight() {
        return timestampWeight;
    }

    @Override public String toString() {
        return getName();
    }
}
