/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.HighResVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.TotalVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.ResultsListener;

/**
 * The {@link FunctionalModule} that is used to gather general information about
 * the vehicle
 *
 * @author Matt Gumbel (matt@solidesign.net)
 *
 */
public class VehicleInformationModule extends FunctionalModule {

    /**
     * Helper method to convert a {@link Set} of {@link CalibrationInformation}
     * to a {@link String}
     *
     * @param cals
     *             the {@link Set} of {@link CalibrationInformation}
     * @return {@link String}
     */
    private static String calibrationAsString(Set<CalibrationInformation> cals) {
        return cals.stream().map(t -> t.toString()).collect(Collectors.joining(NL));
    }

    /**
     * The calibrations read from the vehicle
     */
    private Set<CalibrationInformation> calibrations;

    /**
     * The Family Name of the Engine as read from the vehicle
     */
    private String engineFamilyName;

    /**
     * The Model Year of the Engine as read from the vehicle
     */
    private Integer engineModelYear;

    /**
     * The Vehicle Identification Number read from the vehicle
     */
    private String vin;

    /**
     * Constructor
     */
    public VehicleInformationModule() {
        this(new DateTimeModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param dateTimeModule
     *                       the {@link DateTimeModule} to use
     */
    public VehicleInformationModule(DateTimeModule dateTimeModule) {
        super(dateTimeModule);
    }

    /**
     * Queries the vehicle for all {@link CalibrationInformation} from the
     * modules
     *
     * @return a {@link Set} of {@link CalibrationInformation}
     * @throws IOException
     *                     if there are no {@link CalibrationInformation} returned
     */
    private Set<CalibrationInformation> getCalibrations() throws IOException {
        if (calibrations == null) {
            calibrations = getJ1939().requestMultiple(DM19CalibrationInformationPacket.class)
                    .flatMap(t -> t.getCalibrationInformation().stream()).collect(Collectors.toSet());
            if (calibrations.isEmpty()) {
                throw new IOException("Timeout Error Reading Calibrations");
            }
        }
        return calibrations;
    }

    /**
     * Queries the vehicle for all {@link CalibrationInformation} from the
     * modules. A {@link String} of the resulting {@link CalibrationInformation}
     * is returned
     *
     * @return {@link String}
     * @throws IOException
     *                     if there are no {@link CalibrationInformation} returned
     */
    public String getCalibrationsAsString() throws IOException {
        return calibrationAsString(getCalibrations());
    }

    /**
     * Queries the vehicle for the Engine Family Name
     *
     * @return The Engine Family Name
     * @throws IOException if no values are returned from the vehicle or multiple
     *                     differing values are returned from the vehicle
     */
    public String getEngineFamilyName() throws IOException {
        if (engineFamilyName == null) {
            Set<String> results = getJ1939()
                    .requestMultiple(DM56EngineFamilyPacket.class)
                    .map(t -> t.getFamilyName())
                    .collect(Collectors.toSet());
            if (results.size() == 0) {
                throw new IOException("Timeout Error Reading Engine Family");
            } else if (results.size() > 1) {
                throw new IOException("Different Engine Families Received");
            }
            engineFamilyName = results.stream().findFirst().get();
        }
        return engineFamilyName;
    }

    /**
     * Queries the vehicle for the Engine Model Year
     *
     * @return The Engine Model Year as an integer
     * @throws IOException if no values are returned from the vehicle or multiple
     *                     differing values are returned from the vehicle
     */
    public int getEngineModelYear() throws IOException {
        if (engineModelYear == null) {
            Set<Integer> results = getJ1939()
                    .requestMultiple(DM56EngineFamilyPacket.class)
                    .map(t -> t.getEngineModelYear())
                    .collect(Collectors.toSet());
            if (results.size() == 0) {
                throw new IOException("Timeout Error Reading Engine Model Year");
            } else if (results.size() > 1) {
                throw new IOException("Different Engine Model Years Received");
            }
            engineModelYear = results.stream().findFirst().get();
        }
        return engineModelYear;
    }

    /**
     * Queries the vehicle for the VIN.
     *
     * @return the Vehicle Identification Number as a {@link String}
     * @throws IOException
     *                     if no value is returned from the vehicle or different
     *                     VINs are returned
     */
    public String getVin() throws IOException {
        if (vin == null) {
            Set<String> vins = getJ1939().requestMultiple(VehicleIdentificationPacket.class).map(t -> t.getVin())
                    .collect(Collectors.toSet());
            if (vins.size() == 0) {
                throw new IOException("Timeout Error Reading VIN");
            }
            if (vins.size() > 1) {
                throw new IOException("Different VINs Received");
            }
            vin = vins.stream().findFirst().get();
        }
        return vin;
    }

    /**
     * Sends the Request for Address Claim and reports the results
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     */
    public void reportAddressClaim(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(AddressClaimPacket.PGN, GLOBAL_ADDR);
        List<AddressClaimPacket> responses = generateReport(listener,
                "Global Request for Address Claim",
                AddressClaimPacket.class,
                request);
        if (!responses.isEmpty() && !responses.stream().filter(p -> p.getFunctionId() == 0).findAny().isPresent()) {
            listener.onResult("Error: No module reported Function 0");
        }
    }

    /**
     * Requests the Calibration Information from all vehicle modules and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     */
    public void reportCalibrationInformation(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM19CalibrationInformationPacket.PGN, GLOBAL_ADDR);
        generateReport(listener,
                "Global DM19 (Calibration Information) Request",
                DM19CalibrationInformationPacket.class,
                request);
    }

    /**
     * Requests the Component Identification from all vehicle modules and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     */
    public void reportComponentIdentification(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(ComponentIdentificationPacket.PGN, GLOBAL_ADDR);
        generateReport(listener,
                "Global Component Identification Request",
                ComponentIdentificationPacket.class,
                request);
    }

    /**
     * Queries the bus and reports the speed of the vehicle bus
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     */
    public void reportConnectionSpeed(ResultsListener listener) {
        String result = getTime() + " Baud Rate: ";
        try {
            int speed = getJ1939().getBus().getConnectionSpeed();
            result += NumberFormat.getInstance(Locale.US).format(speed) + " bps";
        } catch (BusException e) {
            result += "Could not be determined";
        }
        listener.onResult(result);
    }

    /**
     * Requests the Engine Hours from the engine and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     */
    public void reportEngineHours(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(EngineHoursPacket.PGN, GLOBAL_ADDR);
        generateReport(listener, "Engine Hours Request", EngineHoursPacket.class, request);
    }

    /**
     * Requests the maximum Total Vehicle Distance from the vehicle and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     */
    public void reportVehicleDistance(ResultsListener listener) {
        listener.onResult(getTime() + " Vehicle Distance");
        Optional<HighResVehicleDistancePacket> hiResPacket = getJ1939()
                .read(HighResVehicleDistancePacket.class, 3, TimeUnit.SECONDS)
                .filter(p -> p.getTotalVehicleDistance() != ParsedPacket.NOT_AVAILABLE
                        && p.getTotalVehicleDistance() != ParsedPacket.ERROR)
                .max((p1, p2) -> Double.compare(p1.getTotalVehicleDistance(), p2.getTotalVehicleDistance()));

        Optional<? extends ParsedPacket> packet;
        if (hiResPacket.isPresent()) {
            packet = hiResPacket;
        } else {
            packet = getJ1939().read(TotalVehicleDistancePacket.class, 300, TimeUnit.MILLISECONDS)
                    .filter(p -> p.getTotalVehicleDistance() != ParsedPacket.NOT_AVAILABLE
                            && p.getTotalVehicleDistance() != ParsedPacket.ERROR)
                    .max((p1, p2) -> Double.compare(p1.getTotalVehicleDistance(), p2.getTotalVehicleDistance()));
        }

        listener.onResult(packet.map(getPacketMapperFunction()).orElse(TIMEOUT_MESSAGE));
    }

    /**
     * Requests the Vehicle Identification from all vehicle modules and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     */
    public void reportVin(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(VehicleIdentificationPacket.PGN, GLOBAL_ADDR);
        generateReport(listener, "Global VIN Request", VehicleIdentificationPacket.class, request);
    }

    public List<VehicleIdentificationPacket> requestVehicleIdentification(ResultsListener listener) {
        reportVin(listener);
        return getJ1939().requestMultiple(VehicleIdentificationPacket.class).collect(Collectors.toList());
    }

    /**
     * Clears the cached values that have been read from the vehicle
     */
    public void reset() {
        vin = null;
        calibrations = null;
    }
}
