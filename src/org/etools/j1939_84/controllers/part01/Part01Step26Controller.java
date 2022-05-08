/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.controllers.ResultsListener.MessageType.ERROR;

import java.util.ArrayList;
import java.util.Arrays;
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

public class Part01Step26Controller extends StepController {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 26;

    private final GhgTrackingModule ghgTrackingModule;
    private final NOxBinningModule nOxBinningModule;

    // 64262 NOx Tracking Valid NOx Lifetime Fuel Consumption Bins
    // 64263 NOx Tracking Valid NOx Lifetime Engine Run Time Bins
    // 64264 NOx Tracking Valid NOx Lifetime Vehicle Distance Bins
    // 64265 NOx Tracking Valid NOx Lifetime Engine Output Energy Bins
    // 64266 NOx Tracking Valid NOx Lifetime Engine Out NOx Mass Bins
    // 64267 NOx Tracking Valid NOx Lifetime System Out NOx Mass Bins
    private static final int[] NOx_LIFETIME_SPs = { 64262, 64263, 64264, 64265, 64266, 64267 };

    // 64258 NOx Tracking Engine Activity Lifetime Fuel Consumption Bins
    // 64259 NOx Tracking Engine Activity Lifetime Engine Run Time Bins
    // 64260 NOx Tracking Engine Activity Lifetime Vehicle Distance Bins
    // 64261 NOx Tracking Engine Activity Lifetime Engine Output Energy Bins NTEEEA
    private static final int[] NOx_LIFETIME_ACTIVITY_SPs = { 64258, 64259, 64260, 64261 };
    // PG Acronym NTFCA
    // NTEHA NTVMA NTEEA NTENA
    // NTSNA NTFCS NTEHS NTVMS
    // NTEES NTENS NTSNS
    // 64274 NOx Tracking Active 100 Hour Fuel Consumption Bins
    // 64275 NOx Tracking Active 100 Hour Engine Run Time Bins
    // 64276 NOx Tracking Active 100 Hour Vehicle Distance Bins
    // 64277 NOx Tracking Active 100 Hour Engine Output Energy Bins
    // 64278 NOx Tracking Active 100 Hour Engine Out NOx Mass Bins
    // 64279 NOx Tracking Active 100 Hour System Out NOx Mass Bins
    private static final int[] NOx_TRACKING_ACTIVE_100_HOURS_SPs = { 64274, 64275, 64276, 64277, 64278, 64279 };
    // 64268 NOx Tracking Stored 100 Hour
    // 64269 NOx Tracking Stored 100 Hour
    // 64270 NOx Tracking Stored 100 Hour
    // 64271 NOx Tracking Stored 100 Hour
    // 64272 NOx Tracking Stored 100 Hour
    // 64273 NOx Tracking Stored 100 Hour
    private static final int[] NOx_TRACKING_STORED_100_HOURS_SPs = { 64268, 64269, 64270, 64271, 64272, 64273 };

    private final BroadcastValidator broadcastValidator;
    private final BusService busService;

    private final J1939DaRepository j1939DaRepository;

    private final TableA1Validator tableA1Validator;

    public Part01Step26Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new TableA1Validator(DataRepository.getInstance(), PART_NUMBER, STEP_NUMBER),
             J1939DaRepository.getInstance(),
             new BroadcastValidator(DataRepository.getInstance(), J1939DaRepository.getInstance()),
             new BusService(J1939DaRepository.getInstance()),
             new GhgTrackingModule(DateTimeModule.getInstance()),
             new NOxBinningModule(DateTimeModule.getInstance()));
    }

    Part01Step26Controller(Executor executor,
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
              1,
              26,
              0);
        this.tableA1Validator = tableA1Validator;
        this.j1939DaRepository = j1939DaRepository;
        this.broadcastValidator = broadcastValidator;
        this.busService = busService;
        this.ghgTrackingModule = ghgTrackingModule;
        this.nOxBinningModule = nOxBinningModule;
    }

    @Override
    protected void run() throws Throwable {
        busService.setup(getJ1939(), getListener());

        // This will listen for all Broadcast PGNs in hopes of finding all Data
        // Stream Supported SPNs. Then by module, process and report on the data found,
        // any missing data and on-request PGNs are requested DS
        // That data is reported on
        // Any yet missing data is requested globally
        // Finally, the bus is monitored again for any missing data
        // The Table A1 validator then reports on the data found

        // 6.1.26.1.a. Create a list of expected SPNs and PGNs from the DM24 response, where the data stream support bit
        // defined in SAE J1939-73 5.7.24 is 0. Omit the following SPs (588, 976, 1213, 1220, 12675, 12730, 12783,
        // 12797) which are included in the list. Omit any remaining SPs that map to multiple PGs. Display the completed
        // list noting those omitted SPs and supported SPs as ‘broadcast’ or ‘upon request’.
        List<Integer> supportedSPNs = getDataRepository().getObdModules()
                                                         .stream()
                                                         // FIXME: (Update method when clarification of SPs list of
                                                         // WARN? FAIL? BOTH?
                                                         // 6.1.26.1.b. Add any omissions from Table A-1, excluding
                                                         // those SPs noted (as CI or SI) for the opposite fuel type
                                                         // provided by the user
                                                         // 6.1.26.1.c. Omit the following SPNs (588, 976, 1213,
                                                         // 1220, 12675, 12691, 12730, 12783, 12797)
                                                         // which are included in the list. Display the completed list
                                                         // noting those omitted SPs, supported SPs as
                                                         // ‘broadcast’ or ‘upon request’, and additions from Table A-1.
                                                         .flatMap(m -> m.getFilteredDataStreamSPNs().stream())
                                                         .map(SupportedSPN::getSpn)
                                                         .collect(Collectors.toList());

        // FIXME: Eric defining and this will need to updated in the tableA1Validator
        // 6.1.26.1.b. Add any omissions from Table A-1, excluding those SPs noted (as CI or SI) for the opposite fuel
        // type provided by the user.
        // 6.1.26.1.c. Display the completed list noting those omitted SPs, supported SPs as ‘broadcast’ or
        // ‘upon request’, and additions from Table A-1.
        tableA1Validator.reportExpectedMessages(getListener());

        // 6.1.26.1.d. Gather broadcast data for all SPNs that are supported for data stream in the OBD ECU DM24
        // responses, and the added SPNs from Table A-1. This shall include the both SPs that are expected to be queried
        // with DS queries (in step 6.1.26.5 for SPs supported in DM24) and SPs that are expected without queries.
        // 6.1.26.1.e. Gather/timestamp each parameter that is observed at least three times to be able to verify
        // frequency of broadcast
        Stream<GenericPacket> packetStream = busService.readBus(broadcastValidator.getMaximumBroadcastPeriod() * 4,
                                                                "6.1.26.1.e");

        List<GenericPacket> packets = packetStream.peek(p -> {
                                                      try {
                                                          Controller.checkEnding();
                                                      } catch (InterruptedException e) {
                                                          packetStream.close();
                                                      }
                                                  })
                                                  .peek(p ->
                                                  // 6.1.26.2.a - Fail if no response/no valid data for any broadcast SP
                                                  // indicated as supported by the OBD ECU in DM24
                                                  tableA1Validator.reportNotAvailableSPNs(p,
                                                                                          getListener(),
                                                                                          "6.1.26.2.a"))
                                                  .peek(p ->
                                                  // 6.1.26.2.c - Fail/warn if any broadcast data is not valid for KOEO
                                                  // conditions as per Table A1, Minimum Data Stream Support
                                                  tableA1Validator.reportImplausibleSPNValues(p,
                                                                                              getListener(),
                                                                                              false,
                                                                                              "6.1.26.2.c"))
                                                  .peek(p ->
                                                  // 6.1.26.2.d - Fail/warn per Table A-1, if an expected SPN from the
                                                  // DM24 support list from an OBD ECU is provided by a non-OBD ECU.
                                                  // (provided extraneously)
                                                  tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                                                                                  getListener(),
                                                                                                  "6.1.26.2.d"))
                                                  .peek(p ->
                                                  // 6.1.26.3.a. Identify SPNs provided in the data stream that are
                                                  // listed in Table A-1, but are not supported by any OBD ECU in its
                                                  // DM24 response.

                                                  // 6.1.26.4.a. Fail/warn per Table A-1 column, “Action if SPN provided
                                                  // but not included in DM24”.
                                                  tableA1Validator.reportProvidedButNotSupportedSPNs(p,
                                                                                                     getListener(),
                                                                                                     "6.1.26.4.a"))
                                                  .peek(p -> tableA1Validator.reportPacketIfNotReported(p,
                                                                                                        getListener(),
                                                                                                        false))
                                                  .collect(Collectors.toList());

        // Notify the user if there's another ECU on the bus using our address
        if (getJ1939().getBus().imposterDetected()) {
            String msg = "6.1.26 - Unexpected Service Tool Message from SA 0xF9 observed. Test results uncertain. False failures are possible";
            addWarning(msg);
            displayInstructionAndWait(msg, "Second device using SA 0xF9", ERROR);
        }

        // 6.1.26.2.e - Fail/warn per Table A-1 if two or more ECUs provide an SPN listed in Table A-1
        tableA1Validator.reportDuplicateSPNs(packets, getListener(), "6.1.26.2.e");

        // Check the Broadcast Period of the received packets1
        // Map of PGN to (Map of Source Address to List of Packets)
        Map<Integer, Map<Integer, List<GenericPacket>>> foundPackets = broadcastValidator.buildPGNPacketsMap(packets);

        // 6.1.26.2.b - Fail if any parameter is not broadcast within ±10% of the specified broadcast period
        broadcastValidator.reportBroadcastPeriod(foundPackets,
                                                 supportedSPNs,
                                                 getListener(),
                                                 getPartNumber(),
                                                 getStepNumber());

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
                                                                                            "6.1.26.2.a");

            // DS Request for all SPNs that are sent on-request AND those were missed earlier
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
                updateProgress("Test 1.26 - Verifying " + Lookup.getAddressName(moduleAddress));
                String spns = j1939DaRepository.findPgnDefinition(pgn)
                                               .getSpnDefinitions()
                                               .stream()
                                               .map(SpnDefinition::getSpnId)
                                               .filter(s -> missingSPNs.contains(s) || dataStreamSPNs.contains(s))
                                               .sorted()
                                               .map(Object::toString)
                                               .collect(Collectors.joining(", "));
                // 6.1.26.5.a - DS messages to ECU that indicated support in DM24 for upon request SPs and SPs not
                // observed in step 1.o and SPs added from Table A-1, regardless of whether the SPs were or were not
                // observed in step 1. [Where a PG contains more than one SP (to be queried), that PG need only be
                // queried one time].
                List<GenericPacket> dsResponse = busService.dsRequest(pgn, moduleAddress, spns)
                                                           .peek(p -> tableA1Validator.reportNotAvailableSPNs(p,
                                                                                                              getListener(),
                                                                                                              "6.1.26.5.a"))
                                                           .peek(p ->
                                                           // 6.1.26.6.e. Fail/warn if any broadcast data is not valid
                                                           // for KOEO conditions as per Table A-1, Min Data Stream
                                                           // Support.
                                                           tableA1Validator.reportImplausibleSPNValues(p,
                                                                                                       getListener(),
                                                                                                       false,
                                                                                                       "6.1.26.6.e"))
                                                           .peek(p ->
                                                           // 6.1.26.6.f. Fail/warn per Table A-1, if an expected SPN
                                                           // from the DM24 support list from an OBD ECU is provided by
                                                           // a non-OBD ECU.
                                                           tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                                                                                           getListener(),
                                                                                                           "6.1.26.6.f"))
                                                           .collect(Collectors.toList());
                onRequestPackets.addAll(dsResponse);
                // 6.1.26.6.a Ignore NACK received for any SP added from table A-1, whose PG does not contain any SPs
                // listed as supported in DM24
                // 6.1.26.6.b Fail if no response or NACK for any SP indicated as supported by the OBD ECU in DM24.
                // FIXME: downgrade needs to be implemented in the broadcastValidator
                // Downgrade the failure to a warning where an upon request SPN was received in 6.1.26.1, and the
                // request was not replied to with a NACK.
                List<String> notAvailableSPNs = broadcastValidator.collectAndReportNotAvailableSPNs(supportedSPNs,
                                                                                                    pgn,
                                                                                                    dsResponse,
                                                                                                    moduleAddress,
                                                                                                    getListener(),
                                                                                                    getPartNumber(),
                                                                                                    getStepNumber(),
                                                                                                    "6.1.26.6.a");

                if (!notAvailableSPNs.isEmpty()) {
                    // 6.1.26.5.b - If no response/no valid data for any SP requested in 6.1.25.3.a, send global message
                    // to request that SP(s)
                    String globalMessage = "Global Request for PGN " + pgn + " for SPNs "
                            + String.join(", ", notAvailableSPNs);
                    List<GenericPacket> globalPackets = busService.globalRequest(pgn, globalMessage)
                                                                  .peek(p -> tableA1Validator.reportNotAvailableSPNs(p,
                                                                                                                     getListener(),
                                                                                                                     "6.1.26.5.b"))
                                                                  .peek(p ->
                                                                  // 6.1.26.6.e. Fail/warn if any broadcast data is not
                                                                  // valid for KOEO conditions
                                                                  // as per Table A-1, Min Data Stream Support.
                                                                  tableA1Validator.reportImplausibleSPNValues(p,
                                                                                                              getListener(),
                                                                                                              false,
                                                                                                              "6.1.26.6.e"))
                                                                  .peek(p ->
                                                                  // 6.1.26.6.f. Warn/info per Table A-1, if an expected
                                                                  // SPN from the DM24 support list is provided by a
                                                                  // non-OBD ECU
                                                                  tableA1Validator.reportNonObdModuleProvidedSPNs(p,
                                                                                                                  getListener(),
                                                                                                                  "6.1.26.6.f"))
                                                                  .collect(Collectors.toList());

                    // Map of PGN to (Map of Source Address to List of Packets)
                    Map<Integer, Map<Integer, List<GenericPacket>>> foundGlobalPackets = broadcastValidator.buildPGNPacketsMap(globalPackets);

                    // 6.1.26.6.c - Fail if any parameter in a fixed period message, upon request, is not broadcast
                    // within ± 10% of the specified broadcast period.
                    broadcastValidator.reportBroadcastPeriod(foundGlobalPackets,
                                                             supportedSPNs,
                                                             getListener(),
                                                             getPartNumber(),
                                                             getStepNumber());

                    onRequestPackets.addAll(globalPackets);
                }
            }
            // 6.1.26.7 Actions4 for MY2022+ Diesel Engines
            if (getEngineModelYear() >= 2022 && getFuelType().isCompressionIgnition()) {
                getDataRepository()
                                   .getObdModules()
                                   .stream()
                                   .filter(obdModuleInformation -> {
                                       return obdModuleInformation.supportsSpn(12675)
                                               || obdModuleInformation.supportsSpn(12730)
                                               || obdModuleInformation.supportsSpn(12691)
                                               || obdModuleInformation.supportsSpn(12797)
                                               || obdModuleInformation.supportsSpn(12783);
                                   })
                                   .forEach(module -> {
                                       if (module.supportsSpn(12675)) {
                                           // 6.1.26.7 - 6.1.26.10
                                           testSp12675(module);
                                       }
                                       if (module.supportsSpn(12730)) {
                                           // 6.1.26.11 - 6.1.26.14
                                           testSp12730(module);
                                       }
                                       if (module.supportsSpn(12691)) {
                                           // 6.1.26.15 - 6.1.26.18
                                           testSp12691(module);
                                       }
                                       if (module.supportsSpn(12797)) {
                                           // 6.1.26.19 - 6.1.26.22
                                           testSp12797(module);
                                       }
                                       if (module.supportsSpn(12783)) {
                                           // 6.1.26.23, 6.1.17.24, 6.1.26.24 & 6.1.26.25
                                           testSp12783(module);
                                       }
                                   });
            }
        }// end obdModule

        // 6.1.26.6.g. Fail/warn per Table A-1, if two or more ECUs provide an SPN listed in Table A-1.
        tableA1Validator.reportDuplicateSPNs(onRequestPackets,
                                             getListener(),
                                             "6.1.26.6.g");

        // 6.1.26.6.h. Fail/warn per Table A-1 column, “Action if SPN provided but not included in DM24”
        // FIXME: the call to then new Table A-1 validator method for this functionality need to be added here once the
        // method has been written - note: call may need to move into obdModule loop
    }

    private void testSp12783(OBDModuleInformation module) {
        // 6.1.26.23 Actions12 for MY2022+ Plug-in HEV DRIVES
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for
        // PG 64244 Hybrid Charge Depleting or Increasing Operation Lifetime Hours
        int[] ghgLifeTimeSps = { 64244 };
        List<GenericPacket> ghgLifeTimePackets = requestPackets(module.getSourceAddress(),
                                                                ghgLifeTimeSps)
                                                                               .stream()
                                                                               // 6.1.26.23.b.
                                                                               // Record
                                                                               // each
                                                                               // value
                                                                               // for
                                                                               // use
                                                                               // in Part
                                                                               // 12.
                                                                               .peek(this::save)
                                                                               .collect(Collectors.toList());

        for (int pg : ghgLifeTimeSps) {
            GenericPacket packetForPg = haveResponseWithPg(ghgLifeTimePackets, pg);
            if (packetForPg == null) {
                // 6.1.26.24.a. Fail PG query where no response was received
                addFailure("6.1.26.24.a - No response was received from "
                        + module.getModuleName() + "for PG "
                        + pg);
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // SAE INTERNATIONAL J1939TM-84 Proposed Draft 24 March
                               // 2022 Page 37 of 140
                               // 6.1.26.24.b - Fail PG query where any accumulator value
                               // received is greater than FAFFFFFFh.
                               if (spn.getRawValue() >= 0xFAFFFFFFL) {
                                   addFailure("6.1.26.24.b - Bin value received is greater than 0xFAFFFFFF(h) "
                                           + module.getModuleName() + " returned "
                                           + Arrays.toString(spn.getBytes()));
                               }
                           });
            }
        }

        // 6.1.26.24 Actions13 for MY2022+ Plug-in HEV DRIVES
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12783 (Hybrid Lifetime Distance Traveled in Charge Depleting Operation with
        // Engine off) for Active 100hr Charge Depleting times and
        // Stored 100hr PSA Charge Depleting PGs:
        // PG PG Label
        // 64246 Hybrid Charge Depleting or Increasing Operation Active 100 Hours
        // 64245 Hybrid Charge Depleting or Increasing Operation Stored 100 Hours
        // PG Acronym HCDIOA HCDIOS
        int[] hybridChargeOpsSps = { 64245, 64246 };
        var hybridChargeOpsPackets = requestPackets(module.getSourceAddress(),
                                                    hybridChargeOpsSps)
                                                                       .stream()
                                                                       // 6.1.26.24.b. Record
                                                                       // each value for use in Part 2.
                                                                       .peek(this::save)
                                                                       .collect(Collectors.toList());

        if (!hybridChargeOpsPackets.isEmpty()) {
            // 6.1.26.24.c - List data received in a table using lifetime, stored 100 hr, active 100hr for columns, and
            // categories for rows.
            getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(ghgLifeTimePackets.stream(),
                                                                                  hybridChargeOpsPackets.stream())
                                                                          .collect(Collectors.toList())));
        }
        for (int pg : hybridChargeOpsSps) {
            GenericPacket packetForPg = haveResponseWithPg(hybridChargeOpsPackets,
                                                           pg);
            if (packetForPg == null) {
                // 6.1.26.25.a - For MY2024+ Plug-in HEV DRIVES, Fail each PG query where
                // no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.1.26.25.a - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
                // 6.1.26.25.b - For MY2022-23 Plug-in HEV DRIVES, Warn each PG query,
                // where no response was received
                if (2022 <= getEngineModelYear() && getEngineModelYear() >= 2023) {
                    addWarning("6.1.26.25.b - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               /// 6.1.26.25.c - Fail each PG query where any active
                               /// technology label or accumulator value
                               // received is greater than FAFFh, respectively.
                               if (spn.getRawValue() >= 0xFAFFL) {
                                   addFailure("6.1.26.22.c - Bin value received is greater than 0xFAFF(h)"
                                           + module.getModuleName() + " returned "
                                           + Arrays.toString(spn.getBytes()));
                               }
                           });
            }
        }
    }

    private void testSp12797(OBDModuleInformation module) {
        // 6.1.26.19 Actions10 for MY2022+ HEV and BEV drives
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for 64241 PSA Times
        // Lifetime Hours
        int[] ghgPropulsionSystemPgs = { 64241 };
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                ghgPropulsionSystemPgs)
                                                                       .stream()
                                                                       // 6.1.26.19.b.
                                                                       // Record
                                                                       // each value for
                                                                       // use
                                                                       // in Part 2.
                                                                       .peek(this::save)
                                                                       .collect(Collectors.toList());

        for (int pg : ghgPropulsionSystemPgs) {
            GenericPacket packetForPg = haveResponseWithPg(ghgTrackingPackets, pg);
            if (packetForPg == null) {
                // 6.1.26.20.a - Fail PG query where no response was received.
                addWarning("6.1.26.20.a - No response was received from "
                        + module.getModuleName() + "for PG "
                        + pg);
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // 6.1.26.20.b - Fail PG query where any accumulator value
                               // received is greater than FAFFFFFFh.
                               if (spn.getRawValue() >= 0xFAFFFFFFL) {
                                   addFailure("6.1.26.20.b - Bin value received is greater than 0xFAFFFFFF(h)"
                                           + module.getModuleName() + " returned "
                                           + spn.getBytes());
                               }

                           });
            }
        }

        // 6.1.26.21 Actions11 for MY2022+ HEV and BEV drives
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12797 (Hybrid Lifetime Propulsion System Active Time) for Active 100hr
        // PSA Times and Stored 100hr PSA Times PGs:
        // PG PG Label
        // 64242 PSA Times Stored 100 Hours
        // 64243 PSA Times Active 100 Hours
        // PG Acronym PSATS PSATA
        // c. List data received in a table using lifetime, stored 100 hr, active 100hr
        // for columns, and categories for rows.
        int[] nOxLifeTimeSps = { 64242, 64243 };
        List<GenericPacket> ghgPackets = requestPackets(module.getSourceAddress(),
                                                        nOxLifeTimeSps)
                                                                       .stream()
                                                                       // 6.1.26.21.b.
                                                                       // Record each
                                                                       // value for use
                                                                       // in Part 2.
                                                                       .peek(this::save)
                                                                       .collect(Collectors.toList());

        if (!ghgPackets.isEmpty()) {
            // 6.1.26.21.c. List data received in a table using bin numbers for rows.
            getListener().onResult(ghgTrackingModule.formatXevTable(Stream.concat(ghgTrackingPackets.stream(),
                                                                                  ghgPackets.stream())
                                                                          .collect(Collectors.toList())));
        }
        for (int pg : nOxLifeTimeSps) {
            GenericPacket packetForPg = haveResponseWithPg(ghgPackets, pg);
            if (packetForPg == null) {
                // 6.1.26.22.a. For MY2024+ HEV and BEV drives, Fail each PG query where no
                // response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.1.26.22.a - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
                // b. For MY2022-23 HEV and BEV drives, Warn each PG query, where no
                // response was
                if (2022 <= getEngineModelYear() && getEngineModelYear() >= 2023) {
                    addWarning("6.1.26.22.b - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // SAE INTERNATIONAL J1939TM-84 Proposed Draft 24 March
                               // 2022 Page 37 of 140
                               // 6.1.26.22.c. Fail each PG query where any accumulator
                               // value received is greater than FAFFh.
                               if (spn.getRawValue() >= 0xFAFFL) {
                                   addFailure("6.1.26.22.c - Bin value received is greater than 0xFAFFFFFF(h)"
                                           + module.getModuleName() + " returned "
                                           + spn.getBytes());
                               }
                           });
            }
        }
    }

    private void testSp12730(OBDModuleInformation module) {
        // 6.1.26.11 Actions6 for all MY2022+ Engines
        // 6.1.26.11.a - DS request messages to ECU that indicated support in DM24 for upon request SP 12730 (GHG
        // Tracking Lifetime Engine Run
        // Time) for PG 64252 GHG Tracking Lifetime Array Data.
        int[] ghgTrackingLifetime100HrPgs = { 64252 };
        var ghgTrackingLifetimePackets = requestPackets(module.getSourceAddress(),
                                                        ghgTrackingLifetime100HrPgs)
                                                                                    .stream()
                                                                                    // 6.1.26.11.b - Record each value
                                                                                    // for use in Part 2.
                                                                                    .peek(this::save)
                                                                                    .collect(Collectors.toList());

        for (int pg : ghgTrackingLifetime100HrPgs) {
            GenericPacket packetForPg = haveResponseWithPg(ghgTrackingLifetimePackets, pg);
            if (packetForPg == null) {
                // 6.1.26.12.a. Fail PG query where no response was received
                addFailure("6.1.26.12.a - No response was received from "
                        + module.getModuleName() + "for PG "
                        + pg);
            } else {
                packetForPg.getSpns().stream().filter(spn -> {
                    // FIXME: requirements need updated
                    return spn.getRawValue() > 0xFAFFFFFFL;
                }).forEach(spn -> {
                    // 6.1.26.12.b. Fail PG query where any bin value received is greater than FAFFh.
                    addFailure("6.1.26.12.b - Bin value received is greater than 0xFAFF(h)"
                            + module.getModuleName() + " returned "
                            + Arrays.toString(spn.getBytes()));
                });
            }
        }

        // 6.1.26.13 Actions7 for MY2022+ Engines
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12730 (GHG Tracking Lifetime Engine Run Time) for each 100hr GHG tracking
        // PG
        // PG PG Label
        // 64254 GHG Tracking Active 100 Hour Array Data
        // 64253 GHG Tracking Stored 100 Hour Array Data
        int[] ghgTrackingActive100HrPgs = { 64253, 64254 };
        var ghgTrackingPackets = requestPackets(module.getSourceAddress(),
                                                ghgTrackingActive100HrPgs)
                                                                          .stream()
                                                                          // 6.1.26.13.b.
                                                                          // Record
                                                                          // each value
                                                                          // for use
                                                                          // in Part 2.
                                                                          .peek(this::save)
                                                                          .collect(Collectors.toList());

        if (!ghgTrackingPackets.isEmpty()) {
            // 6.1.26.13.c. List data received in a table using lifetime, stored 100 hr,
            // active 100hr for columns, and categories for rows.
            getListener().onResult(ghgTrackingModule.formatTrackingTable(Stream.concat(ghgTrackingLifetimePackets.stream(),
                                                                                       ghgTrackingPackets.stream())
                                                                               .collect(Collectors.toList())));
        }

        for (int pg : ghgTrackingActive100HrPgs) {
            GenericPacket packetForPg = haveResponseWithPg(ghgTrackingPackets, pg);
            if (packetForPg == null) {
                // 6.1.26.14.a. For all MY2024+ engines, Fail each PG query where no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.1.26.10.a - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
                // 6.1.26.14.b. For MY2022-23 engines, Warn each PG query, where no response was received
                if (2022 <= getEngineModelYear() && getEngineModelYear() >= 2023) {
                    addWarning("6.1.26.10.b - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               if (spn.getRawValue() > 0xFAFFL) {
                                   // 6.1.26.14.c. Fail each PG query where any value received is greater than FAFFh.
                                   addFailure("6.1.26.14.c - Bin value received is greater than 0xFAFF"
                                           + module.getModuleName() + " returned "
                                           + Arrays.toString(spn.getBytes()));
                               }
                               if (spn.getSlot().toValue(spn.getBytes()) > 0) {
                                   // 6.1.26.14.d - Fail each active 100 hr array value that is greater than zero
                                   addFailure("6.1.26.14.d - Active 100 hr array value received is greater than zero"
                                           + module.getModuleName() + " returned "
                                           + Arrays.toString(spn.getBytes()));
                               }
                           });
            }
        }
    }

    private void testSp12691(OBDModuleInformation module) {
        int[] ghgTrackingLifePgs = { 64257 };
        // 6.1.26.15 Actions8 for all MY2022+ Engines
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for
        // PG 64257 Green House Gas Lifetime Active Technology Tracking.
        var ghgPackets = requestPackets(module.getSourceAddress(),
                                        ghgTrackingLifePgs)
                                                           .stream()
                                                           // 6.1.26.15.b. Record
                                                           // each value for use
                                                           // in Part 2.
                                                           .peek(this::save)
                                                           .collect(Collectors.toList());

        for (int pg : ghgTrackingLifePgs) {
            GenericPacket packetForPg = haveResponseWithPg(ghgPackets, pg);
            if (packetForPg == null) {
                // 6.1.26.16.a. Warn PG query where no response was received.
                addWarning("6.1.26.16.a - No response was received from "
                        + module.getModuleName() + "for PG "
                        + pg);
            } else {
                    packetForPg.getSpns()
                               .forEach(spn -> {
                                   // 6.1.26.16.b. Fail any accumulator value received that is greater
                                   // than FAFFFFFFh.
                                   if (spn.getRawValue() > 0xFAFFFFFFL) {
                                       addFailure("6.1.26.16.b - Bin value received is greater than 0xFAFFFFFF"
                                               + module.getModuleName() + " returned "
                                               + Arrays.toString(spn.getBytes()));
                                   }
                                   // 6.1.26.16.c. Fail PG query where any index value received is
                                   // greater than FAh.
                                   if (spn.getId() == 12691 && spn.getValue() > 0xFAL) {
                                       addFailure("6.1.26.16.c - " + spn.getId() + " PG query index received was "
                                               + spn.getValue());
                                   }

                               });
            }
        }

        // 6.1.26.17 Actions9 for MY2022+ Engines
        // SAE INTERNATIONAL J1939TM-84 Proposed Draft 24 March 2022 Page 36 of 140
        // a. DS request message to ECU that indicated support in DM24 for upon request
        // SP 12691 (GHG Tracking Lifetime Active Technology Index) for Active 100hr
        // Active technology PG, followed by DS request message to ECU for
        // Stored 100 hr Active Technology PG.
        // PG PG Label
        // 64256 Green House Gas Active 100 Hour Active Technology Tracking
        // 64255 Green House Gas Stored 100 Hour Active Technology Tracking
        int[] ghgTracking100HrPgs = { 64255, 64256 };
        // PG Acronym GHGTTA GHGTTS
        var ghg100HrPackets = requestPackets(module.getSourceAddress(),
                                             ghgTracking100HrPgs)
                                                                 .stream()
                                                                 // 6.1.26.17.b. Record
                                                                 // each value for use
                                                                 // in Part 2.
                                                                 .peek(this::save)
                                                                 .collect(Collectors.toList());
        if (!ghg100HrPackets.isEmpty()) {
            // 6.1.26.17.c. List data received in a table using lifetime, stored 100 hr,
            // active 100hr for columns, and categories for rows.
            getListener().onResult(ghgTrackingModule.formatTechTable(Stream.concat(ghgPackets.stream(),
                                                                                   ghg100HrPackets.stream())
                                                                           .collect(Collectors.toList())));
        }
        List<String> expectedLabels = new ArrayList<>();
        for (GenericPacket packet : ghgPackets) {
            for (Spn sp : packet.getSpns()) {
                expectedLabels.add(sp.getLabel().substring(sp.getLabel().indexOf("Active")));
            }
        }

        for (int pg : ghgTracking100HrPgs) {
            GenericPacket packetForPg = haveResponseWithPg(ghg100HrPackets, pg);
            if (packetForPg == null) {
                if (getEngineModelYear() >= 2024) {
                    // 6.1.26.18.a. For all MY2024+ engines, Warn each PG query where no response was received.
                    addFailure("6.1.26.18.a - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
                if (getEngineModelYear() >= 2022 && getEngineModelYear() <= 2023) {
                    // 6.1.26.18.b. For MY2022-23 engines, Warn each PG query, where no response was received
                    addWarning("6.1.26.18.b - No response was received from "
                            + module.getModuleName() + " for PG "
                            + pg);
                }
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // 6.1.26.18.c. Fail PG query where any bin value received is greater than FAFFh.
                               if (spn.getRawValue() >= 0xFAFFL) {
                                   addFailure("6.1.26.18.c - Bin value received is greater than 0xFAFF(h)"
                                           + module.getModuleName() + " returned "
                                           + spn.getBytes());
                               }
                               // 6.1.26.18.d. Fail PG query where any index value received is greater than FAh.
                               if (spn.getId() == 12691 && spn.getValue() > 0xFAL) {
                                   addFailure("6.1.26.16.c - PG query index received was " + "");
                               }
                               if (spn.getValue() > 0) {
                                   // 6.1.26.18.g. Fail each active 100 hr array value that is greater than zero
                                   addFailure("6.1.26.18.g - Active 100 hr array value received was greater than zero.  "
                                           + module.getModuleName() + " returned a value of " + spn.getValue());
                               }
                               // FIXME:
                               // 6.1.26.18.f. Fail each response where the set of labels received is not a
                               // subset of the set of labels received for the lifetime active technology
                               // response.
                               // if (!expectedLabels.contains(spn.getLabel()
                               // .substring(spn.getLabel().indexOf("Hour Active") + 5))) {
                               // addFailure("6.1.26.18.f - " + spn.getLabel());
                               // }
                           });
                // FIXME:
                // 6.1.26.18.e. Fail each response where the number of labels received are not
                // the same as the number of labels received for the lifetime technology
                // response.
                // if (packetForPg.getSpns().size() != ghgPackets.get(0).getSpns().size()) {
                // addFailure("6.1.26.18.e - Number of response labels mismatch");
                // }
            }
        }
    }

    private void testSp12675(OBDModuleInformation module) {
        int[] nOxLifeTimeSps = CollectionUtils.join(NOx_LIFETIME_SPs,
                                                    NOx_LIFETIME_ACTIVITY_SPs);
        // 6.1.26.7.a. DS request messages to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine
        var nOxPackets = requestPackets(module.getSourceAddress(),
                                        nOxLifeTimeSps)
                                                       .stream()
                                                       // 6.1.26.7.b. Record
                                                       // each value for use
                                                       // in Part 2.
                                                       .peek(this::save)
                                                       .collect(Collectors.toList());
        if (!nOxPackets.isEmpty()) {
            // 6.1.26.7.c. List data received in a table using bin numbers for rows.
            getListener().onResult(nOxBinningModule.format(nOxPackets));
        }
        for (int pg : nOxLifeTimeSps) {
            GenericPacket packetForPg = haveResponseWithPg(nOxPackets, pg);
            if (packetForPg == null) {
                // 6.1.26.8.a. Fail each PG query where no response was received.
                addFailure("6.1.26.8.a - No response was received from "
                        + module.getModuleName() + "for PG "
                        + pg);
            } else {
                packetForPg.getSpns()
                           .forEach(spn -> {
                               // 6.1.26.8.b. Fail each PG query where any bin value received
                               // is greater than FAFFFFFFh.
                               if (spn.getRawValue() >= 0xFAFFFFFFL) {
                                   addFailure("6.1.26.8.b - Bin value received is greater than 0xFAFFFFFF(h)"
                                           + module.getModuleName() + " returned "
                                           + Arrays.toString(spn.getBytes()));
                               }
                           });
            }
        }

        int[] nOx100HourSps = CollectionUtils.join(NOx_TRACKING_ACTIVE_100_HOURS_SPs,
                                                   NOx_TRACKING_STORED_100_HOURS_SPs);
        // 6.1.26.9.a - DS request message to ECU that indicated support in DM24 for upon
        // request SPN 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1
        // - Total) for each active 100hr NOx binning PG, followed by each Stored 100 hr PG
        // Label
        List<GenericPacket> nOx100HourPackets = requestPackets(module.getSourceAddress(),
                                                               nOx100HourSps)
                                                                             .stream()
                                                                             // 6.1.26.9.b -
                                                                             // Record each
                                                                             // value
                                                                             // for use in
                                                                             // Part 2.
                                                                             .peek(this::save)
                                                                             .collect(Collectors.toList());

        if (!nOx100HourPackets.isEmpty()) {
            // 6.1.26.9.c - List data received in a table using bin numbers for rows.
            getListener().onResult(nOxBinningModule.format(nOx100HourPackets));
        }
        for (int pg : nOx100HourSps) {
            GenericPacket packetForPg = haveResponseWithPg(nOx100HourPackets, pg);
            if (packetForPg == null) {
                // 6.1.26.10.a. For all MY2024+ Diesel engines, Fail each PG query where no response was received.
                if (getEngineModelYear() >= 2024) {
                    addFailure("6.1.26.10.a - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
                // 6.1.26.10.b. For all MY2022-23 Diesel engines, Warn each PG query where no response was received.
                if (2022 <= getEngineModelYear() && getEngineModelYear() >= 2023) {
                    addWarning("6.1.26.10.b - No response was received from "
                            + module.getModuleName() + "for PG "
                            + pg);
                }
            } else {
                packetForPg.getSpns().forEach(spn -> {
                    if (spn.getRawValue() >= 0xFAFFFFFFL) {
                        // 6.1.26.10.c. Fail each PG query where any bin value received is greater than FAFFh. (Use
                        // FAFFFFFFh for NOx values)
                        addFailure("6.1.26.10.c - Bin value received is greater than 0xFAFFFFFF(h)"
                                + module.getModuleName() + " returned " + Arrays.toString(spn.getBytes()) + " for "
                                + spn);

                    }
                    // 6.1.26.10.d. Fail each active 100 hr array value that is greater than zero. (where supported)
                    if (spn.getValue() > 0) {
                        // 6.1.26.10.d. Fail each active 100 hr array value that is greater than zero. (where supported)
                        addFailure("6.1.26.10.d - Active 100 hr array value received is greater than zero (where supported)"
                                + module.getModuleName() + " returned " + Arrays.toString(spn.getBytes()) + " for "
                                + spn);

                    }
                });
            }
        }
    }

}
