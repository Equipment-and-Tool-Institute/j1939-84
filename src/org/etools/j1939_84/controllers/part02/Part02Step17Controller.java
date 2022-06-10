/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

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
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_ACTIVITY_SPs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_LIFETIME_SPs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_ACTIVE_100_HOURS_SPs;
import static org.etools.j1939tools.modules.NOxBinningModule.NOx_TRACKING_STORED_100_HOURS_SPs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
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
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.j1939.packets.GenericPacket;
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

        // 6.2.17.1.a Create a list of expected SPNs and PGNs from the DM24 response, where the data stream support bit
        // defined in SAE J1939-73 5.7.24 is 0. Omit the following SPNs (588, 976, 1213, 1220, 12675, 12691, 12730,
        // 12783, 12797) which are included in the list. Omit any remaining SPNs that map to multiple PGs.
        List<Integer> supportedSPNs = getDataRepository().getObdModules()
                                                         .stream()
                                                         // Display the completed list noting those omitted SPs,
                                                         // supported SPs as ‘broadcast’ or ‘upon request’, and
                                                         // additions from Table A-1.
                                                         .peek(module -> tableA1Validator.reportMessages(getListener(),
                                                                                                         module))
                                                         .flatMap(m -> m.getFilteredDataStreamSPNs().stream())
                                                         .map(SupportedSPN::getSpn)
                                                         .collect(Collectors.toList());

        // 6.2.17.1.b. Gather broadcast data for all SPNs that are supported for data stream in the OBD ECU responses
        // and the added SPNs from Table A-1, using the SPN list from Part 1. This shall include the both SPs that are
        // expected to be queried with DS queries (in step 5 for SPs supported in DM24) and SPs that that are expected
        // without queries.
        // 6.2.17.1.c. Gather/timestamp each parameter at least three times to be able to verify frequency of broadcast
        // x4 to ensure all necessary messages have been received
        Stream<GenericPacket> packetStream = busService.readBus(broadcastValidator.getMaximumBroadcastPeriod() * 4,
                                                                "6.2.17.1.c");
        var packets = packetStream
                                  .peek(p -> {
                                      try {
                                          Controller.checkEnding();
                                      } catch (InterruptedException e) {
                                          packetStream.close();
                                      }
                                  })
                                  .peek(p -> {
                                      // 6.2.17.2.a. Fail if no response/no valid data for any broadcast SP indicated as
                                      // supported in DM24.
                                      tableA1Validator.reportNotAvailableSPNs(p, getListener(), "6.2.17.2.a");
                                  })
                                  .peek(p -> {
                                      // 6.2.17.2.b. Fail/warn if any broadcast data is not valid for KOER
                                      // conditions as per Table A-1, Minimum Data Stream Support.
                                      tableA1Validator.reportImplausibleSPNValues(p, getListener(), true, "6.2.17.2.b");
                                  })
                                  .peek(p -> {
                                      // 6.2.17.2.c. Fail/warn per Table A-1 if an expected SPN from the
                                      // DM24 support list is provided by a non-OBD ECU.
                                      tableA1Validator.reportNonObdModuleProvidedSPNs(p, getListener(), "6.2.17.2.c");
                                  })
                                  .peek(p -> {
                                      // 6.2.17.3.a. Identify SPNs provided in the data stream that are listed
                                      // in Table A-1 but not supported by any OBD ECU in its DM24 response.
                                      // 6.2.17.4.a. Fail/warn per Table A-1 column, “Action if SPN provided
                                      // but not included in DM24”.
                                      tableA1Validator.reportProvidedButNotSupportedSPNs(p,
                                                                                         getListener(),
                                                                                         "6.2.17.4.a");
                                  })
                                  .collect(Collectors.toList());
        List<GenericPacket> onRequestPackets = new ArrayList<>();

        // 6.2.17.2.d. Fail/warn per Table A-1, if two or more ECUs provide an SPN listed.
        tableA1Validator.reportDuplicateSPNs(packets, getListener(), "6.2.17.2.d");

        // Check the Broadcast Period of the received packets1
        // Map of PGN to (Map of Source Address to List of Packets)
        Map<Integer, Map<Integer, List<GenericPacket>>> foundPackets = broadcastValidator.buildPGNPacketsMap(packets);

        // 6.2.17.6.c - Fail if any parameter in a fixed period message, upon request, is not broadcast within ± 10% of
        // the specified broadcast period
        // 6.2.17.6.d - Fail if any parameter in a variable period broadcast message exceeds 110% of its recommended
        // broadcast period. [see foot 26].
        broadcastValidator.reportBroadcastPeriod(foundPackets,
                                                 supportedSPNs,
                                                 getListener(),
                                                 getPartNumber(),
                                                 getStepNumber());

        // Find and report any Supported SPNs which should have been received but weren't
        for (OBDModuleInformation obdModule : getDataRepository().getObdModules()) {
            int moduleAddress = obdModule.getSourceAddress();

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

                // 6.2.17.5.a. DS messages to ECU that indicated support in DM24 for upon request SPs and SPs added from
                // Table A-1, regardless of whether the SPs were or were not observed in step 1. [Where a PG contains
                // more than one SP (to be queried), that PG need only be queried one time].
                List<GenericPacket> dsResponse = busService.dsRequest(pgn, moduleAddress, spns)
                                                           .peek(p ->
                                                           // 6.2.17.6.a. Ignore NACK received for any SP added from
                                                           // table A-1, whose PG does not contain any SPs listed as
                                                           // supported in DM24.
                                                           // 6.2.17.6.b. Fail if no response or NACK for any SP
                                                           // indicated as supported by the OBD ECU in DM24. Downgrade
                                                           // the failure to a warning where an upon request SPN was
                                                           // received in 6.1.17.1, and the request was not replied to
                                                           // with a NACK
                                                           tableA1Validator.reportNotAvailableSPNs(p,
                                                                                                   getListener(),
                                                                                                   "6.2.17.6.b"))
                                                           .peek(p ->
                                                           // 6.2.17.6.e. Fail/warn if any data received (that is
                                                           // supported in DM24 by the subject OBD ECU) is not valid for
                                                           // KOEO conditions as per section A.1Table A-1, Minimum Data
                                                           // Stream Support.
                                                           tableA1Validator.reportImplausibleSPNValues(p,
                                                                                                       getListener(),
                                                                                                       true,
                                                                                                       "6.2.17.6.e"))
                                                           .collect(Collectors.toList());
                onRequestPackets.addAll(dsResponse);

                List<String> notAvailableSPNs = broadcastValidator.collectAndReportNotAvailableSPNs(supportedSPNs,
                                                                                                    pgn,
                                                                                                    dsResponse,
                                                                                                    moduleAddress,
                                                                                                    getListener(),
                                                                                                    getPartNumber(),
                                                                                                    getStepNumber(),
                                                                                                    "6.2.17.5.b");

                if (!notAvailableSPNs.isEmpty()) {
                    // 6.2.17.5.b. If no response/no valid data for any SPN requested in 6.2.16.3.a, send global message
                    // to request that SPN(s). Re-request the missing SPNs globally
                    String globalMessage = "PGN " + pgn + " for SPNs " + String.join(", ", notAvailableSPNs);
                    if (!j1939DaRepository.findPgnDefinition(pgn).isOnRequest()) {
                        // 6.2.17.5.b. Warn when global request was required for “broadcast” SPN
                        addWarning("6.2.17.5.b - " + "Global request was required for PGN " + pgn
                                + " for broadcast SPNs " +
                                String.join(", ", notAvailableSPNs));
                    }
                    List<GenericPacket> globalPackets = busService.globalRequest(pgn, globalMessage)
                                                                  .peek(p ->
                                                                  // 6.2.17.6.a. Ignore NACK received for any SP added
                                                                  // from table A-1, whose PG does not contain any SPs
                                                                  // listed as supported in DM24.
                                                                  // 6.2.17.6.b Fail if no response or NACK for any SP
                                                                  // indicated as supported by the OBD ECU in DM24.
                                                                  // Downgrade the failure to a warning where an upon
                                                                  // request SPN was received in 6.1.17.1, and the
                                                                  // request was not replied to with a NACK
                                                                  tableA1Validator.reportNotAvailableSPNs(p,
                                                                                                          getListener(),
                                                                                                          "6.2.17.6.b"))
                                                                  .peek(p ->
                                                                  // 6.2.17.6.e. Fail/warn if any data received (that is
                                                                  // supported in DM24 by the subject OBD ECU) is not
                                                                  // valid for KOER conditions as per Table A-1, Minimum
                                                                  // Data Stream Support
                                                                  tableA1Validator.reportImplausibleSPNValues(p,
                                                                                                              getListener(),
                                                                                                              true,
                                                                                                              "6.2.17.6.b"))
                                                                  .collect(Collectors.toList());

                    // 6.2.17.6.e. Fail if no response/no valid data for any upon request
                    // SPN indicated as supported in DM24, per Table A-1.
                    broadcastValidator.collectAndReportNotAvailableSPNs(supportedSPNs,
                                                                        pgn,
                                                                        globalPackets,
                                                                        null,
                                                                        getListener(),
                                                                        getPartNumber(),
                                                                        getStepNumber(),
                                                                        "6.2.17.6.e");
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

        // 6.2.17.6.g. Fail/warn per Table A-1, if two or more ECUs provide an SPN listed in Table A-1.
        tableA1Validator.reportDuplicateSPNs(onRequestPackets,
                                             getListener(),
                                             "6.2.17.6.g");

        // 6.2.17.6.h. Fail/warn per Table A-1 column, “Action if SPN provided but not included in DM24”
        onRequestPackets.forEach(packet -> tableA1Validator.reportProvidedButNotSupportedSPNs(packet,
                                                                                              getListener(),
                                                                                              "6.2.17.6.h"));
    }

    private void testSp12783(OBDModuleInformation module) {
        // 6.2.17.23 Actions12 for MY2022+ Plug-in HEV DRIVES
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for PG 64244 Hybrid Charge Depleting or Increasing Operation Lifetime Hours
        List<GenericPacket> ghgChgDepletingLifeTimePackets = requestPackets(module.getSourceAddress(),
                                                                            GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG)
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

        GenericPacket packetForPg = haveResponseWithPg(ghgChgDepletingLifeTimePackets,
                                                       GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        if (packetForPg == null) {
            // 6.2.17.24.a. Fail PG query where no response was received
            addFailure("6.2.17.24.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG);
        } else {
            var partOnePacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
            packetForPg.getSpns()
                       .forEach(spn -> {
                           // 6.2.17.24.b - Fail PG query where any accumulator value
                           // received is greater than FAFFFFFFh.
                           if (spn.getRawValue() > 0xFAFFFFFFL) {
                               addFailure("6.2.17.24.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                       + module.getModuleName() + " for " + spn);
                           }
                           // 6.2.17.24.c Fail all values where the corresponding value received is part 1 is
                           // greater than the part 2 value
                           var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                           if (partOneValue > spn.getValue()) {
                               addFailure("6.2.17.24.c - Value received from " + module.getModuleName() + " for " + spn
                                                  + "  in part 1 was greater than part 2 value");
                           }

                       });
        }

        // 6.2.17.25 Actions13 for MY2022+ Plug-in HEV DRIVES
        // 6.2.17.25.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for Active 100hr Charge Depleting times and
        // Stored 100hr PSA Charge Depleting PGs:
        // PG PG Label
        // 64246 Hybrid Charge Depleting or Increasing Operation Active 100 Hours - PG Acronym HCDIOA
        // 64245 Hybrid Charge Depleting or Increasing Operation Stored 100 Hours - - PG Acronym HCDIOS
        var hybridChargeOpsPackets = requestPackets(module.getSourceAddress(),
                                                    GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR,
                                                    GHG_STORED_HYBRID_CHG_DEPLETING_100_HR)
                                                                                           .stream()
                                                                                           // 6.2.17.25.b. Record each
                                                                                           // value for use in Part 12.
                                                                                           .peek(this::save)
                                                                                           .collect(Collectors.toList());

        // 6.2.17.25.c - List data received in a table using lifetime, stored 100 hr, active 100hr for columns, and
        // categories for rows.
        getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(ghgChgDepletingLifeTimePackets.stream(),
                                                                              hybridChargeOpsPackets.stream())
                                                                      .collect(Collectors.toList())));
        for (int pg : List.of(GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR,
                              GHG_STORED_HYBRID_CHG_DEPLETING_100_HR)) {
            GenericPacket hybridPacketForPg = haveResponseWithPg(hybridChargeOpsPackets,
                                                                 pg);
            if (hybridPacketForPg == null) {
                // 6.2.17.26.a - For MY2024+ Plug-in HEV DRIVES, Fail each PG query where
                // no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.2.17.26.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.2.17.26.b - For MY2022-23 Plug-in HEV DRIVES, Warn each PG query,
                // where no response was received
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.2.17.26.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                var partOnePacket = get(hybridPacketForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                hybridPacketForPg.getSpns()
                                 .forEach(spn -> {
                                     /// 6.2.17.26.c - Fail each PG query where any active
                                     /// technology label or accumulator value
                                     // received is greater than FAFFh, respectively.
                                     if (spn.getRawValue() > 0xFAFFL) {
                                         addFailure("6.2.17.26.c - Bin value received is greater than 0xFAFF(h) from "
                                                 + module.getModuleName() + " for " + spn);
                                     }
                                     // 6.2.17.26.d - Fail all values where the corresponding value received in part 1
                                     // is greater than the part 2 value
                                     var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                                     if (partOneValue > spn.getValue()) {
                                         addFailure("6.2.17.26.d - Value received from " + module.getModuleName() + " for " + spn
                                                            + "  in part 1 was greater than part 2 value");
                                     }
                                 });
            }
        }
    }

    private void testSp12797(OBDModuleInformation module) {
        // 6.2.17.19 Actions10 for MY2022+ HEV and BEV drives
        // 62.17.19.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for 64241 PSA Times
        // Lifetime Hours
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                GHG_TRACKING_LIFETIME_HYBRID_PG)
                                                                                .stream()
                                                                                // 6.2.17.19.b.
                                                                                // Record each value for
                                                                                // use in Part 12.
                                                                                .peek(this::save)
                                                                                .collect(Collectors.toList());

        GenericPacket packetForPg = haveResponseWithPg(ghgTrackingPackets, GHG_TRACKING_LIFETIME_HYBRID_PG);
        if (packetForPg == null) {
            // 6.2.17.20.a - Fail PG query where no response was received.
            addWarning("6.2.17.20.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_HYBRID_PG);
        } else {
            var partOnePacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
            packetForPg.getSpns()
                       .forEach(spn -> {
                           // 6.2.17.20.b - Fail PG query where any accumulator value
                           // received is greater than FAFFFFFFh.
                           if (spn.getRawValue() > 0xFAFFFFFFL) {
                               addFailure("6.2.17.20.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                       + module.getModuleName() + " for " + spn);
                           }
                           // 6.2.17.20.c - Fail all values where the corresponding value received in part 1 is greater
                           // than the part 2 value
                           var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                           if (partOneValue > spn.getValue()) {
                               addFailure("6.2.17.20.c - Value received from " + module.getModuleName() + " for " + spn
                                       + "  in part 1 was greater than part 2 value");
                           }

                       });
        }

        // 6.2.17.21 Actions11 for MY2022+ HEV and BEV drives
        // 6.2.17.21.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for Active 100hr
        // PSA Times and Stored 100hr PSA Times PGs:
        // PG PG Label
        // 64242 PSA Times Stored 100 Hours - PG Acronym PSATS
        // 64243 PSA Times Active 100 Hours - PG Acronym PSATA
        // 6.2.17.21.c - List data received in a table using lifetime, stored 100 hr, active 100hr
        // for columns, and categories for rows.
        List<GenericPacket> ghgPackets = requestPackets(module.getSourceAddress(),
                                                        GHG_STORED_HYBRID_100_HR,
                                                        GHG_ACTIVE_HYBRID_100_HR)
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
        for (int pg : List.of(GHG_STORED_HYBRID_100_HR, GHG_ACTIVE_HYBRID_100_HR)) {
            GenericPacket hybridPacketForPg = haveResponseWithPg(ghgPackets, pg);
            if (hybridPacketForPg == null) {
                // 6.2.17.22.a - For MY2024+ HEV and BEV drives, Fail each PG query where no
                // response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.2.17.22.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.2.17.22.b - For MY2022-23 HEV and BEV drives, Warn each PG query, where no
                // response was received.
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.2.17.22.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                var partOnePacket = get(hybridPacketForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                hybridPacketForPg.getSpns()
                                 .forEach(spn -> {
                                     // 6.2.17.22.c - Fail each PG query where any accumulator
                                     // value received is greater than FAFFh.
                                     if (spn.getRawValue() > 0xFAFFL) {
                                         addFailure("6.2.17.22.c - Bin value received is greater than 0xFAFF(h) from "
                                                 + module.getModuleName() + " for " + spn);
                                     }
                                     // 6.2.17.22.d - Fail all values where the corresponding value received in part 1
                                     // is greater than the part 2 values. (where supported)
                                     var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                                     if (partOneValue > spn.getValue()) {
                                         addFailure("6.2.17.22.d - Value received from " + module.getModuleName()
                                                 + " for " + spn
                                                 + "  in part 1 was greater than part 2 value");
                                     }
                                 });
            }
        }
    }

    private void testSp12730(OBDModuleInformation module) {
        // 6.2.17.11 Actions6 for all MY2022+ Engines
        // 6.2.17.11.a - DS request messages to ECU that indicated support in DM24 for upon request SP 12730 (GHG
        // Tracking Lifetime Engine Run Time) for PG 64252 GHG Tracking Lifetime Array Data.
        var ghgTrackingLifetimePackets = requestPackets(module.getSourceAddress(),
                                                        GHG_TRACKING_LIFETIME_PG)
                                                                                 .stream()
                                                                                 // 6.2.17.11.b - Record each value
                                                                                 // for use in Part 12.
                                                                                 .peek(this::save)
                                                                                 .collect(Collectors.toList());

        GenericPacket packetForPg = haveResponseWithPg(ghgTrackingLifetimePackets, GHG_TRACKING_LIFETIME_PG);
        if (packetForPg == null) {
            // 6.2.17.12.a. Fail PG query where no response was received
            addFailure("6.2.17.12.a - No response was received from "
                    + module.getModuleName() + " for PG "
                    + GHG_TRACKING_LIFETIME_PG);
        } else {
            var partOnePacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
            packetForPg.getSpns()
                       .forEach(spn -> {
                           // 6.2.17.12.b. Fail PG query where any bin value received is greater than FAFFh.
                           if (spn.getRawValue() > 0xFAFFFFFFL) {
                               addFailure("6.2.17.12.b - Bin value received is greater than 0xFAFFFFFFL(h) from "
                                       + module.getModuleName() + " for " + spn);
                           }
                           // 6.2.17.12.c - Fail all values where the corresponding value received in part 1 is
                           // greater than the part 2 value (where supported)
                           var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                           if (partOneValue > spn.getValue()) {
                               addFailure("6.2.17.12.c - Value received from " + module.getModuleName()
                                       + " for " + spn
                                       + " in part 1 was greater than part 2 value");
                           }
                       });
        }

        // 6.2.17.13 Actions7 for MY2022+ Engines
        // 6.2.17.13.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12730 (GHG Tracking Lifetime Engine Run Time) for each 100hr GHG tracking PG
        // PG PG Label
        // 64254 GHG Tracking Active 100 Hour Array Data
        // 64253 GHG Tracking Stored 100 Hour Array Data
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                GHG_ACTIVE_100_HR,
                                                GHG_STORED_100_HR)
                                                                  .stream()
                                                                  // 6.2.17.13.b.
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

        for (int pg : List.of(GHG_ACTIVE_100_HR, GHG_STORED_100_HR)) {
            GenericPacket trackingPacketForPg = haveResponseWithPg(ghgTrackingPackets, pg);
            if (trackingPacketForPg == null) {
                // 6.2.17.14.a. For all MY2024+ engines, Fail each PG query where no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.2.17.14.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.2.17.14.b. For MY2022-23 engines, Warn each PG query, where no response was received
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.2.17.14.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                var partOnePacket = get(trackingPacketForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                trackingPacketForPg.getSpns()
                                   .forEach(spn -> {
                                       if (spn.getRawValue() > 0xFAFFL) {
                                           // 6.2.17.14.c - Fail each PG query where any value received is greater than
                                           // FAFFh.
                                           addFailure("6.2.17.14.c - Bin value received is greater than 0xFAFF from "
                                                   + module.getModuleName() + " for " + spn);
                                       }
                                       // 6.2.17.14.d - Fail all values where the corresponding value received in part 1 is greater than the part 2 value
                                       var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                                       if (partOneValue > spn.getValue()) {
                                           addFailure("6.2.17.14.d - Value received from " + module.getModuleName()
                                                              + " for " + spn
                                                              + "  in part 1 was greater than part 2 value");
                                       }
                                   });
            }
        }
    }

    private void testSp12691(OBDModuleInformation module) {
        // 6.2.17.15 Actions8 for all MY2022+ Engines
        // 6.2.17.15.a - DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for
        // PG 64257 Green House Gas Lifetime Active Technology Tracking.
        var ghgPackets = requestPackets(module.getSourceAddress(),
                                        GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG)
                                                                             .stream()
                                                                             // 6.2.17.15.b. Record
                                                                             // each value for use
                                                                             // in Part 12.
                                                                             .peek(this::save)
                                                                             .collect(Collectors.toList());

        for (int pg : List.of(GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG)) {
            GenericPacket packetForPg = haveResponseWithPg(ghgPackets, pg);
            if (packetForPg == null) {
                // 6.2.17.16.a. Warn PG query where no response was received.
                addWarning("6.2.17.16.a - No response was received from "
                        + module.getModuleName() + " for PG "
                        + pg);
            } else {
                var partOnePacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // 6.2.17.16.b. Fail any accumulator value received that is greater
                               // than FAFFFFFFh.
                               if (spn.getRawValue() > 0xFAFFFFFFL) {
                                   addFailure("6.2.17.16.b - Bin value received is greater than 0xFAFFFFFF(h) from "
                                           + module.getModuleName() + " for " + spn);
                               }
                               // 6.2.17.16.c. Fail all values where the corresponding value received in part 1 is greater than the part 2 value.
                               var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                               if (partOneValue > spn.getValue()) {
                                   addFailure("6.2.17.16.c - Value received from " + module.getModuleName()
                                                      + " for " + spn
                                                      + "  in part 1 was greater than part 2 value");
                               }
                           });
            }
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
                                                                           // 6.2.17.17.b. Record
                                                                           // each value for use
                                                                           // in Part 12.
                                                                           .peek(this::save)
                                                                           .collect(Collectors.toList());
        // 6.2.17.17.c. List data received in a table using lifetime, stored 100 hr,
        // active 100hr for columns, and categories for rows.
        getListener().onResult(ghgTrackingModule.formatTechTable(Stream.concat(ghgPackets.stream(),
                                                                               ghg100HrPackets.stream())
                                                                       .collect(Collectors.toList())));

        for (int pg : List.of(GHG_ACTIVE_GREEN_HOUSE_100_HR, GHG_STORED_GREEN_HOUSE_100_HR)) {
            GenericPacket packetForPg = haveResponseWithPg(ghg100HrPackets, pg);
            if (packetForPg == null) {
                if (getEngineModelYear() >= 2024) {
                    // FIXME: current version of the document says warning; Eric clarified to be a failure
                    // @Joe - we just need to remove above comment when Eric gives us a document with it corrected.
                    // 6.2.17.18.a. For all MY2024+ engines, Warn each PG query where no response was received.
                    addFailure("6.2.17.18.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    // 6.2.17.18.b. For MY2022-23 engines, Warn each PG query, where no response was received
                    addWarning("6.2.17.18.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // 6.2.17.18.c. Fail PG query where any bin value received is greater than FAFFh.
                               if (spn.getRawValue() > 0xFAFFL) {
                                   addFailure("6.2.17.18.c - Bin value received is greater than 0xFAFF(h) from "
                                           + module.getModuleName() + " for " + spn);
                               }
                               // 6.2.17.18.d. Fail PG query where any index value received is greater than FAh.
                               if (spn.getId() == 12691 && spn.getRawValue() > 0xFAL) {
                                   addFailure("6.2.17.18.d - PG query index received was greater than FA(h) from "
                                           + module.getModuleName() + " for " + spn);
                               }
                               if (GHG_ACTIVE_GREEN_HOUSE_100_HR == spn.getId() && spn.getValue() > 0) {
                                   // 6.2.17.18.g. Fail each active 100 hr array value that is greater than zero
                                   addFailure("6.2.17.18.g - Active 100 hr array value received was greater than zero from  "
                                           + module.getModuleName() + " for " + spn);
                               }

                           });
                // FIXME:
                // 6.2.17.18.e. Fail each response where the number of labels received are not
                // @Joe this is clarified in the email from Eric the same as the number of labels received for the
                // lifetime technology response.
                // FIXME:
                // 6.2.17.18.f. Fail each response where the set of labels received is not a
                // subset of the set of labels received for the lifetime active technology
                // response.

            }
        }
    }

    private void testSp12675(OBDModuleInformation module) {
        int[] nOxLifeTimeSps = CollectionUtils.join(NOx_LIFETIME_SPs,
                                                    NOx_LIFETIME_ACTIVITY_SPs);
        // 6.2.17.7.a. DS request messages to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine
        var nOxPackets = requestPackets(module.getSourceAddress(),
                                        nOxLifeTimeSps)
                                                       .stream()
                                                       // 6.2.17.7.b. Record
                                                       // each value for use
                                                       // in Part 12.
                                                       .peek(this::save)
                                                       .collect(Collectors.toList());
        // FIXME:  @Joe this requirement needs to be removed
        // 6.2.17.7.c. List data received in a table using bin numbers for rows.
        getListener().onResult(nOxBinningModule.format(nOxPackets));
        for (int pg : nOxLifeTimeSps) {
            GenericPacket packetForPg = haveResponseWithPg(nOxPackets, pg);
            if (packetForPg == null) {
                // 6.2.17.8.a. Fail each PG query where no response was received.
                addFailure("6.2.17.8.a - No response was received from "
                        + module.getModuleName() + " for PG "
                        + pg);
            } else {
                var partOnePacket = get(packetForPg.getPgnDefinition().getId(), module.getSourceAddress(), 1);
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // 6.2.17.8.b. Fail each PG query where any bin value received
                               // is greater than FAFFFFFFh.
                               if (spn.getRawValue() > 0xFAFFFFFFL) {
                                   addFailure("6.2.17.8.b - Bin value received is greater than 0xFAFFFFFF(h) form "
                                           + module.getModuleName() + " for " + spn);
                               }
                               // 6.2.17.8.c Fail all values where the corresponding value received in part 1 is greater
                               // than the part 2 value
                               var partOneValue = partOnePacket.getSpnValue(spn.getId()).orElse(NOT_AVAILABLE);
                               if (partOneValue > spn.getValue()) {
                                   addFailure("6.2.17.8.c - Value received from " + module.getModuleName()
                                           + " for " + spn + " in part 1 was greater than part 2 value");
                               }
                           });
            }
        }

        // 6.2.17.9.a - DS request message to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1
        // - Total) for each active 100hr NOx binning PG, followed by each Stored 100 hr PG
        // Label
        List<GenericPacket> nOx100HourPackets = requestPackets(module.getSourceAddress(),
                                                               CollectionUtils.join(NOx_TRACKING_ACTIVE_100_HOURS_SPs,
                                                                                    NOx_TRACKING_STORED_100_HOURS_SPs))
                                                                                                                       .stream()
                                                                                                                       // 6.2.17.9.b
                                                                                                                       // - Record
                                                                                                                       // each value
                                                                                                                       // for use
                                                                                                                       // in Part 12.
                                                                                                                       .peek(this::save)
                                                                                                                       .collect(Collectors.toList());

        // 6.2.17.9.c - List data received in a table using bin numbers for rows.
        getListener().onResult(nOxBinningModule.format(nOx100HourPackets));
        for (int pg : CollectionUtils.join(NOx_TRACKING_ACTIVE_100_HOURS_SPs,
                                           NOx_TRACKING_STORED_100_HOURS_SPs)) {
            GenericPacket packetForPg = haveResponseWithPg(nOx100HourPackets, pg);
            if (packetForPg == null) {
                // 6.2.17.10.a. For all MY2024+ Diesel engines, Fail each PG query where no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.2.17.10.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                // 6.2.17.10.b. For all MY2022-23 Diesel engines, Warn each PG query where no response was received.
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    addWarning("6.2.17.10.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                packetForPg.getSpns().forEach(spn -> {
                    if (spn.getRawValue() > 0xFAFFFFFFL) {
                        // 6.2.17.10.c. Fail each PG query where any bin value received is greater than FAFFh. (Use
                        // FAFFFFFFh for NOx values)
                        addFailure("6.2.17.10.c - Bin value received is greater than 0xFAFFFFFF(h) from "
                                + module.getModuleName() + " for " + spn);

                    }
                    // 6.2.17.10.e. Fail each active 100 hr array value that is greater than zero. (where supported)
                    if (spn.getValue() > 0) {
                        // 6.2.17.10.d. Warn for all active 100 hr bin 3 through bin 17 values that are greater than
                        // zero. (Where supported)
                        addWarning("6.2.17.10.d - Bin value received is greater than zero from "
                                + module.getModuleName() + " for " + spn);

                    }
                });
            }
        }
    }
}
