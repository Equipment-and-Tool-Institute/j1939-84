/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.j1939.J1939TP;
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

    static final String WINDOWS_PATH = System.getenv("WINDIR");

    /**
     * The {@link Adapter} that can be used for System Testing
     */
    private static final Adapter LOOP_BACK_ADAPTER = new Adapter("Loop Back Adapter", "Simulated", FAKE_DEV_ID);

    private final File base;

    private List<Adapter> adapters;

    private AutoCloseable engine;

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
     *                     the base path where the RP121032.ini file is located
     */
    RP1210(String basePath) {
        base = basePath == null ? null : new File(basePath);
    }

    /**
     * Reads the file system to create and return a {@link List} of
     * {@link Adapter}s that can be used for communications with the vehicle
     *
     * @return              {@link List} of {@link Adapter}s
     * @throws BusException
     *                          if there is a problem generating the list
     */
    public List<Adapter> getAdapters() throws BusException {
        if (adapters == null) {
            adapters = new ArrayList<>();
            adapters.addAll(getSyntheticAdapters());
            adapters.addAll(parseAdapters());
            adapters.sort(Comparator.comparing(Adapter::getName));
        }
        return adapters;
    }

    private Collection<? extends Adapter> getSyntheticAdapters() {
        List<Adapter> adapters = new ArrayList<>();
        if (J1939_84.isTesting()) {
            adapters.add(LOOP_BACK_ADAPTER);
        }
        return adapters;
    }

    /**
     * Parses the RP121032.ini file in the base location to determine the
     * {@link List} of {@link Adapter}s that can be used for vehicle
     * communications
     *
     * @return              a {@link List} of {@link Adapter}s
     * @throws BusException
     *                          if there is a problem reading the file system
     */
    private List<Adapter> parseAdapters() throws BusException {
        List<Adapter> list = new ArrayList<>();
        if (base != null) {
            try {
                for (String id : getAdapterManufacturers()) {
                    try {
                        Ini driverIni = getDriverIni(id);

                        Section vendorSection = driverIni.get("VendorInformation");
                        String vendorName = vendorSection.getOrDefault("Name", "");

                        long timeStampWeight = getTimeStampWeight(vendorSection);

                        // loop through protocols to find J1939
                        for (String protocolId : getProtocols(vendorSection)) {
                            Section protocolSection = driverIni.get("ProtocolInformation" + protocolId);
                            if (isJ1939Section(protocolSection)) {
                                Arrays.stream(getDevices(protocolSection))
                                      .map(devId -> createAdapter(id, driverIni, vendorName, timeStampWeight, devId))
                                      .forEach(list::add);
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
     * @param  adapter
     *                          the {@link Adapter} to use for communications
     * @param  address
     *                          the source address of the tool
     * @return              An {@link Bus}
     * @throws BusException
     *                          if there is a problem setting the adapter
     */
    public Bus setAdapter(Adapter adapter, int address) throws BusException {
        if (engine != null) {
            try {
                engine.close();
            } catch (Exception e) {
                throw new IllegalStateException("Unexpected error closing simulated engine.", e);
            }
            engine = null;
        }

        if (adapter.getDeviceId() == FAKE_DEV_ID) {
            EchoBus bus = new EchoBus(address);
            engine = new Engine(bus);
            return bus;
        } else {
            return new J1939TP(new RP1210Bus(adapter, address, true));
        }
    }

    private Ini getDriverIni(String id) throws IOException {
        return new Ini(new File(base, id + ".INI"));
    }

    private Ini getRp1210Ini() throws IOException {
        return new Ini(new File(base, "RP121032.INI"));
    }

    private String[] getAdapterManufacturers() throws IOException {
        return getRp1210Ini().get("RP1210Support").getOrDefault("APIImplementations", "").split("\\s*,\\s*");
    }

    private static String[] getDevices(Section protocolSection) {
        return protocolSection.getOrDefault("Devices", "").split("\\s*,\\s*");
    }

    private static boolean isJ1939Section(Section protocolSection) {
        return protocolSection.getOrDefault("ProtocolString", "").contains("J1939");
    }

    private static String[] getProtocols(Section vendorSection) {
        return vendorSection.getOrDefault("Protocols", "").split("\\s*,\\s*");
    }

    private static long getTimeStampWeight(Section vendorSection) {
        long timeStampWeight;
        try {
            timeStampWeight = Long.parseLong(vendorSection.getOrDefault("TimeStampWeight", "1"));
        } catch (Throwable t) {
            J1939_84.getLogger()
                    .log(Level.SEVERE,
                         "Error Parsing TimeStampWeight from ini file.  Assuming 1000 (ms resolution).",
                         t);
            timeStampWeight = 1000;
        }
        return timeStampWeight;
    }

    private static Adapter createAdapter(String id, Ini driver, String vendorName, long timeStampWeight, String devId) {
        short deviceId = Short.parseShort(devId);
        String deviceName = driver.get("DeviceInformation" + devId).getOrDefault("DeviceDescription", "UNKNOWN");
        return new Adapter(vendorName + " - " + deviceName, id, deviceId, timeStampWeight);
    }
}
