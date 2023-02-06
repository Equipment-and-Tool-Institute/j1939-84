/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.controllers.ResultsListener.MessageType.ERROR;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.ParsedPacket.NOT_AVAILABLE;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_GREEN_HOUSE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_HYBRID_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_GREEN_HOUSE_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_HYBRID_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_STORED_HYBRID_CHG_DEPLETING_100_HR;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_PG;
import static org.etools.j1939tools.modules.GhgTrackingModule.GHG_TRACKING_LIFETIME_PG;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_ACTIVITY_PGs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_PGs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_ACTIVE_100_HOURS_PGs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_STORED_100_HOURS_PGs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.etools.j1939_84.controllers.BroadcastValidator;
import org.etools.j1939_84.controllers.BusService;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA1Validator;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.model.ActiveTechnology;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.GhgActiveTechnologyPacket;
import org.etools.j1939tools.j1939.packets.GhgLifetimeActiveTechnologyPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.GhgTrackingModule;
import org.etools.j1939tools.modules.NOxBinningModule;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * 6.2.17 KOER Data stream verification
 */
public class Part02Step17Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 17;
    private static final int TOTAL_STEPS = 0;

    private final GhgTrackingModule ghgTrackingModule;
    private final NOxBinningModule nOxBinningModule;

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
             new CommunicationsModule(),
             new TableA1Validator(PART_NUMBER, STEP_NUMBER),
             J1939DaRepository.getInstance(),
             new BroadcastValidator(DataRepository.getInstance(), J1939DaRepository.getInstance()),
             new BusService(J1939DaRepository.getInstance()),
             new GhgTrackingModule(DateTimeModule.getInstance()),
             new NOxBinningModule(DateTimeModule.getInstance()));
    }

    Part02Step17Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           TableA1Validator tableA1Validator,
                           J1939DaRepository j1939DaRepository,
                           BroadcastValidator broadcastValidator,
                           BusService busService,
                           GhgTrackingModule ghgTrackingModule,
                           NOxBinningModule nOxBinningModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.tableA1Validator = tableA1Validator;
        this.j1939DaRepository = j1939DaRepository;
        this.broadcastValidator = broadcastValidator;
        this.busService = busService;
        this.ghgTrackingModule = ghgTrackingModule;
        this.nOxBinningModule = nOxBinningModule;
    }

    @Override
    protected void run() throws Throwable {
        // Copied from Part1 Step 26

        busService.setup(getJ1939(), getListener());

        // 6.2.17.1.a. Create a consolidated list of expected SPs and PGs for the OBD ECU DM24 response(s) given
        // in 6.1.4, where the data stream support bit (SPN 4101) defined in SAE J1939-73 5.7.24 is 0
        // 6.2.17.1.b. Create a second list of Table A-1 omissions from the consolidated OBD ECU DM24 response(s),
        // excluding those SPs noted (as CI or SI) for the opposite fuel type provided by the user.
        // 6.2.17.1.c. Omit the following SPNs (588, 976, 1213, 1220, 12675, 12691, 12730, 12783, 12797) which are
        // included in the lists. Display a list of the omitted SPs in the report.
        // 6.2.17.1.d. Display the completed lists of supported SPs and unsupported SPs (as ‘broadcast’ or ‘upon
        // request’), that follow from the vehicle DM24 composite
        tableA1Validator.reportExpectedMessages(getListener());

        // 6.2.17.2.a. Gather broadcast data for all SPNs that are supported for data stream in the OBD ECU responses
        // and the added SPNs from Table A-1, using the SPN list from Part 1. This shall include the both SPs that are
        // expected to be queried with DS queries (in step 5 for SPs supported in DM24) and SPs that that are expected
        // without queries.
        // 6.2.17.2.b. Gather and timestamp each message that is observed at least three times to be able to verify
        // frequency of broadcast. Display the first message received in the report with SPs scaled into engineering
        // values.
        // 6.2.17.2.c. Display the first three messages observed with their arrival times.
        Stream<GenericPacket> packetStream = busService.readBus(broadcastValidator.getMaximumBroadcastPeriod() * 4,
                                                                "6.2.17.2.c");
        var packets = packetStream
                                  .peek(p -> {
                                      try {
                                          Controller.checkEnding();
                                      } catch (InterruptedException e) {
                                          packetStream.close();
                                      }
                                  })
                                  .peek(p -> {
                                      // 6.2.17.3.a. Fail if no message or message with invalid SP data for any
                                      // broadcast SP indicated as supported by the OBD ECU(s) in DM24.
                                      tableA1Validator.reportNotAvailableSPNs(p, getListener(), "6.2.17.3.a");
                                  })
                                  .peek(p -> {
                                      // 6.2.17.3.c. Fail/warn if any broadcast data is not valid for KOER
                                      // conditions as per Table A-1, Minimum Data Stream Support.
                                      tableA1Validator.reportImplausibleSPNValues(p, getListener(), true, "6.2.17.3.c");
                                  })
                                  .peek(p -> {
                                      // 6.2.17.3.d. Fail/warn per Table A-1 if an expected SPN from the
                                      // DM24 support list is provided by a non-OBD ECU.
                                      tableA1Validator.reportNonObdModuleProvidedSPNs(p, getListener(), "6.2.17.3.d");
                                  })
                                  .peek(p ->
                                  // 6.2.17.3.f. Fail/warn per Table A-1 column, “Action if SPN provided
                                  // but not included in DM24”.
                                  tableA1Validator.reportProvidedButNotSupportedSPNs(p,
                                                                                     getListener(),
                                                                                     "6.2.17.3.f"))
                                  .peek(p -> {
                                      // 6.2.17.4.a. Fail each broadcast SP that was not observed in the broadcast data
                                      // during the 20 seconds of observation.
                                      tableA1Validator.reportPacketIfNotReported(p,
                                                                                 getListener(),
                                                                                 false);
                                  })
                                  .collect(Collectors.toList());

        // Notify the user if there's another ECU on the bus using our address
        if (getJ1939().getBus().imposterDetected()) {
            String msg = "6.2.17 - Unexpected Service Tool Message from SA 0xF9 observed. Test results uncertain. False failures are possible";
            addWarning(msg);
            displayInstructionAndWait(msg, "Second device using SA 0xF9", ERROR);
        }
        // Check the Broadcast Period of the received packets1
        // Map of PGN to (Map of Source Address to List of Packets)
        Map<Integer, Map<Integer, List<GenericPacket>>> foundPackets = broadcastValidator.buildPGNPacketsMap(packets);

        List<Integer> supportedSPNs = getDataRepository().getObdModules()
                                                         .stream()
                                                         .flatMap(m -> m.getFilteredDataStreamSPNs().stream())
                                                         .map(SupportedSPN::getSpn)
                                                         .collect(Collectors.toList());

        // 6.2.17.3.b - Fail if any parameter is not broadcast within ±10% of the specified broadcast period
        broadcastValidator.reportBroadcastPeriod(foundPackets,
                                                 supportedSPNs,
                                                 getListener(),
                                                 getPartNumber(),
                                                 getStepNumber());

        // 6.2.17.3.e. Fail/warn per Table A-1, if two or more ECUs provide an SPN listed.
        tableA1Validator.reportDuplicateSPNs(packets, getListener(), "6.2.17.3.e");

        List<GenericPacket> onRequestPackets = new ArrayList<>();
        // Find and report any Supported SPNs which should have been received but weren't
        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {
            int moduleAddress = obdModule.getSourceAddress();

            // Get the SPNs which are supported by the module
            List<Integer> dataStreamSPNs = obdModule.getFilteredDataStreamSPNs()
                                                    .stream()
                                                    .map(SupportedSPN::getSpn)
                                                    .collect(Collectors.toList());

            // Find SPNs sent as Not Available and those that should have been sent
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
                                                                                            "6.2.17.5.a");

            List<Integer> requestPGNs = busService.getPGNsForDSRequest(missingSPNs, dataStreamSPNs);

            // Remove the SPNs that were already received
            Set<Integer> receivedSPNs = packets.stream()
                                               .filter(p -> p.getSourceAddress() == moduleAddress)
                                               .flatMap(p -> p.getSpns().stream())
                                               .filter(s -> !s.isNotAvailable())
                                               .map(Spn::getId)
                                               .collect(Collectors.toSet());
            dataStreamSPNs.removeAll(receivedSPNs);

            for (int pgn : requestPGNs) {
                updateProgress("Test 2.17 - Verifying " + Lookup.getAddressName(moduleAddress));
                String spns = j1939DaRepository.findPgnDefinition(pgn)
                                               .getSpnDefinitions()
                                               .stream()
                                               .map(SpnDefinition::getSpnId)
                                               .filter(s -> missingSPNs.contains(s) || dataStreamSPNs.contains(s))
                                               .sorted()
                                               .map(Object::toString)
                                               .collect(Collectors.joining(", "));

                // 6.2.17.5.a. DS messages to the ECU(s) that indicated support in DM24 for upon request SPs and
                // broadcast SPs that were not observed in step 1 from both lists. [SPs that were observed as ‘not
                // available’ in broadcast data from a particular ECU need not be queried — the data evaluation from the
                // broadcast data suffices. Where a PG from a particular OBD ECU contains more than one SP (to be
                // evaluated), that PG need only be queried one time].
                List<GenericPacket> dsResponse = busService.dsRequest(pgn, moduleAddress, spns)
                                                           .peek(p ->
                                                           // 6.2.17.6.b. Fail/warn if any data received (that is
                                                           // supported in DM24 by the subject OBD ECU) is not valid for
                                                           // KOEO conditions as per section A.1Table A-1, Minimum Data
                                                           // Stream Support.
                                                           tableA1Validator.reportImplausibleSPNValues(p,
                                                                                                       getListener(),
                                                                                                       true,
                                                                                                       "6.2.17.6.b"))
                                                           .peek(p ->
                                                           // 6.2.17.6.c. Fail/warn per Table A-1, if an expected SPN
                                                           // from the DM24 support list from an OBD ECU is provided by
                                                           // a non-OBD ECU.
                                                           tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                                                                                           getListener(),
                                                                                                           "6.2.17.6.c"))
                                                           .collect(Collectors.toList());
                onRequestPackets.addAll(dsResponse);

                // 6.2.17.6.a Fail/Warn if no response or NACK received for any SP indicated as supported by the OBD ECU
                // in DM24:
                // 6.2.17.6.a.i Warn for received NACKs, when on request SPN(s) are (only) received as broadcast data.
                // 6.2.17.6.a.ii Fail, on-request SPN(s) that are not received (at all) when requested.
                List<String> notAvailableSPNs = broadcastValidator.collectAndReportNotAvailableSPNs(supportedSPNs,
                                                                                                    pgn,
                                                                                                    dsResponse,
                                                                                                    moduleAddress,
                                                                                                    getListener(),
                                                                                                    getPartNumber(),
                                                                                                    getStepNumber(),
                                                                                                    "6.2.17.6.a");

                if (!notAvailableSPNs.isEmpty()) {
                    // 6.2.17.5.b. If no response/no valid data for any SPN requested in 6.2.16.3.a, send global message
                    // to request that SPN(s). Re-request the missing SPNs globally
                    String globalMessage = "PGN " + pgn + " for SPNs " + String.join(", ", notAvailableSPNs);
                    List<GenericPacket> globalPackets = busService.globalRequest(pgn, globalMessage)
                                                                  .peek(p -> tableA1Validator.reportNotAvailableSPNs(p,
                                                                                                                     getListener(),
                                                                                                                     "6.2.17.5.b"))
                                                                  .peek(p ->
                                                                  // 6.2.17.6.b. Fail/warn if any data received (that is
                                                                  // supported in DM24 by the subject OBD ECU) is not
                                                                  // valid for KOER conditions as per Table A-1, Minimum
                                                                  // Data Stream Support
                                                                  tableA1Validator.reportImplausibleSPNValues(p,
                                                                                                              getListener(),
                                                                                                              true,
                                                                                                              "6.2.17.6.b"))
                                                                  .peek(p ->
                                                                  // 6.2.17.6.e. Fail/warn per Table A-1 column, “Action
                                                                  // if SPN provided but not included in DM24”
                                                                  tableA1Validator.reportProvidedButNotSupportedSPNs(p,
                                                                                                                     getListener(),
                                                                                                                     "6.2.17.6.e"))
                                                                  .collect(Collectors.toList());

                    onRequestPackets.addAll(globalPackets);
                }
            }// end pgn

            // 6.2.17.7 Actions4 for MY2022+ Diesel Engines
            if (getEngineModelYear() >= 2022 && getFuelType().isCompressionIgnition()) {
                if (obdModule.supportsSpn(12675)) {
                    // 6.2.17.7 - 6.2.17.10
                    testSp12675(obdModule);
                }
                if (obdModule.supportsSpn(12730)) {
                    // 6.2.17.11 - 6.2.17.14
                    testSp12730(obdModule);
                }
                if (obdModule.supportsSpn(12691)) {
                    // 6.2.17.15 - 6.2.17.18
                    testSp12691(obdModule);
                }
                if (obdModule.supportsSpn(12797)) {
                    // 6.2.17.19 - 6.2.17.22
                    testSp12797(obdModule);
                }
                if (obdModule.supportsSpn(12783)) {
                    // 6.2.17.23 - 6.2.17.26
                    testSp12783(obdModule);
                }
            }
        }// end obdModule

        // 6.2.17.6.d. Fail/warn per Table A-1, if two or more ECUs provide an SPN listed in Table A-1.
        tableA1Validator.reportDuplicateSPNs(onRequestPackets,
                                             getListener(),
                                             "6.2.17.6.d");

    }

    private void testSp12783(OBDModuleInformation module) {
        // 6.2.17.23 Actions12 for MY2022+ Plug-in HEV DRIVES
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for PG 64244 Hybrid Charge Depleting or Increasing Operation Lifetime Hours
        int[] pgns = { GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG };
        var ghgChgDepletingLifeTimePackets = requestPackets(module.getSourceAddress(), pgns).stream()
                                                                                            .map(p -> (GhgActiveTechnologyPacket) p)
                                                                                            .collect(Collectors.toList())
                                                                                            .stream()
                                                                                            // 6.2.17.23.b.
                                                                                            // Record
                                                                                            // each
                                                                                            // value
                                                                                            // for
                                                                                            // use
                                                                                            // in
                                                                                            // Part
                                                                                            // 12.
                                                                                            .peek(this::save)
                                                                                            .collect(Collectors.toList());

        if (ghgChgDepletingLifeTimePackets.isEmpty()) {
            // 6.2.17.24.a. Fail PG query where no response was received
            addFailure("6.2.17.24.a - No response was received from "
                    + module.getModuleName());
        } else {
            ghgChgDepletingLifeTimePackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.2.17.24.c - Message from part 1 is missing so verification of values skipped");
                }
                packet.getSpns()
                      .forEach(spn -> {
                          // 6.2.17.24.b - Fail PG query where any accumulator value
                          // received is greater than FAFFFFFFh.
                          validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.24.b");

                          // 6.2.17.24.c Fail all values where the corresponding value received is part 1 is
                          // greater than the part 2 value
                          if (spn.hasValue() && partOnePacket != null) {
                              var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                              if (partOneValue > spn.getValue()) {
                                  addFailure("6.2.17.24.c - Value received from " + module.getModuleName() + " for "
                                          + spn
                                          + "  in part 1 was greater than part 2 value");
                              }
                          }

                      });
            });
        }
        int[] pgns1 = { GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR, GHG_STORED_HYBRID_CHG_DEPLETING_100_HR };

        // 6.2.17.25 Actions13 for MY2022+ Plug-in HEV DRIVES
        // 6.2.17.25.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for Active 100hr Charge Depleting times and
        // Stored 100hr PSA Charge Depleting PGs:
        // PG PG Label
        // 64246 Hybrid Charge Depleting or Increasing Operation Active 100 Hours - PG Acronym HCDIOA
        // 64245 Hybrid Charge Depleting or Increasing Operation Stored 100 Hours - - PG Acronym HCDIOS
        var hybridChargeOpsPackets = requestPackets(module.getSourceAddress(), pgns1).stream()
                                                                                     .map(p -> (GhgActiveTechnologyPacket) p)
                                                                                     .collect(Collectors.toList())
                                                                                     .stream()
                                                                                     // 6.2.17.25.b. Record
                                                                                     // each
                                                                                     // value for use in Part
                                                                                     // 12.
                                                                                     .peek(this::save)
                                                                                     .collect(Collectors.toList());

        // 6.2.17.25.c - List data received in a table using lifetime, stored 100 hr, active 100hr for columns, and
        // categories for rows.
        getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(ghgChgDepletingLifeTimePackets.stream(),
                                                                              hybridChargeOpsPackets.stream())
                                                                      .collect(Collectors.toList())));
        if (hybridChargeOpsPackets.isEmpty()) {
            // 6.2.17.26.a - For MY2024+ Plug-in HEV DRIVES, Fail each PG query where
            // no response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.2.17.26.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.2.17.26.b - For MY2022-23 Plug-in HEV DRIVES, Warn each PG query,
            // where no response was received
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.2.17.26.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            hybridChargeOpsPackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.2.17.26.d - Message from part 1 is missing so verification of values skipped");
                }
                packet.getSpns()
                      .forEach(spn -> {
                          /// 6.2.17.26.c - Fail each PG query where any active
                          /// technology label or accumulator value
                          // received is greater than FAFFh, respectively.
                          validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.26.c");
                          if (spn.hasValue() && partOnePacket != null) {
                              var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                              // 6.2.17.26.d - Fail all values where the corresponding value received in part 1
                              // is greater than the part 2 value
                              if (partOneValue > spn.getValue())
                                  addFailure("6.2.17.26.d - Value received from " + module.getModuleName()
                                          + " for " + spn
                                          + "  in part 1 was greater than part 2 value");
                          }
                      });
            });
        }
    }

    private void testSp12797(OBDModuleInformation module) {
        // 6.2.17.19 Actions10 for MY2022+ HEV and BEV drives
        // 62.17.19.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for 64241 PSA Times
        // Lifetime Hours
        int[] pgns = { GHG_TRACKING_LIFETIME_HYBRID_PG };
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(), pgns).stream()
                                                                                .map(p -> (GhgActiveTechnologyPacket) p)
                                                                                .collect(Collectors.toList())
                                                                                .stream()
                                                                                // 6.2.17.19.b.
                                                                                // Record each value for
                                                                                // use in Part 12.
                                                                                .peek(this::save)
                                                                                .collect(Collectors.toList());

        if (ghgTrackingPackets.isEmpty()) {
            // 6.2.17.20.a - Fail PG query where no response was received.
            addWarning("6.2.17.20.a - No response was received from "
                    + module.getModuleName());
        } else {
            ghgTrackingPackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.2.17.20.c - Message from part 1 is missing so verification of values skipped");
                }
                packet.getSpns()
                      .forEach(spn -> {
                          // 6.2.17.20.b - Fail PG query where any accumulator value
                          // received is greater than FAFFFFFFh.
                          validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.20.b");
                          // 6.2.17.20.c - Fail all values where the corresponding value received in part 1 is greater
                          // than the part 2 value
                          if (spn.hasValue() && partOnePacket != null) {
                              var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                              if (partOneValue > spn.getValue()) {
                                  addFailure("6.2.17.20.c - Value received from " + module.getModuleName() + " for "
                                          + spn
                                          + "  in part 1 was greater than part 2 value");
                              }
                          }

                      });
            });
        }
        int[] pgns1 = { GHG_STORED_HYBRID_100_HR, GHG_ACTIVE_HYBRID_100_HR };

        // 6.2.17.21 Actions11 for MY2022+ HEV and BEV drives
        // 6.2.17.21.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for Active 100hr
        // PSA Times and Stored 100hr PSA Times PGs:
        // PG PG Label
        // 64242 PSA Times Stored 100 Hours - PG Acronym PSATS
        // 64243 PSA Times Active 100 Hours - PG Acronym PSATA
        // 6.2.17.21.c - List data received in a table using lifetime, stored 100 hr, active 100hr
        // for columns, and categories for rows.
        var ghgPackets = requestPackets(module.getSourceAddress(), pgns1).stream()
                                                                         .map(p -> (GhgActiveTechnologyPacket) p)
                                                                         .collect(Collectors.toList())
                                                                         .stream()
                                                                         // 6.2.17.21.b. -
                                                                         // Record each
                                                                         // value for use
                                                                         // in Part 12.
                                                                         .peek(this::save)
                                                                         .collect(Collectors.toList());

        if (!ghgTrackingPackets.isEmpty() || !ghgPackets.isEmpty()) {
            // 6.2.17.19.c - List data received in a table using lifetime, stored 100 hr, active 100hr for columns, and
            // categories for rows.
            // 6.2.17.21.c. List data received in a table using lifetime, stored 100 hr, active 100 hr for columns and
            // categories for rows.
            getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(ghgTrackingPackets.stream(),
                                                                                  ghgPackets.stream())
                                                                          .collect(
                                                                                   Collectors.toList())));
        }
        if (ghgPackets.isEmpty()) {
            // 6.2.17.22.a - For MY2024+ HEV and BEV drives, Fail each PG query where no
            // response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.2.17.22.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.2.17.22.b - For MY2022-23 HEV and BEV drives, Warn each PG query, where no
            // response was received.
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.2.17.22.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            ghgPackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.2.17.22.d - Message from part 1 is missing so verification of values skipped");
                }
                packet.getSpns()
                      .forEach(spn -> {
                          // 6.2.17.22.c - Fail each PG query where any accumulator
                          // value received is greater than FAFFh.
                          validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.22.c");

                          // 6.2.17.22.d - Fail all values where the corresponding value received in part 1
                          // is greater than the part 2 values. (where supported)
                          if (spn.hasValue() && partOnePacket != null) {
                              var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                              if (partOneValue > spn.getValue()) {
                                  addFailure("6.2.17.22.d - Value received from " + module.getModuleName()
                                          + " for " + spn
                                          + "  in part 1 was greater than part 2 value");
                              }
                          }
                      });
            });
        }
    }

    private void testSp12730(OBDModuleInformation module) {
        // 6.2.17.11 Actions6 for all MY2022+ Engines
        // 6.2.17.11.a - DS request messages to ECU that indicated support in DM24 for upon request SP 12730 (GHG
        // Tracking Lifetime Engine Run Time) for PG 64252 GHG Tracking Lifetime Array Data.
        int[] pgns = { GHG_TRACKING_LIFETIME_PG };
        var ghgTrackingLifetimePackets = requestPackets(module.getSourceAddress(), pgns).stream()
                                                                                        .map(p -> (GhgActiveTechnologyPacket) p)
                                                                                        .collect(Collectors.toList())
                                                                                        .stream()
                                                                                        // 6.2.17.9.b - Record each
                                                                                        // value
                                                                                        // for use in Part 12.
                                                                                        .peek(this::save)
                                                                                        .collect(Collectors.toList());

        if (ghgTrackingLifetimePackets.isEmpty()) {
            // 6.2.17.12.a. Fail PG query where no response was received
            addFailure("6.2.17.12.a - No response was received from "
                    + module.getModuleName());
        } else {
            ghgTrackingLifetimePackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.1.17.12.c - Message from part 1 is missing so verification of values skipped");
                }
                packet.getSpns()
                      .forEach(spn -> {
                          // 6.2.17.12.b. Fail PG query where any bin value received is greater than FAFFh.
                          validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.12.b");
                          // 6.2.17.12.c - Fail all values where the corresponding value received in part 1 is
                          // greater than the part 2 value (where supported)
                          if (spn.hasValue() && partOnePacket != null) {
                              var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                              if (partOneValue > spn.getValue()) {
                                  addFailure("6.2.17.12.c - Value received from " + module.getModuleName()
                                          + " for " + spn
                                          + " in part 1 was greater than part 2 value");
                              }
                          }
                      });
            });
        }
        int[] pgns1 = { GHG_ACTIVE_100_HR, GHG_STORED_100_HR };

        // 6.2.17.13 Actions7 for MY2022+ Engines
        // 6.2.17.13.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12730 (GHG Tracking Lifetime Engine Run Time) for each 100hr GHG tracking PG
        // PG PG Label
        // 64254 GHG Tracking Active 100 Hour Array Data
        // 64253 GHG Tracking Stored 100 Hour Array Data
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(), pgns1).stream()
                                                                                 .map(p -> (GhgActiveTechnologyPacket) p)
                                                                                 .collect(Collectors.toList())
                                                                                 .stream()
                                                                                 // 6.2.17.11.b.
                                                                                 // Record
                                                                                 // each value
                                                                                 // for use
                                                                                 // in Part 12.
                                                                                 .peek(this::save)
                                                                                 .collect(Collectors.toList());

        if (!ghgTrackingLifetimePackets.isEmpty() || !ghgTrackingPackets.isEmpty()) {
            // 6.2.17.13.c. List data received in a table using lifetime, stored 100 hr,
            // active 100hr for columns, and categories for rows.
            getListener().onResult(ghgTrackingModule.formatTrackingTable(Stream.concat(ghgTrackingLifetimePackets.stream(),
                                                                                       ghgTrackingPackets.stream())
                                                                               .collect(Collectors.toList())));
        }

        if (ghgTrackingPackets.isEmpty()) {
            // 6.2.17.14.a. For all MY2024+ engines, Fail each PG query where no response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.2.17.14.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.2.17.14.b. For MY2022-23 engines, Warn each PG query, where no response was received
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.2.17.14.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            ghgTrackingPackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.2.17.14.d - Message from part 1 is missing so verification of values skipped");
                }
                packet.getSpns()
                      .forEach(spn -> {
                          validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.14.c");
                          // 6.2.17.14.d - Fail all values where the corresponding value received in part 1 is
                          // greater than the part 2 value
                          if (spn.hasValue() && partOnePacket != null) {
                              var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                              if (partOneValue > spn.getValue()) {
                                  addFailure("6.2.17.14.d - Value received from " + module.getModuleName()
                                          + " for " + spn
                                          + "  in part 1 was greater than part 2 value");
                              }
                          }
                      });
            });
        }

    }

    private void testSp12691(OBDModuleInformation module) {
        // 6.2.17.15 Actions8 for all MY2022+ Engines
        // 6.2.17.15.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for
        // PG 64257 Green House Gas Lifetime Active Technology Tracking.
        var lifetimeGhgPackets = requestPackets(module.getSourceAddress(),
                                                GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG)
                                                                                     .stream()
                                                                                     .map(GenericPacket::getPacket)
                                                                                     .map(GhgLifetimeActiveTechnologyPacket::new)
                                                                                     // 6.2.17.15.b. Record
                                                                                     // each value for use
                                                                                     // in Part 12.
                                                                                     .peek(this::save)
                                                                                     .collect(Collectors.toList());

        if (lifetimeGhgPackets.isEmpty()) {
            // 6.2.17.16.a. Warn PG query where no response was received.
            addWarning("6.2.17.16.a - No response was received from "
                    + module.getModuleName());
        } else {
            lifetimeGhgPackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.2.17.16.c - Message from part 1 is missing so verification of values skipped");
                }
                packet.getSpns()
                      .forEach(spn -> {
                          // 6.2.17.16.b. Fail any accumulator value received that is greater
                          // than FAFFFFFFh.
                          validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.16.b");
                          // 6.2.17.16.c. Fail all values where the corresponding value received in part 1 is
                          // greater than the part 2 value.
                          if (spn.hasValue() && partOnePacket != null) {
                              var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                              if (partOneValue > spn.getValue()) {
                                  addFailure("6.2.17.16.c - Value received from " + module.getModuleName()
                                          + " for " + spn
                                          + "  in part 1 was greater than part 2 value");
                              }
                          }
                      });
            });
        }

        // 6.2.17.17 Actions9 for MY2022+ Engines
        // 6.2.17.17.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for Active 100hr
        // Active technology PG, followed by DS request message to ECU for
        // Stored 100 hr Active Technology PG.
        // PG PG Label
        // 64256 Green House Gas Active 100 Hour Active Technology Tracking - PG Acronym GHGTTA
        // 64255 Green House Gas Stored 100 Hour Active Technology Tracking - PG Acronym GHGTTS
        var ghg100HrPackets = requestPackets(module.getSourceAddress(),
                                             GHG_ACTIVE_GREEN_HOUSE_100_HR,
                                             GHG_STORED_GREEN_HOUSE_100_HR)
                                                                           .stream()
                                                                           .map(GenericPacket::getPacket)
                                                                           .map(GhgActiveTechnologyPacket::new)
                                                                           // 6.2.17.17.b. Record
                                                                           // each value for use
                                                                           // in Part 12.
                                                                           .peek(this::save)
                                                                           .collect(Collectors.toList());

        // 6.2.17.17.c. List data received in a table using lifetime, stored 100 hr,
        // active 100hr for columns, and categories for rows.
        getListener().onResult(ghgTrackingModule.formatTechTable(Stream.concat(lifetimeGhgPackets.stream(),
                                                                               ghg100HrPackets.stream())
                                                                       .collect(Collectors.toList())));

        if (ghg100HrPackets.isEmpty()) {
            if (getEngineModelYear() >= 2024) {
                // 6.2.17.18.a. For all MY2024+ engines, Warn each PG query where no response was received.
                addFailure("6.2.17.18.a - No response was received from "
                        + module.getModuleName());
            }
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                // 6.2.17.18.b. For MY2022-23 engines, Warn each PG query, where no response was received
                addWarning("6.2.17.18.b - No response was received from "
                        + module.getModuleName());
            }
        } else {
            ghg100HrPackets.forEach(packet -> {
                packet.getSpns().forEach(spn -> {
                    // 6.2.17.18.c. Fail PG query where any bin value received is greater than FAFFh.
                    validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.18.c");

                });
            });
        }

        var lifetimeIndexes = lifetimeGhgPackets.stream()
                                                .flatMap(ghgPacket -> ghgPacket.getActiveTechnologies().stream())
                                                .map(ActiveTechnology::getIndex)
                                                .collect(Collectors.toList());

        var activeIndexes = ghg100HrPackets.stream()
                                           .filter(p -> {
                                               return p.getPacket().getPgn() == GHG_ACTIVE_GREEN_HOUSE_100_HR;
                                           })
                                           .flatMap(ghgPacket -> ghgPacket.getActiveTechnologies().stream())
                                           .map(ActiveTechnology::getIndex)
                                           .collect(Collectors.toList());
        var storedIndexes = ghg100HrPackets.stream()
                                           .filter(genericPacket -> {
                                               return genericPacket.getPgnDefinition()
                                                                   .getId() == GHG_STORED_GREEN_HOUSE_100_HR;
                                           })
                                           .flatMap(ghgPacket -> ghgPacket.getActiveTechnologies().stream())
                                           .map(ActiveTechnology::getIndex)
                                           .collect(Collectors.toList());

        // 6.2.17.18.e. Fail each response where the number of labels received are not
        // the same as the number of labels received for the lifetime technology response.
        if (lifetimeIndexes.size() != activeIndexes.size()) {
            addFailure("6.2.17.18.e - Number of active labels received differs from the number of lifetime labels");
        }
        if (lifetimeIndexes.size() != storedIndexes.size()) {
            addFailure("6.2.17.18.e - Number of stored labels received differs from the number of lifetime labels");
        }

        // 6.2.17.18.f. Fail each response where the set of labels received is not a
        // subset of the set of labels received for the lifetime’ active technology
        // response.
        if (!lifetimeIndexes.containsAll(activeIndexes)) {
            addFailure("6.2.17.18.f - Active labels received is not a subset of lifetime labels");
        }
        if (!lifetimeIndexes.containsAll(storedIndexes)) {
            addFailure("6.2.17.18.f - Stored labels received is not a subset of lifetime labels");
        }
    }

    private void testSp12675(OBDModuleInformation module) {
        int[] nOxLifeTimeSps = Stream.of(new int[][] { NOx_LIFETIME_PGs, NOx_LIFETIME_ACTIVITY_PGs })
                                     .flatMapToInt(x -> IntStream.of(x))
                                     .filter(x -> x != 0)
                                     .toArray();
        // 6.2.17.7.a. DS request messages to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine
        var nOxPackets = requestPackets(module.getSourceAddress(), nOxLifeTimeSps).stream()
                                                                                  // 6.2.17.7.b. Record
                                                                                  // each value for use
                                                                                  // in Part 12.
                                                                                  .peek(this::save)
                                                                                  .collect(Collectors.toList());

        if (nOxPackets.isEmpty()) {
            // 6.2.17.8.a. Fail each PG query where no response was received.
            addFailure("6.2.17.8.a - No response was received from "
                    + module.getModuleName());
        } else {
            // 6.2.17.7.c. List data received in a table using bin numbers for rows.
            getListener().onResult(nOxBinningModule.format(nOxPackets));
            nOxPackets.forEach(packet -> {
                var partOnePacket = get(packet.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                if (partOnePacket == null) {
                    addInfo("6.2.17.8.c - Message from part 1 for PG " + packet.getPgnDefinition()
                                                                               .getId()
                            + " is missing so verification of values skipped");
                }
                packet.getSpns().forEach(spn -> {
                    // 6.2.17.8.b. Fail each PG query where any bin value received
                    // is greater than FAFFFFFFh.
                    validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.8.b");

                    // 6.2.17.8.c Fail all values where the corresponding value received in part 1 is greater
                    // than the part 2 value
                    if (spn.hasValue() && partOnePacket != null) {
                        var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                        if (partOneValue > spn.getValue()) {
                            addFailure("6.2.17.8.c - Value received from " + module.getModuleName()
                                    + " for " + spn + " in part 1 was greater than part 2 value");
                        }
                    }
                });

            });

        }

        // 6.2.17.9.a - DS request message to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1
        // - Total) for each active 100hr NOx binning PG, followed by each Stored 100 hr PG
        // Label
        var nOx100HourPackets = requestPackets(module.getSourceAddress(),
                                               CollectionUtils.join(NOx_TRACKING_ACTIVE_100_HOURS_PGs,
                                                                    NOx_TRACKING_STORED_100_HOURS_PGs)).stream()
                                                                                                       // 6.2.17.9.b
                                                                                                       // -
                                                                                                       // Record
                                                                                                       // each
                                                                                                       // value
                                                                                                       // for
                                                                                                       // use
                                                                                                       // in
                                                                                                       // Part
                                                                                                       // 12.
                                                                                                       .peek(this::save)
                                                                                                       .collect(Collectors.toList());

        // 6.2.17.9.c - List data received in a table using bin numbers for rows.
        if (nOx100HourPackets.isEmpty()) {
            // 6.2.17.10.a. For all MY2024+ Diesel engines, Fail each PG query where no response was received.
            if (getEngineModelYear() >= 2024) {
                addFailure("6.2.17.10.a - No response was received from "
                        + module.getModuleName());
            }
            // 6.2.17.10.b. For all MY2022-23 Diesel engines, Warn each PG query where no response was received.
            if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                addWarning("6.2.17.10.b - No response was received from "
                        + module.getModuleName());
            }

        } else {
            // 6.2.17.9.c - List data received in a table using bin numbers for rows.
            getListener().onResult(nOxBinningModule.format(nOx100HourPackets));

            nOx100HourPackets.forEach(packet -> {
                List<Integer> active100HrSps = Arrays.stream(NOx_TRACKING_ACTIVE_100_HOURS_PGs)
                                                     .boxed()
                                                     .collect(Collectors.toList());
                packet.getSpns().forEach(spn -> {
                    // 6.2.17.10.c. Fail each PG query where any bin value received is greater than FAFFh. (Use
                    // FAFFFFFFh for NOx values)
                    validateSpnValueGreaterThanFaBasedSlotLength(module, spn, FAIL, "6.2.17.9.c");
                    // 6.2.17.10.e. Warn each active 100 hr array value that is greater than zero. (where supported)
                    if (active100HrSps.contains(packet.getPgnDefinition().getId()) && spn.getValue() > 0) {
                        // 6.2.17.10.d. Fail each active 100 hr array value that is greater than zero. (where
                        // supported)
                        addFailure("6.2.17.10.d - Active 100 hr array value received is greater than zero (where supported) from "
                                + module.getModuleName() + " for " + spn);
                    }

                });
            });
        }
    }
}
