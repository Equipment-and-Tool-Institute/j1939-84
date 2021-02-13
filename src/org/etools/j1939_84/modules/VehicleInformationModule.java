/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939_84.controllers.ResultsListener.NOOP;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * The {@link FunctionalModule} that is used to gather general information about
 * the vehicle
 *
 * @author Matt Gumbel (matt@solidesign.net)
 */
public class VehicleInformationModule extends FunctionalModule {
    /**
     * The calibrations read from the vehicle
     */
    private List<CalibrationInformation> calibrations;

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
        super();
    }

    /**
     * Queries the vehicle for all {@link CalibrationInformation} from the
     * modules
     *
     * @return a {@link Set} of {@link CalibrationInformation}
     * @throws IOException
     *         if there are no {@link CalibrationInformation} returned
     */
    public List<CalibrationInformation> getCalibrations() throws IOException {
        if (calibrations == null) {
            Collection<Either<DM19CalibrationInformationPacket, AcknowledgmentPacket>> raw = new ArrayList<>(getJ1939()
                                                                                                                     .requestGlobal(
                                                                                                                             null,
                                                                                                                             DM19CalibrationInformationPacket.class,
                                                                                                                             NOOP
                                                                                                                     )
                                                                                                                     .getEither());
            calibrations = raw.stream()
                    // flatten an Optional<Either<packetWithAList>>
                    .flatMap(t -> t.left.stream()
                            .flatMap(p -> p.getCalibrationInformation().stream()))
                    // get consistent order
                    .sorted(Comparator.comparing(
                            CalibrationInformation::toString))
                    .collect(Collectors.toList());
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
     *         if there are no {@link CalibrationInformation} returned
     */
    public String getCalibrationsAsString() throws IOException {
        return getCalibrations().stream().map(CalibrationInformation::toString).collect(Collectors.joining(NL));
    }

    /**
     * Queries the vehicle for the Engine Family Name
     *
     * @return The Engine Family Name
     * @throws IOException
     *         if no values are returned from the vehicle or multiple
     *         differing values are returned from the vehicle
     */
    public String getEngineFamilyName() throws IOException {
        if (engineFamilyName == null) {
            Set<String> results = getJ1939()
                    .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP)
                    .getEither().stream()
                    .flatMap(e -> e.left.stream())
                    .map(DM56EngineFamilyPacket::getFamilyName)
                    .collect(Collectors.toSet());
            if (results.size() == 0) {
                throw new IOException("Timeout Error Reading Engine Family");
            } else if (results.size() > 1) {
                throw new IOException("Different Engine Families Received");
            } else {
                engineFamilyName = results.stream().findFirst().get();
            }
        }
        return engineFamilyName;
    }

    /**
     * Queries the vehicle for the Engine Model Year
     *
     * @return The Engine Model Year as an integer
     * @throws IOException
     *         if no values are returned from the vehicle or multiple
     *         differing values are returned from the vehicle
     */
    public int getEngineModelYear() throws IOException {
        if (engineModelYear == null) {
            Set<Integer> results = getJ1939()
                    .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP)
                    .getEither().stream()
                    .flatMap(e -> e.left.stream())
                    .map(DM56EngineFamilyPacket::getEngineModelYear)
                    .collect(Collectors.toSet());
            if (results.size() == 0) {
                throw new IOException("Timeout Error Reading Engine Model Year");
            } else if (results.size() > 1) {
                throw new IOException("Different Engine Model Years Received");
            } else {
                engineModelYear = results.stream().findFirst().get();
            }
        }
        return engineModelYear;
    }

    /**
     * Queries the vehicle for the VIN.
     *
     * @return the Vehicle Identification Number as a {@link String}
     * @throws IOException
     *         if no value is returned from the vehicle or different VINs
     *         are returned
     */
    public String getVin() throws IOException {
        if (vin == null) {
            Set<String> vins = getJ1939().requestGlobal(null, VehicleIdentificationPacket.class, NOOP)
                    .getPackets().stream()
                    .map(VehicleIdentificationPacket::getVin)
                    .collect(Collectors.toSet());
            if (vins.size() == 0) {
                throw new IOException("Timeout Error Reading VIN");
            } else if (vins.size() > 1) {
                throw new IOException("Different VINs Received");
            } else {
                vin = vins.stream().findFirst().get();
            }
        }
        return vin;
    }

    /**
     * Sends the Request for Address Claim and reports the results
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the report
     */
    public RequestResult<AddressClaimPacket> reportAddressClaim(ResultsListener listener) {
        RequestResult<AddressClaimPacket> responses = getJ1939().requestGlobal("Global Request for Address Claim",
                                                                               AddressClaimPacket.class,
                                                                               listener);
        if (!responses.getPackets().isEmpty()
                && responses.getPackets().stream().noneMatch(p -> p.getFunctionId() == 0)) {
            listener.onResult("Error: No module reported Function 0");
        }
        return responses;
    }

    public List<DM19CalibrationInformationPacket> reportCalibrationInformation(ResultsListener listener) {
        return requestDMPackets("DM19", DM19CalibrationInformationPacket.class, GLOBAL_ADDR, listener).getPackets();
    }

    public BusResult<DM19CalibrationInformationPacket> reportCalibrationInformation(ResultsListener listener,
                                                                                    int address) {
        return requestDMPackets("DM19", DM19CalibrationInformationPacket.class, address, listener).busResult();
    }

    /**
     * Requests globally the Component Identification from all vehicle modules
     * and generates a {@link String} that's suitable for inclusion in the
     * report
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the report
     * @return {@link List} of {@link ComponentIdentificationPacket}
     */
    public RequestResult<ComponentIdentificationPacket> reportComponentIdentification(ResultsListener listener) {
        listener.onResult("");
        return getJ1939().requestGlobal("Global Component Identification Request",
                                        ComponentIdentificationPacket.class,
                                        listener);
    }

    /**
     * Requests the Component Identification from all specified address and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the report
     * @param address
     *         the address of vehicle module to which the message will be
     *         addressed
     * @return {@link List} of {@link ComponentIdentificationPacket}
     */
    public BusResult<ComponentIdentificationPacket> reportComponentIdentification(ResultsListener listener,
                                                                                  int address) {
        return getJ1939().requestDS("DS Component Identification Request to " + Lookup.getAddressName(address),
                                    ComponentIdentificationPacket.class,
                                    address,
                                    listener);
    }

    /**
     * Queries the bus and reports the speed of the vehicle bus
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the report
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
     * Requests the Vehicle Identification from all vehicle modules and
     * generates adds the information gathered to the report returning the
     * Packets returned by the query.
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the report
     * @return List of {@link VehicleIdentificationPacket}
     */
    public List<VehicleIdentificationPacket> reportVin(ResultsListener listener) {
        return getJ1939().requestGlobal("Global VIN Request", VehicleIdentificationPacket.class, listener).getPackets();
    }

    /**
     * Sends a global request for the Engine Hours from the engine and generates
     * a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *         the {@link ResultsListener} that will be given the report
     */
    public RequestResult<EngineHoursPacket> requestEngineHours(ResultsListener listener) {
        return getJ1939().requestGlobal("Global Engine Hours Request", EngineHoursPacket.class, listener);
    }

    /**
     * Sends the DM5 to determine which modules support HD-OBD. It returns a
     * {@link List} of source addresses of the modules that do support HD-OBD.
     *
     * @return List of source addresses
     */
    public List<Integer> getOBDModules(ResultsListener listener) {
        return requestDMPackets("DM5", DM5DiagnosticReadinessPacket.class, GLOBAL_ADDR, listener).getPackets()
                .stream()
                .filter(DM5DiagnosticReadinessPacket::isHdObd)
                .map(ParsedPacket::getSourceAddress)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    public void requestKeyOnEngineOff(ResultsListener listener) {
        requestKeyStateEngineState(true, false, listener);
    }

    public void requestKeyOnEngineOn(ResultsListener listener) {
        requestKeyStateEngineState(true, true, listener);
    }

    public void requestKeyOffEngineOff(ResultsListener listener) {
        requestKeyStateEngineState(false, false, listener);
    }

    private void requestKeyStateEngineState(boolean isKeyOn, boolean isEngineOn, ResultsListener listener) {
        int pgn;
        if (isKeyOn && isEngineOn) {
            pgn = 0x1FFFF;
        } else if (isKeyOn) {
            pgn = 0x1FFFE;
        } else {
            pgn = 0x1FFFC;
        }
        getJ1939().requestGlobal("Requesting Key " + isKeyOn + " Engine " + isEngineOn + " - REPORT IF SEEN IN THE FIELD",
                                 pgn,
                                 getJ1939().createRequestPacket(pgn, GLOBAL_ADDR),
                                 listener);
    }

    /**
     * Clears the cached values that have been read from the vehicle
     */
    public void reset() {
        vin = null;
        calibrations = null;
    }
}
