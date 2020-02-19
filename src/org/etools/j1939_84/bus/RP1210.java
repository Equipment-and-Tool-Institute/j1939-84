/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.simulated.Engine;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 * Class used to gather the RP1210 Adapters available for vehicle communications
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class RP1210 {

    /**
     * The device Id used to indicate the adapter is not a physical one
     */
    public static final short FAKE_DEV_ID = (short) -1;

    /**
     * The {@link Adapter} that can be used for System Testing
     */
    private static final Adapter LOOP_BACK_ADAPTER = new Adapter("Loop Back Adapter", "Simulated", FAKE_DEV_ID);

    static final String WINDOWS_PATH = System.getenv("WINDIR");

    private List<Adapter> adapters;

    private final File base;

    private Engine engine;

    /**
     * Default Constructor
     */
    public RP1210() {
        this(WINDOWS_PATH);
    }

    /**
     * Constructor exposed for testing
     *
     * @param basePath
     *                 the base path where the RP121032.ini file is located
     */
    RP1210(String basePath) {
        base = basePath == null ? null : new File(basePath);
    }

    /**
     * Reads the file system to create and return a {@link List} of
     * {@link Adapter}s that can be used for communications with the vehicle
     *
     * @return {@link List} of {@link Adapter}s
     * @throws BusException
     *                      if there is a problem generating the list
     */
    public List<Adapter> getAdapters() throws BusException {
        if (adapters == null) {
            adapters = new ArrayList<>();
            // if (J1939_84.isTesting() || J1939_84.isDebug())
            {
                adapters.add(LOOP_BACK_ADAPTER);
            }
            adapters.addAll(parse());
            Collections.sort(adapters, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        }
        return adapters;
    }

    /**
     * Parses the RP121032.ini file in the base location to determine the
     * {@link List} of {@link Adapter}s that can be used for vehicle
     * communications
     *
     * @return a {@link List} of {@link Adapter}s
     * @throws BusException
     *                      if there is a problem reading the file system
     */
    private List<Adapter> parse() throws BusException {
        List<Adapter> list = new ArrayList<>();
        if (base != null) {
            try {
                Ini ini = new Ini(new File(base, "RP121032.INI"));
                for (String id : ini.get("RP1210Support").getOrDefault("APIImplementations", "").split("\\s*,\\s*")) {
                    try {
                        Ini driver = new Ini(new File(base, id + ".INI"));
                        Section vendorSection = driver.get("VendorInformation");
                        final String vendorName = vendorSection.getOrDefault("Name", "");

                        // loop through protocols to find J1939
                        for (String protocolId : vendorSection.getOrDefault("Protocols", "").split("\\s*,\\s*")) {
                            Section protocolSection = driver.get("ProtocolInformation" + protocolId);
                            if (protocolSection.getOrDefault("ProtocolString", "").contains("J1939")) {
                                // add listed devices
                                for (String devId : protocolSection.getOrDefault("Devices", "").split("\\s*,\\s*")) {
                                    final short deviceId = Short.parseShort(devId);
                                    final String deviceName = driver.get("DeviceInformation" + devId)
                                            .getOrDefault("DeviceDescription", "UNKNOWN");
                                    list.add(new Adapter(vendorName + " - " + deviceName, id, deviceId));
                                }
                            }
                        }
                    } catch (IOException e) {
                        J1939_84.getLogger().log(Level.SEVERE, "Error Parsing ini file", e);
                    }
                }
            } catch (IOException e) {
                throw new BusException("Failed to parse adapters.", e);
            }
        }
        return list;
    }

    /**
     * Sets the {@link Adapter} that will be used for communication with the
     * vehicle. A {@link Bus} is returned which will be used to send and read
     * {@link Packet}s
     *
     * @param adapter
     *                the {@link Adapter} to use for communications
     * @param address
     *                the source address of the tool
     * @return An {@link Bus}
     * @throws BusException
     *                      if there is a problem setting the adapter
     */
    public Bus setAdapter(Adapter adapter, int address) throws BusException {
        if (engine != null) {
            engine.close();
            engine = null;
        }

        if (adapter.getDeviceId() == FAKE_DEV_ID) {
            EchoBus bus = new EchoBus(address);
            if (J1939_84.isDebug()) {
                engine = new Engine(bus);
            }
            return bus;
        } else {
            return new RP1210Bus(adapter, address);
        }
    }
}
