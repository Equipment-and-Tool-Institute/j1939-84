/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.controllers.ResultsListener.NOOP;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.J1939.GLOBAL_ADDR;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.KeyState;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.packets.AddressClaimPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939tools.modules.FunctionalModule;

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
     * @return             a {@link Set} of {@link CalibrationInformation}
     * @throws IOException
     *                         if there are no {@link CalibrationInformation} returned
     */
    public List<CalibrationInformation> getCalibrations() throws IOException {
        if (calibrations == null) {
            calibrations = getJ1939()
                                     .requestGlobal(null, DM19CalibrationInformationPacket.class, NOOP)
                                     .toPacketStream()
                                     .map(DM19CalibrationInformationPacket::getCalibrationInformation)
                                     .flatMap(Collection::stream)
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
     * @return             {@link String}
     * @throws IOException
     *                         if there are no {@link CalibrationInformation} returned
     */
    public String getCalibrationsAsString() throws IOException {
        return getCalibrations().stream().map(CalibrationInformation::toString).collect(Collectors.joining(NL));
    }

    /**
     * Queries the vehicle for the Engine Family Name
     *
     * @return             The Engine Family Name
     * @throws IOException
     *                         if no values are returned from the vehicle or multiple
     *                         differing values are returned from the vehicle
     */
    public String getEngineFamilyName() throws IOException {
        if (engineFamilyName == null) {
            Set<String> results = getJ1939()
                                            .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP)
                                            .toPacketStream()
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
     * @return             The Engine Model Year as an integer
     * @throws IOException
     *                         if no values are returned from the vehicle or multiple
     *                         differing values are returned from the vehicle
     */
    public int getEngineModelYear() throws IOException {
        if (engineModelYear == null) {
            Set<Integer> results = getJ1939()
                                             .requestGlobal(null, DM56EngineFamilyPacket.class, NOOP)
                                             .toPacketStream()
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
     * @return             the Vehicle Identification Number as a {@link String}
     * @throws IOException
     *                         if no value is returned from the vehicle or different VINs
     *                         are returned
     */
    public String getVin() throws IOException {
        if (vin == null) {
            Set<String> vins = getJ1939().requestGlobal(null, VehicleIdentificationPacket.class, NOOP)
                                         .toPacketStream()
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
     *                     the {@link CommunicationsListener that will be given the report
     */
    public RequestResult<AddressClaimPacket> reportAddressClaim(ResultsListener listener) {
        RequestResult<AddressClaimPacket> responses = getJ1939().requestGlobal("Address Claim",
                                                                               AddressClaimPacket.class,
                                                                               listener);
        if (!responses.getPackets().isEmpty()
                && responses.getPackets().stream().noneMatch(p -> p.getFunctionId() == 0)) {
            listener.onResult("Error: No ECU reported Function 0");
        }
        return responses;
    }

    /**
     * Queries the bus and reports the speed of the vehicle bus
     *
     * @param listener
     *                     the {@link CommunicationsListener} that will be given the report
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
     * Sends the DM5 to determine which modules support HD-OBD. It returns a
     * {@link List} of source addresses of the modules that do support HD-OBD.
     *
     * @return          List of source addresses
     * @param  listener
     */
    public List<Integer> getOBDModules(CommunicationsListener listener) {
        return requestDMPackets("DM5",
                                DM5DiagnosticReadinessPacket.class,
                                GLOBAL_ADDR,
                                listener)
                                         .toPacketStream()
                                         .filter(DM5DiagnosticReadinessPacket::isHdObd)
                                         .map(ParsedPacket::getSourceAddress)
                                         .sorted()
                                         .distinct()
                                         .collect(Collectors.toList());
    }

    // TODO move back
    public void changeKeyState(ResultsListener listener, KeyState keyState) {
        int pgn;
        if (keyState == KEY_ON_ENGINE_RUNNING) {
            pgn = 0x1FFFF;
        } else if (keyState == KEY_ON_ENGINE_OFF) {
            pgn = 0x1FFFE;
        } else if (keyState == KEY_OFF) {
            pgn = 0x1FFFC;
        } else {
            throw new IllegalArgumentException("Unknown Key State of " + keyState);
        }

        getJ1939().requestGlobal("Requesting " + keyState + " - REPORT IF SEEN IN THE FIELD",
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
