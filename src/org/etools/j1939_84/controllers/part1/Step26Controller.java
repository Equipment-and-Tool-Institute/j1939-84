/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
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
public class Step26Controller extends StepController {

    private static List<Integer> collectNotAvailableSPNs(List<Integer> requiredSpns, Stream<GenericPacket> stream) {
        return stream
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

    public Step26Controller(DataRepository dataRepository) {
        this(new J1939DaRepository(), dataRepository);
    }

    Step26Controller(Executor executor,
            EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule,
            TableA1Validator tableA1Validator,
            J1939DaRepository j1939DaRepository,
            DataRepository dataRepository,
            BroadcastValidator broadcastValidator,
            BusService busService) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, 1, 26, 0);
        this.tableA1Validator = tableA1Validator;
        this.j1939DaRepository = j1939DaRepository;
        this.dataRepository = dataRepository;
        this.broadcastValidator = broadcastValidator;
        this.busService = busService;
    }

    private Step26Controller(J1939DaRepository j1939DaRepository, DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                new TableA1Validator(dataRepository),
                j1939DaRepository,
                dataRepository,
                new BroadcastValidator(dataRepository, j1939DaRepository),
                new BusService(j1939DaRepository));
    }

    /**
     * Reports if the given PGN was not received or if any supported SPNs were
     * received as Not Available
     *
     * @param supportedSpns
     *            the list Supported SPNs
     * @param pgn
     *            the PGN of interest
     * @param packets
     *            the packet that may contain the PGN
     * @param moduleAddress
     *            the module address of concern, can be null for Global messages
     * @return true if the given PGN wasn't received or any supported SPN is Not
     *         Available
     */
    private boolean checkForNotAvailableSPNs(List<Integer> supportedSpns,
                                             int pgn,
                                             List<GenericPacket> packets,
                                             Integer moduleAddress) {
        String message = null;
        if (packets.isEmpty()) {
            if (moduleAddress != null) {
                message = "No DS response for PGN " + pgn + " from " + getModuleName(moduleAddress);
            } else {
                message = "No Global response for PGN " + pgn;
            }
        } else {
            List<Integer> missingSpns = collectNotAvailableSPNs(supportedSpns, packets.stream());
            if (!missingSpns.isEmpty()) {
                String missedSpns = missingSpns.stream().map(String::valueOf).collect(Collectors.joining(", "));
                if (moduleAddress != null) {
                    message = "SPNs received as NOT AVAILABLE from " + getModuleName(moduleAddress) + ": " + missedSpns;
                } else {
                    message = "SPNs received as NOT AVAILABLE: " + missedSpns;
                }
            }
        }
        if (message != null) {
            addFailure(getPartNumber(), getStepNumber(), message);
        }
        return message != null;
    }

    /**
     * Reports the PGNs there are supported by the module but not received and
     * the SPNs that were received by broadcast as Not Available
     *
     * @param obdModuleInformation
     *            the module information
     * @param foundPackets
     *            the Map of PGNs to the List of those packets sent by the
     *            module
     * @param spns
     *            the list of SPNs that are still of concern
     * @return the List of SPNs which were not found
     */
    private List<Integer> collectAndReportNotAvailableSPNs(OBDModuleInformation obdModuleInformation,
                                                           Map<Integer, List<GenericPacket>> foundPackets,
                                                           List<Integer> spns) {

        int moduleSourceAddress = obdModuleInformation.getSourceAddress();

        // Find the PGN Definitions for the PGNs we expect to receive
        List<Integer> requiredPgns = new ArrayList<>(busService.collectNonOnRequestPGNs(spns));

        List<Integer> missingSpns = new ArrayList<>();

        requiredPgns.removeAll(foundPackets.keySet());
        if (!requiredPgns.isEmpty()) {
            // Expected PGNs were not received.
            // Add those SPNs to the missingSpns List and create a list of them
            // for the report
            // a. Fail if not received for any broadcast SPN indicated as
            // supported by the OBD ECU in DM24
            // with the Source Address matching the received message) in DM24.
            requiredPgns.stream()
                    .map(j1939DaRepository::findPgnDefinition)
                    .flatMap(pgnDef -> pgnDef.getSpnDefinitions().stream())
                    .map(SpnDefinition::getSpnId)
                    .filter(spns::contains)
                    .distinct()
                    .sorted()
                    .peek(missingSpns::add)
                    .map(spn -> "SPN " + spn + " was not broadcast by " + getModuleName(moduleSourceAddress))
                    .forEach(message -> addFailure(getPartNumber(), getStepNumber(), message));
        }

        // Find any Supported SPNs which has a value of Not Available
        // a. Fail if unsupported (received as not available (as described in
        // SAE J1939-71))
        // for any broadcast SPN indicated as supported by the OBD ECU in DM24
        // with the Source Address matching the received message) in DM24.
        List<Integer> missedSpns = collectNotAvailableSPNs(spns,
                foundPackets.values().stream().flatMap(Collection::stream));
        missedSpns.forEach(spn -> {
            String msg = "SPN " + spn + " was broadcast as NOT AVAILABLE by " + getModuleName(moduleSourceAddress);
            addFailure(getPartNumber(), getStepNumber(), msg);
        });

        missingSpns.addAll(missedSpns);

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
        List<Integer> supportedSpns = dataRepository.getObdModules()
                .stream()
                .flatMap(m -> m.getDataStreamSpns().stream())
                .map(SupportedSPN::getSpn)
                .collect(Collectors.toList());

        // 6.1.26.3.a. Identify SPNs provided in the data stream that are listed
        // in Table A-1, but are not supported by any OBD ECU in its DM24
        // response.
        // 6.1.26.4.a. Fail/warn per Table A-1 column, “Action if SPN provided
        // but not included in DM24”
        tableA1Validator.reportMissingSPNs(supportedSpns, getListener(), fuelType, getPartNumber(), getStepNumber());

        // a. Gather broadcast data for all SPNs that are supported for data
        // stream in the OBD ECU DM24 responses.
        // we need 3 samples plus time for a BAM, to 4 * maxPeriod
        List<GenericPacket> packets = busService.readBus(broadcastValidator.getMaximumBroadcastPeriod() * 4);

        // This list will keep track of the PGNs which we need to listen for at
        // the end of the test
        List<Integer> broadcastPgns = new ArrayList<>();

        // Find and report any Supported SPNs which should have been received
        // but weren't
        for (OBDModuleInformation obdModule : dataRepository.getObdModules()) {

            int moduleAddress = obdModule.getSourceAddress();

            // Check the Broadcast Period of the received packets1
            Map<Integer, List<GenericPacket>> foundPackets = broadcastValidator.buildPGNPacketsMap(packets,
                    moduleAddress);
            broadcastValidator.reportBroadcastPeriod(foundPackets,
                    moduleAddress,
                    getListener(),
                    getPartNumber(),
                    getStepNumber());

            // Get the SPNs which are supported by the module
            List<Integer> dataStreamSpns = obdModule.getDataStreamSpns()
                    .stream()
                    .map(SupportedSPN::getSpn)
                    .collect(Collectors.toList());
            // Find SPNs sent a Not Available
            List<Integer> missingSpns = collectAndReportNotAvailableSPNs(obdModule, foundPackets, dataStreamSpns);

            String moduleName = Lookup.getAddressName(moduleAddress);

            // DS Request for all SPNs that are sent on-request AND those were
            // missed earlier
            List<Integer> requestPgns = busService.getPgnsForDSRequest(missingSpns, dataStreamSpns);
            for (int pgn : requestPgns) {
                updateProgress("DS Request for " + pgn + " to " + moduleName);
                List<GenericPacket> dsResponse = busService.dsRequest(pgn, moduleAddress);
                packets.addAll(dsResponse);

                boolean needToRequestGlobally = checkForNotAvailableSPNs(supportedSpns, pgn, dsResponse, moduleAddress);

                if (needToRequestGlobally) {
                    // Re-request the missing SPNs globally
                    updateProgress("Global Request for PGN " + pgn);
                    List<GenericPacket> globalPackets = busService.globalRequest(pgn);
                    packets.addAll(globalPackets);
                    checkForNotAvailableSPNs(supportedSpns, pgn, globalPackets, null);
                }
            }

            // Gather the PGNs which are sent on Broadcast and needed to be
            // requested
            // There are some PGNs which are sent periodically once requested
            broadcastPgns.addAll(busService.collectBroadcastPGNs(requestPgns));
        }

        // See if there are any PGNs that were missing which need to be listened
        // for
        // We listen for missing SPNs from all modules, rather than waiting
        // foreach module
        if (!broadcastPgns.isEmpty()) {

            // Get the list of SPNs that that Support in the broadcastPgns
            List<Integer> spns = broadcastPgns.stream()
                    .map(j1939DaRepository::findPgnDefinition)
                    .flatMap(pgnDef -> pgnDef.getSpnDefinitions().stream())
                    .map(SpnDefinition::getSpnId)
                    .filter(supportedSpns::contains)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Listen for the PGNs of interest
            int waitTime = broadcastValidator.getMaximumBroadcastPeriod(broadcastPgns) * 4;
            Predicate<GenericPacket> busFilter = p -> broadcastPgns.contains(p.getPacket().getPgn());
            List<GenericPacket> broadcastPackets = busService.readBus(waitTime, busFilter);
            packets.addAll(broadcastPackets);

            for (OBDModuleInformation obdModule : dataRepository.getObdModules()) {
                int moduleAddress = obdModule.getSourceAddress();
                // Report on the broadcast periods and any Not Available SPNs
                Map<Integer, List<GenericPacket>> foundPackets = broadcastValidator.buildPGNPacketsMap(broadcastPackets,
                        moduleAddress);
                broadcastValidator.reportBroadcastPeriod(foundPackets,
                        moduleAddress,
                        getListener(),
                        getPartNumber(),
                        getStepNumber());
                collectAndReportNotAvailableSPNs(obdModule, foundPackets, spns);
            }
        }

        for (int moduleAddress : dataRepository.getObdModuleAddresses()) {
            // e. Fail/warn per Table A-1, if an expected SPN from the DM24
            // support list from an OBD ECU is provided by a non-OBD ECU.
            // (provided extraneously)
            tableA1Validator.reportNonObdModuleProvidedSPNs(packets,
                    moduleAddress,
                    getListener(),
                    getPartNumber(),
                    getStepNumber());
        }

        // d. Fail/warn if any broadcast data is not valid for KOEO conditions
        // as per Table A-1, Minimum Data Stream Support.
        tableA1Validator.reportImplausibleSPNValues(packets,
                supportedSpns,
                getListener(),
                false,
                fuelType,
                getPartNumber(),
                getStepNumber());

        // f. Fail/warn per Table A-1 if two or more ECUs provide an SPN listed
        // in Table A-1
        tableA1Validator.reportDuplicateSPNs(packets, getListener(), getPartNumber(), getStepNumber());

        updateProgress("End Part 1 Step 26");
    }

}
