/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA1Validator;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * Data stream support verification 6.1.26.1 Actions: a. Gather broadcast data
 * for all SPNs that are supported for data stream in the OBD ECU DM24
 * responses. b. Gather/timestamp each parameter at least three times to be able
 * to verify frequency of broadcast.
 * <p>
 * 6.1.26.2 Fail/warn criteria: a. Fail if no response/no valid data for any
 * broadcast SPN indicated as supported by the OBD ECU in DM24. b. Fail if any
 * parameter is not broadcast within ± 10% of the specified broadcast period. c.
 * Fail/warn if any broadcast data is not valid for KOEO conditions as per Table
 * A-1, Minimum Data Stream Support. d. Fail/warn per Table A-1, if an expected
 * SPN from the DM24 support list is provided by a non-OBD ECU. e. Fail/warn per
 * Table A-1 if two or more ECUs provide an SPN listed in Table A-1
 * <p>
 * 6.1.26.3 Actions2 a. Identify SPNs provided in the data stream that are
 * listed in Table A-1, but are not supported by any OBD ECU in its DM24
 * response. 6.1.26.4 Fail/Warn Criteria2 a. Fail/warn per Table A-1 column,
 * “Action if SPN provided but not included in DM24”.
 * <p>
 * 6.1.26.5 Actions3: a. DS messages to ECU that indicated support in DM24 for
 * upon request SPNs and SPNs not observed in step 1. b. If no response/no valid
 * data for any SPN requested in 6.1.25.3.a, send global message to request that
 * SPN(s).
 * <p>
 * 6.1.26.6 Fail/warn criteria3: [Refer to footnotes 23 and 24 for a and b,
 * respectively] a. Fail if no response/no valid data for any broadcast SPN
 * indicated as supported by the OBD ECU in DM24. b. Fail if any parameter in a
 * fixed period broadcast message is not broadcast within ± 10% of the specified
 * broadcast period. c. Fail if any parameter in a variable period broadcast
 * message exceeds 110% of its recommended broadcast period.26 d. Fail/warn if
 * any broadcast data is not valid for KOEO conditions as per section A.1Table
 * A-1, Minimum Data Stream Support. e. Warn/info per Table A-1, if an expected
 * SPN from the DM24 support list is provided by a non-OBD ECU. f. Fail/warn per
 * Table A-1, if two or more ECUs provide an SPN listed in Table A-1.
 */
public class Part01Step26Controller extends StepController {

    private static List<Integer> collectNotAvailableSPNs(List<Integer> requiredSpns,
            Stream<GenericPacket> packetStream) {
        return packetStream
                .flatMap(p -> p.getSpns().stream())
                .filter(Spn::isNotAvailable)
                .map(Spn::getId)
                .filter(requiredSpns::contains)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private static String getModuleName(int moduleSourceAddress) {
        return Lookup.getAddressName(moduleSourceAddress);
    }

    private final BroadcastValidator broadcastValidator;
    private final BusService busService;
    private final DataRepository dataRepository;

    private final J1939DaRepository j1939DaRepository;

    private final TableA1Validator tableA1Validator;

    public Part01Step26Controller(DataRepository dataRepository) {
        this(new J1939DaRepository(), dataRepository);
    }

    Part01Step26Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           TableA1Validator tableA1Validator,
                           J1939DaRepository j1939DaRepository,
                           DataRepository dataRepository,
                           BroadcastValidator broadcastValidator,
                           BusService busService,
                           DateTimeModule dateTimeModule) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, dateTimeModule, 1, 26, 0);
        this.tableA1Validator = tableA1Validator;
        this.j1939DaRepository = j1939DaRepository;
        this.dataRepository = dataRepository;
        this.broadcastValidator = broadcastValidator;
        this.busService = busService;
    }

    private Part01Step26Controller(J1939DaRepository j1939DaRepository, DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new TableA1Validator(dataRepository),
                j1939DaRepository,
                dataRepository,
                new BroadcastValidator(dataRepository, j1939DaRepository),
                new BusService(j1939DaRepository),
                DateTimeModule.getInstance());
    }

    /**
     * Reports if the given PGN was not received or if any supported SPNs were
     * received as Not Available
     *
     * @param supportedSPNs
     *         the list Supported SPNs
     * @param pgn
     *         the PGN of interest
     * @param packets
     *         the packet that may contain the PGN
     * @param moduleAddress
     *         the module address of concern, can be null for Global messages
     * @return true if the given PGN wasn't received or any supported SPN is Not
     * Available
     */
    private List<String> checkForNotAvailableSPNs(List<Integer> supportedSPNs,
            int pgn,
            List<GenericPacket> packets,
            Integer moduleAddress) {
        Set<Integer> spns = new HashSet<>();

        if (packets.isEmpty()) {
            spns.addAll(j1939DaRepository.findPgnDefinition(pgn)
                    .getSpnDefinitions()
                    .stream()
                    .map(SpnDefinition::getSpnId)
                    .filter(supportedSPNs::contains)
                    .collect(Collectors.toSet()));

            String message;
            if (moduleAddress != null) {
                message = "6.1.26.6.a - No DS response for PGN " + pgn + " from " + getModuleName(moduleAddress);
            } else {
                message = "6.1.26.6.a - No Global response for PGN " + pgn;
            }
            addFailure(getPartNumber(), getStepNumber(), message);
        } else {
            List<Integer> missingSPNs = collectNotAvailableSPNs(supportedSPNs, packets.stream());
            if (!missingSPNs.isEmpty()) {
                spns.addAll(missingSPNs);
            }
        }

        return spns.stream().sorted().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * Reports the PGNs there are supported by the module but not received and
     * the SPNs that were received by broadcast as Not Available
     *
     * @param moduleSourceAddress
     *         the module source address
     * @param foundPackets
     *         the Map of PGNs to the List of those packets sent by the
     *         module
     * @param supportedSPNs
     *         the list of SPNs that are still of concern
     * @return the List of SPNs which were not found
     */
    private List<Integer> collectAndReportNotAvailableSPNs(int moduleSourceAddress,
            List<GenericPacket> foundPackets,
            List<Integer> supportedSPNs) {

        // Find the PGN Definitions for the PGNs we expect to receive
        List<Integer> requiredPgns = new ArrayList<>(busService.collectNonOnRequestPGNs(supportedSPNs));

        List<Integer> missingSpns = new ArrayList<>();

        Set<Integer> foundPGNs = foundPackets.stream().map(p -> p.getPacket().getPgn()).collect(Collectors.toSet());
        requiredPgns.removeAll(foundPGNs);
        if (!requiredPgns.isEmpty()) {
            // Expected PGNs were not received.
            // Add those SPNs to the missingSpns List and create a list of them for the report
            // a. Fail if not received for any broadcast SPN indicated as supported by the OBD ECU in DM24
            // with the Source Address matching the received message) in DM24.
            getListener().onResult("");
            requiredPgns.stream()
                    .map(j1939DaRepository::findPgnDefinition)
                    .flatMap(pgnDef -> pgnDef.getSpnDefinitions().stream())
                    .map(SpnDefinition::getSpnId)
                    .filter(supportedSPNs::contains)
                    .distinct()
                    .sorted()
                    .peek(missingSpns::add)
                    .map(spn -> "6.1.26.2.a - SPN " + spn + " was not broadcast by " + getModuleName(moduleSourceAddress))
                    .forEach(message -> addFailure(getPartNumber(), getStepNumber(), message));
        }

        // Find any Supported SPNs which has a value of Not Available
        missingSpns.addAll(collectNotAvailableSPNs(supportedSPNs, foundPackets.stream()));

        return missingSpns;
    }

    @Override
    protected void run() throws Throwable {
        updateProgress("Start Part 1 Step 26");

        busService.setup(getJ1939(), getListener());

        // This will listen for all Broadcast PGNs in hopes of finding all Data
        // Stream Supported SPNs
        // Then by module, process and report on the data found
        // Any missing data and on-request PGNs are requested DS
        // That data is reported on
        // Any yet missing data is requested globally
        // Finally, the bus is monitored again for any missing data
        // The Table A1 validator then reports on the data found

        FuelType fuelType = dataRepository.getVehicleInformation().getFuelType();

        // Collect all the Data Stream Supported SPNs from all OBD Modules.
        List<Integer> supportedSPNs = dataRepository.getObdModules()
                .stream()
                .flatMap(m -> m.getDataStreamSpns().stream())
                .map(SupportedSPN::getSpn)
                .collect(Collectors.toList());

        tableA1Validator.reportExpectedMessages(getListener());

        // 6.1.26.1.a. Gather broadcast data for all SPNs that are supported for data
        // stream in the OBD ECU DM24 responses.
        // we need 3 samples plus time for a BAM, to 4 * maxPeriod
        Stream<GenericPacket> packetStream = busService.readBus(broadcastValidator.getMaximumBroadcastPeriod() * 4);

        List<GenericPacket> packets = packetStream
                .peek(p ->
                        // 6.1.26.2.a. Fail if unsupported (received as not available (as described in SAE J1939-71))
                        // for any broadcast SPN indicated as supported by the OBD ECU in DM24
                        // with the Source Address matching the received message) in DM24.
                        tableA1Validator.reportNotAvailableSPNs(p,
                                getListener(),
                                getPartNumber(),
                                getStepNumber(),
                                "6.1.26.2.a"))
                .peek(p ->
                        // 6.1.26.2.d. Fail/warn if any broadcast data is not valid for KOEO conditions
                        // as per Table A-1, Min Data Stream Support.
                        tableA1Validator.reportImplausibleSPNValues(p,
                                getListener(),
                                false,
                                fuelType,
                                getPartNumber(),
                                getStepNumber(),
                                "6.1.26.2.d"))
                .peek(p ->
                        // 6.1.26.2.e. Fail/warn per Table A-1, if an expected SPN from the DM24 support
                        // list from an OBD ECU is provided by a non-OBD ECU. (provided extraneously)

                        tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                getListener(),
                                getPartNumber(),
                                getStepNumber(),
                                "6.1.26.2.e"))
                .peek(p ->
                        // 6.1.26.3.a. Identify SPNs provided in the data stream that are listed
                        // in Table A-1, but are not supported by any OBD ECU in its DM24 response.
                        //6.1.26.4.a. Fail/warn per Table A-1 column, “Action if SPN provided but not included in DM24”.
                        tableA1Validator.reportProvidedButNotSupportedSPNs(p,
                                getListener(),
                                fuelType,
                                getPartNumber(),
                                getStepNumber(),
                                "6.1.26.4.a"))
                .peek(p -> tableA1Validator.reportPacketIfNotReported(p, getListener(), false))
                .collect(Collectors.toList());

        // 6.1.26.2.f. Fail/warn per Table A-1 if two or more ECUs provide an SPN listed in Table A-1
        tableA1Validator.reportDuplicateSPNs(packets, getListener(), getPartNumber(), getStepNumber(), "6.1.26.2.f");

        // Check the Broadcast Period of the received packets1
        //Map of PGN to (Map of Source Address to List of Packets)
        Map<Integer, Map<Integer, List<GenericPacket>>> foundPackets = broadcastValidator.buildPGNPacketsMap(packets);

        broadcastValidator.reportBroadcastPeriod(foundPackets,
                supportedSPNs,
                getListener(),
                getPartNumber(),
                getStepNumber());

        List<GenericPacket> onRequestPackets = new ArrayList<>();
        // Find and report any Supported SPNs which should have been received but weren't
        for (OBDModuleInformation obdModule : dataRepository.getObdModules()) {

            int moduleAddress = obdModule.getSourceAddress();

            // Get the SPNs which are supported by the module
            List<Integer> dataStreamSPNs = obdModule.getDataStreamSpns()
                    .stream()
                    .map(SupportedSPN::getSpn)
                    .collect(Collectors.toList());

            // Find SPNs sent as Not Available and those that should have been sent
            List<GenericPacket> modulePackets = packets.stream()
                    .filter(p -> p.getSourceAddress() == moduleAddress)
                    .collect(Collectors.toList());

            List<Integer> missingSPNs = collectAndReportNotAvailableSPNs(moduleAddress,
                    modulePackets,
                    dataStreamSPNs);

            // DS Request for all SPNs that are sent on-request AND those were missed earlier
            List<Integer> requestPGNs = busService.getPGNsForDSRequest(missingSPNs, dataStreamSPNs);

            //Remove the SPNs that were already received
            Set<Integer> receivedSPNs = packets.stream()
                    .filter(p -> p.getSourceAddress() == moduleAddress)
                    .flatMap(p -> p.getSpns().stream())
                    .filter(s -> !s.isNotAvailable())
                    .map(Spn::getId)
                    .collect(Collectors.toSet());
            dataStreamSPNs.removeAll(receivedSPNs);

            for (int pgn : requestPGNs) {
                String spns = j1939DaRepository.findPgnDefinition(pgn)
                        .getSpnDefinitions()
                        .stream()
                        .map(SpnDefinition::getSpnId)
                        .filter(s -> missingSPNs.contains(s) || dataStreamSPNs.contains(s))
                        .sorted()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                String dsMessage = "DS Request for PGN " + pgn + " to " + Lookup.getAddressName(moduleAddress) + " for SPNs " + spns;
                updateProgress(dsMessage);
                List<GenericPacket> dsResponse = busService.dsRequest(pgn, moduleAddress, dsMessage)
                        .peek(p -> tableA1Validator.reportNotAvailableSPNs(p,
                                getListener(),
                                getPartNumber(),
                                getStepNumber(),
                                "6.1.26.6.a"))
                        .peek(p ->
                                // 6.1.26.6.d. Fail/warn if any broadcast data is not valid for KOEO conditions
                                // as per Table A-1, Min Data Stream Support.
                                tableA1Validator.reportImplausibleSPNValues(p,
                                        getListener(),
                                        false,
                                        fuelType,
                                        getPartNumber(),
                                        getStepNumber(),
                                        "6.1.26.6.d"))
                        .peek(p ->
                                // 6.1.26.2.e. Fail/warn per Table A-1, if an expected SPN from the DM24 support
                                // list from an OBD ECU is provided by a non-OBD ECU. (provided extraneously)
                                tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                        getListener(),
                                        getPartNumber(),
                                        getStepNumber(),
                                        "6.1.26.6.e"))
                        .collect(Collectors.toList());
                onRequestPackets.addAll(dsResponse);

                List<String> notAvailableSPNs = checkForNotAvailableSPNs(supportedSPNs, pgn, dsResponse, moduleAddress);

                if (!notAvailableSPNs.isEmpty()) {
                    // Re-request the missing SPNs globally
                    String globalMessage = "Global Request for PGN " + pgn + " for SPNs "
                            + String.join(", ", notAvailableSPNs);
                    updateProgress(globalMessage);
                    List<GenericPacket> globalPackets = busService.globalRequest(pgn, globalMessage)
                            .peek(p -> tableA1Validator.reportNotAvailableSPNs(p,
                                    getListener(),
                                    getPartNumber(),
                                    getStepNumber(),
                                    "6.1.26.6.a"))
                            .peek(p ->
                                    // 6.1.26.6.d. Fail/warn if any broadcast data is not valid for KOEO conditions
                                    // as per Table A-1, Min Data Stream Support.
                                    tableA1Validator.reportImplausibleSPNValues(p,
                                            getListener(),
                                            false,
                                            fuelType,
                                            getPartNumber(),
                                            getStepNumber(),
                                            "6.1.26.6.d"))
                            .peek(p ->
                                    // 6.1.26.6.e. Fail/warn per Table A-1, if an expected SPN from the DM24 support
                                    // list from an OBD ECU is provided by a non-OBD ECU. (provided extraneously)
                                    tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                            getListener(),
                                            getPartNumber(),
                                            getStepNumber(),
                                            "6.1.26.6.e"))
                            .collect(Collectors.toList());
                    onRequestPackets.addAll(globalPackets);
                    checkForNotAvailableSPNs(supportedSPNs, pgn, globalPackets, null);
                }
            }
        }

        // 6.1.26.6.f. Fail/warn per Table A-1 if two or more ECUs provide an SPN listed in Table A-1
        tableA1Validator.reportDuplicateSPNs(onRequestPackets,
                getListener(),
                getPartNumber(),
                getStepNumber(),
                "6.1.26.6.f");

        updateProgress("End Part 1 Step 26");
    }

}
