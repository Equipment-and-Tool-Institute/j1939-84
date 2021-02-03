/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;
import org.etools.j1939_84.controllers.BroadcastValidator;
import org.etools.j1939_84.controllers.BusService;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA1Validator;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.2.17 KOER Data stream verification
 */
public class Part02Step17Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 17;
    private static final int TOTAL_STEPS = 0;

    private final BroadcastValidator broadcastValidator;
    private final BusService busService;

    private final J1939DaRepository j1939DaRepository;

    private final TableA1Validator tableA1Validator;

    public Part02Step17Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new TableA1Validator(DataRepository.getInstance()),
             J1939DaRepository.getInstance(),
             new BroadcastValidator(DataRepository.getInstance(), J1939DaRepository.getInstance()),
             new BusService(J1939DaRepository.getInstance()));
    }

    Part02Step17Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           TableA1Validator tableA1Validator,
                           J1939DaRepository j1939DaRepository,
                           BroadcastValidator broadcastValidator,
                           BusService busService) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.tableA1Validator = tableA1Validator;
        this.j1939DaRepository = j1939DaRepository;
        this.broadcastValidator = broadcastValidator;
        this.busService = busService;
    }

    @Override
    protected void run() throws Throwable {
        //Copied from Part1 Step 26

        busService.setup(getJ1939(), getListener());

        FuelType fuelType = getDataRepository().getVehicleInformation().getFuelType();

        // Collect all the Data Stream Supported SPNs from all OBD Modules.
        List<Integer> supportedSPNs = getDataRepository().getObdModules()
                .stream()
                .flatMap(m -> m.getFilteredDataStreamSPNs().stream())
                .map(SupportedSPN::getSpn)
                .collect(Collectors.toList());

        tableA1Validator.reportExpectedMessages(getListener());

        // 6.2.17.1.a. Gather broadcast data for all SPNs that are supported for data stream in the OBD ECU responses.
        // x2 to ensure all necessary messages have been received
        List<GenericPacket> packets = busService.readBus(broadcastValidator.getMaximumBroadcastPeriod() * 2)
                .peek(p ->
                              //  6.2.17.2.a. Fail if unsupported (received as not available (as described in SAE J1939-71))
                              // for any broadcast SPN indicated as supported by the OBD ECU in DM24
                              // with the Source Address matching the received message) in DM24.
                              tableA1Validator.reportNotAvailableSPNs(p,
                                                                      getListener(),
                                                                      getPartNumber(),
                                                                      getStepNumber(),
                                                                      "6.2.17.2.a"))
                .peek(p ->
                              // 6.2.17.2.b. Fail/warn if any broadcast data is not valid for KOER conditions
                              // as per Table A-1, Minimum Data Stream Support.
                              tableA1Validator.reportImplausibleSPNValues(p,
                                                                          getListener(),
                                                                          true,
                                                                          fuelType,
                                                                          getPartNumber(),
                                                                          getStepNumber(),
                                                                          "6.2.17.2.b"))
                .peek(p ->
                              // 6.2.17.2.c. Fail/warn per Table A-1 if an expected SPN from the DM24 support list
                              // is provided by a non-OBD ECU.
                              tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                                                              getListener(),
                                                                              getPartNumber(),
                                                                              getStepNumber(),
                                                                              "6.2.17.2.c"))
                .peek(p ->

                              // 6.2.17.3.a. Identify SPNs provided in the data stream that are listed in Table A-1 but not supported by any OBD ECU in its DM24 response.
                              // 6.2.17.4.a. Fail/warn per Table A-1 column, “Action if SPN provided but not included in DM24”.
                              tableA1Validator.reportProvidedButNotSupportedSPNs(p,
                                                                                 getListener(),
                                                                                 fuelType,
                                                                                 getPartNumber(),
                                                                                 getStepNumber(),
                                                                                 "6.2.17.4.a"))
                .peek(p -> tableA1Validator.reportPacketIfNotReported(p, getListener(), false))
                .collect(Collectors.toList());

        // 6.2.17.2.d. Fail/warn per Table A-1, if two or more ECUs provide an SPN listed.
        tableA1Validator.reportDuplicateSPNs(packets, getListener(), getPartNumber(), getStepNumber(), "6.2.17.2.d");

        // Check the Broadcast Period of the received packets1
        //Map of PGN to (Map of Source Address to List of Packets)
        Map<Integer, Map<Integer, List<GenericPacket>>> foundPackets = broadcastValidator.buildPGNPacketsMap(packets);

        broadcastValidator.reportBroadcastPeriod(foundPackets,
                                                 supportedSPNs,
                                                 getListener(),
                                                 getPartNumber(),
                                                 getStepNumber());

        // Find and report any Supported SPNs which should have been received but weren't
        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {
            int moduleAddress = obdModule.getSourceAddress();
            updateProgress("Verifying " + Lookup.getAddressName(moduleAddress));

            // Get the SPNs which are supported by the module
            List<Integer> dataStreamSPNs = obdModule.getFilteredDataStreamSPNs()
                    .stream()
                    .map(SupportedSPN::getSpn)
                    .collect(Collectors.toList());

            List<GenericPacket> modulePackets = packets.stream()
                    .filter(p -> p.getSourceAddress() == moduleAddress)
                    .collect(Collectors.toList());

            // Find the PGN Definitions for the PGNs we expect to receive
            List<Integer> requiredPgns = new ArrayList<>(busService.collectNonOnRequestPGNs(supportedSPNs));

            List<Integer> missingSPNs = broadcastValidator.collectAndReportNotAvailableSPNs(moduleAddress,
                                                                                            modulePackets,
                                                                                            dataStreamSPNs,
                                                                                            requiredPgns,
                                                                                            getListener(),
                                                                                            getPartNumber(),
                                                                                            getStepNumber(),
                                                                                            "6.2.17.2.a");

            // 6.2.17.5.a. DS messages to ECU that indicated support in DM24 for upon request SPNs and SPNs not observed in step 1.
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
                List<GenericPacket> dsResponse = busService.dsRequest(pgn, moduleAddress, spns)
                        .peek(p ->
                                      // 6.2.17.6.a. Fail if no response/no valid data for any upon request SPN indicated as
                                      // supported in DM24, per Table A-1.
                                      tableA1Validator.reportNotAvailableSPNs(p,
                                                                              getListener(),
                                                                              getPartNumber(),
                                                                              getStepNumber(),
                                                                              "6.2.17.6.a"))
                        .peek(p ->
                                      // 6.2.17.6.b. Fail/warn if any upon request data is not valid for
                                      // KOER conditions as per section A.1.
                                      tableA1Validator.reportImplausibleSPNValues(p,
                                                                                  getListener(),
                                                                                  true,
                                                                                  fuelType,
                                                                                  getPartNumber(),
                                                                                  getStepNumber(),
                                                                                  "6.2.17.6.b"))
                        .collect(Collectors.toList());

                List<String> notAvailableSPNs = broadcastValidator.collectAndReportNotAvailableSPNs(supportedSPNs,
                                                                                                    pgn,
                                                                                                    dsResponse,
                                                                                                    moduleAddress,
                                                                                                    getListener(),
                                                                                                    getPartNumber(),
                                                                                                    getStepNumber(),
                                                                                                    "6.2.17.6.a");

                if (!notAvailableSPNs.isEmpty()) {
                    // 6.2.17.5.b. If no response/no valid data for any SPN requested in 6.2.16.3.a, send global message to request that SPN(s).
                    // Re-request the missing SPNs globally
                    String globalMessage = "Global Request for PGN " + pgn + " for SPNs " + String.join(", ",
                                                                                                        notAvailableSPNs);
                    if (!j1939DaRepository.findPgnDefinition(pgn).isOnRequest()) {
                        // 6.2.17.6.c. Warn when global request was required for “broadcast” SPN
                        addWarning("6.2.17.6.c - " + globalMessage);
                    }
                    List<GenericPacket> globalPackets = busService.globalRequest(pgn, globalMessage)
                            .peek(p ->
                                          //  6.2.17.6.a. Fail if no response/no valid data for any upon request
                                          //  SPN indicated as supported in DM24, per Table A-1.
                                          tableA1Validator.reportNotAvailableSPNs(p,
                                                                                  getListener(),
                                                                                  getPartNumber(),
                                                                                  getStepNumber(),
                                                                                  "6.2.17.6.a"))
                            .peek(p ->

                                          // 6.2.17.6.b. Fail/warn if any upon request data is not valid for
                                          // KOER conditions as per section A.1.
                                          tableA1Validator.reportImplausibleSPNValues(p,
                                                                                      getListener(),
                                                                                      true,
                                                                                      fuelType,
                                                                                      getPartNumber(),
                                                                                      getStepNumber(),
                                                                                      "6.2.17.6.b"))
                            .collect(Collectors.toList());

                    //  6.2.17.6.a. Fail if no response/no valid data for any upon request
                    //  SPN indicated as supported in DM24, per Table A-1.
                    broadcastValidator.collectAndReportNotAvailableSPNs(supportedSPNs,
                                                                        pgn,
                                                                        globalPackets,
                                                                        null,
                                                                        getListener(),
                                                                        getPartNumber(),
                                                                        getStepNumber(),
                                                                        "6.2.17.6.a");
                }
            }
        }
    }

}
